package org.example.project.catalog.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.serialization.ContentConvertException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.example.project.catalog.domain.CatalogException
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.domain.ComposedPrompt
import org.example.project.catalog.domain.TemplateDetails
import org.example.project.catalog.domain.TemplatePage

class HttpCatalogRepository(private val client: HttpClient, private val ioDispatcher: CoroutineDispatcher) : CatalogRepository {
    override suspend fun listTemplates(limit: Int, offset: Int): TemplatePage = request {
        client.get("api/v1/templates") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.checked().body<TemplatePageDto>().toDomain()
    }

    override suspend fun getTemplate(versionId: String): TemplateDetails = request {
        client.get {
            url { appendPathSegments("api", "v1", "templates", versionId) }
        }.checked().body<TemplateDetailsDto>().toDomain()
    }

    override suspend fun composePrompt(versionId: String, parameters: Map<String, String>): ComposedPrompt = request {
        client.post("api/v1/prompts/compose") {
            contentType(ContentType.Application.Json)
            setBody(ComposeRequestDto(versionId, parameters))
        }.checked().body<ComposedPromptDto>().toDomain()
    }

    private fun HttpResponse.checked(): HttpResponse {
        when {
            status.isSuccess() -> return this
            status == HttpStatusCode.BadRequest || status == HttpStatusCode.UnprocessableEntity -> throw CatalogException.InvalidInput()
            status == HttpStatusCode.NotFound -> throw CatalogException.NotFound()
            status == HttpStatusCode.ServiceUnavailable || status == HttpStatusCode.GatewayTimeout -> throw CatalogException.Unavailable()
            else -> throw CatalogException.Unexpected()
        }
    }

    private suspend fun <T> request(block: suspend () -> T): T = try {
        withContext(ioDispatcher) { block() }
    } catch (e: HttpRequestTimeoutException) {
        throw CatalogException.Unavailable(e)
    } catch (e: IOException) {
        throw CatalogException.Unavailable(e)
    } catch (e: SerializationException) {
        throw CatalogException.Unexpected(e)
    } catch (e: ContentConvertException) {
        throw CatalogException.Unexpected(e)
    }
}
