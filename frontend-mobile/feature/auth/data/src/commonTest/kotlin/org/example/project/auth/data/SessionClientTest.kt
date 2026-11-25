package org.example.project.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.project.auth.domain.SessionTokens
import org.example.project.auth.domain.TokenStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionClientTest {
    @Test
    fun loginUsesInjectedIoDispatcher() = runTest {
        val io = StandardTestDispatcher(testScheduler)
        var requests = 0
        val client = sessionClient(io) {
            requests++
            respond(
                """{"access_token":"access","refresh_token":"refresh","expires_in":900,"token_type":"bearer"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val job = launch(start = CoroutineStart.UNDISPATCHED) { client.login("user@example.com", "password") }
        assertEquals(0, requests)
        testScheduler.runCurrent()
        job.join()
        assertEquals(1, requests)
    }

    @Test
    fun logoutPropagatesCancellation() = runTest {
        val store = InMemoryTokenStore(SessionTokens("access", "refresh", 900))
        val client = sessionClient(store = store) { throw CancellationException("request cancelled") }

        assertFailsWith<CancellationException> { client.logout() }
        assertEquals(null, store.read())
    }

    private fun sessionClient(
        ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Unconfined,
        store: InMemoryTokenStore = InMemoryTokenStore(),
        handler: io.ktor.client.engine.mock.MockRequestHandler,
    ) = SessionClient(
        client = HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { url("http://localhost/") }
        },
        store = store,
        ioDispatcher = ioDispatcher,
    )

    private class InMemoryTokenStore(initial: SessionTokens? = null) : TokenStore {
        private var tokens = initial
        override suspend fun read() = tokens
        override suspend fun write(tokens: SessionTokens) { this.tokens = tokens }
        override suspend fun clear() { tokens = null }
    }
}
