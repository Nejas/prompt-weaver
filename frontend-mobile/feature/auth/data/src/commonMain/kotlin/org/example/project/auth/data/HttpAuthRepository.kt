package org.example.project.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.RegisteredUser
import org.example.project.auth.domain.Registration

class HttpAuthRepository(
    private val client: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {
    override suspend fun register(registration: Registration): RegisteredUser = request {
        client.post("api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegistrationRequestDto.from(registration))
        }.checked().body<RegisteredUserDto>().toDomain()
    }

    private fun HttpResponse.checked(): HttpResponse = when {
        status.isSuccess() -> this
        status == HttpStatusCode.BadRequest || status == HttpStatusCode.UnprocessableEntity -> throw AuthException.InvalidRegistration()
        status == HttpStatusCode.Conflict -> throw AuthException.EmailAlreadyRegistered()
        status == HttpStatusCode.ServiceUnavailable || status == HttpStatusCode.GatewayTimeout -> throw AuthException.Unavailable()
        else -> throw AuthException.Unexpected()
    }

    private suspend fun <T> request(block: suspend () -> T): T = try {
        withContext(ioDispatcher) { block() }
    } catch (error: HttpRequestTimeoutException) {
        throw AuthException.Unavailable(error)
    } catch (error: IOException) {
        throw AuthException.Unavailable(error)
    } catch (error: SerializationException) {
        throw AuthException.Unexpected(error)
    } catch (error: ContentConvertException) {
        throw AuthException.Unexpected(error)
    }
}
