package org.example.project.catalog.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.project.catalog.domain.CatalogException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpCatalogRepositoryTest {
    private fun repository(dispatcher: CoroutineDispatcher = Dispatchers.Unconfined, handler: io.ktor.client.engine.mock.MockRequestHandler) = HttpCatalogRepository(
        HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { url("http://localhost/") }
        },
        dispatcher
    )

    @Test
    fun listMapsPageAndSendsPagination() = runTest {
        val repo = repository { request ->
            assertEquals("10", request.url.parameters["limit"])
            assertEquals("20", request.url.parameters["offset"])
            respond("""{"templates":[{"id":"v1","title":"Title","description":"D","category":"C","slug":"s"}],"limit":10,"offset":20}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val page = repo.listTemplates(10, 20)
        assertEquals("v1", page.templates.single().versionId)
        assertEquals(20, page.offset)
    }

    @Test
    fun notFoundBecomesDomainError() = runTest {
        val repo = repository { respond("", HttpStatusCode.NotFound) }
        assertFailsWith<CatalogException.NotFound> { repo.getTemplate("missing") }
    }

    @Test
    fun detailsMapParameters() = runTest {
        val repo = repository { request ->
            assertEquals("/api/v1/templates/v1", request.url.encodedPath)
            respond("""{"id":"v1","title":"T","description":"D","category":"C","slug":"s","body":"Hello","parameters":[{"name":"x","label":"X","type":"text","required":true,"max_length":20,"options":[]}]}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        assertEquals(20, repo.getTemplate("v1").parameters.single().maxLength)
    }

    @Test
    fun composeMapsResponse() = runTest {
        val repo = repository { request ->
            assertEquals("/api/v1/prompts/compose", request.url.encodedPath)
            assertEquals("POST", request.method.value)
            respond("""{"template_version_id":"v1","prompt":"Hello"}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        assertEquals("Hello", repo.composePrompt("v1", mapOf("x" to "value")).text)
    }

    @Test
    fun invalidAndUnavailableResponsesBecomeDomainErrors() = runTest {
        val invalid = repository { respond("", HttpStatusCode.BadRequest) }
        val unavailable = repository { respond("", HttpStatusCode.ServiceUnavailable) }
        assertFailsWith<CatalogException.InvalidInput> { invalid.listTemplates() }
        assertFailsWith<CatalogException.Unavailable> { unavailable.listTemplates() }
    }

    @Test
    fun requestUsesInjectedDispatcherAndPropagatesCancellation() = runTest {
        val io = StandardTestDispatcher(testScheduler)
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val repo = repository(io) {
            started.complete(Unit)
            try { awaitCancellation() } finally { cancelled.complete(Unit) }
        }
        val job = launch(start = CoroutineStart.UNDISPATCHED) { repo.listTemplates() }
        assertEquals(false, started.isCompleted)
        testScheduler.runCurrent()
        started.await()
        job.cancelAndJoin()
        cancelled.await()
        assertEquals(true, job.isCancelled)
    }

    @Test
    fun malformedJsonBecomesDomainError() = runTest {
        val repo = repository { respond("{", headers = headersOf(HttpHeaders.ContentType, "application/json")) }
        assertFailsWith<CatalogException.Unexpected> { repo.listTemplates() }
    }
}
