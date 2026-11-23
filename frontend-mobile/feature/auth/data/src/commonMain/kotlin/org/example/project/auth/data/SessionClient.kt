package org.example.project.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.SessionTokens
import org.example.project.auth.domain.TokenStore
import org.example.project.auth.domain.RegisteredUser
import org.example.project.auth.domain.Registration

/** Serializes session changes so rotating refresh tokens cannot be used concurrently. */
class SessionClient(private val client: HttpClient, private val store: TokenStore) {
    private val mutex = Mutex()

    suspend fun register(registration: Registration): RegisteredUser = mutex.withLock {
        try {
            val response = client.post("api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegistrationRequestDto.from(registration))
            }
            when (response.status) {
                HttpStatusCode.BadRequest, HttpStatusCode.UnprocessableEntity -> throw AuthException.InvalidRegistration()
                HttpStatusCode.Conflict -> throw AuthException.EmailAlreadyRegistered()
                else -> if (!response.status.isSuccess()) {
                    if (response.status.value >= 500) throw AuthException.Unavailable()
                    throw AuthException.Unexpected()
                }
            }
            val result = response.body<RegisteredUserDto>()
            val tokens = result.tokens()
            try {
                store.write(tokens)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                throw AuthException.RegistrationSessionNotSaved(error)
            }
            result.toDomain()
        } catch (error: HttpRequestTimeoutException) {
            throw AuthException.Unavailable(error)
        } catch (error: IOException) {
            throw AuthException.Unavailable(error)
        } catch (error: SerializationException) {
            throw AuthException.Unexpected(error)
        } catch (error: ContentConvertException) {
            throw AuthException.Unexpected(error)
        } catch (error: IllegalArgumentException) {
            throw AuthException.Unexpected(error)
        }
    }

    suspend fun login(email: String, password: String): SessionTokens = mutex.withLock {
        val tokens = requestTokens("api/v1/auth/login", LoginRequest(email.trim(), password))
        store.write(tokens)
        tokens
    }

    suspend fun refresh(): SessionTokens? = mutex.withLock {
        val old = store.read() ?: return@withLock null
        try {
            val tokens = requestTokens("api/v1/auth/refresh", RefreshRequest(old.refreshToken))
            store.write(tokens)
            tokens
        } catch (error: AuthException.Unauthorized) {
            withContext(NonCancellable) { store.clear() }
            throw error
        }
    }

    /** Returns false when local logout succeeded but server revocation could not be confirmed. */
    suspend fun logout(): Boolean = mutex.withLock {
        val old = store.read()
        try {
            if (old == null) true else {
                val response = client.post("api/v1/auth/logout") {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshRequest(old.refreshToken))
                }
                response.status.isSuccess()
            }
        } catch (_: IOException) {
            false
        } catch (_: HttpRequestTimeoutException) {
            false
        } finally {
            withContext(NonCancellable) { store.clear() }
        }
    }

    private suspend inline fun <reified T : Any> requestTokens(path: String, body: T): SessionTokens {
        try {
            val response = client.post(path) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            if (response.status == HttpStatusCode.Unauthorized) throw AuthException.Unauthorized()
            if (!response.status.isSuccess()) {
                if (response.status.value >= 500) throw AuthException.Unavailable()
                throw AuthException.Unexpected()
            }
            return response.body<TokenDto>().toDomain()
        } catch (error: HttpRequestTimeoutException) {
            throw AuthException.Unavailable(error)
        } catch (error: IOException) {
            throw AuthException.Unavailable(error)
        } catch (error: IllegalArgumentException) {
            throw AuthException.Unexpected(error)
        } catch (error: SerializationException) {
            throw AuthException.Unexpected(error)
        } catch (error: ContentConvertException) {
            throw AuthException.Unexpected(error)
        }
    }
}
