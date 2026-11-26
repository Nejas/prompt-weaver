package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

fun createHttpClient(config: GatewayConfig): HttpClient = HttpClient {
    defaultRequest { url(config.baseUrl) }
    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        requestTimeoutMillis = 15_000
        socketTimeoutMillis = 15_000
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpSend)
}.also { client ->
    client.plugin(HttpSend).intercept { request ->
        val path = safeLogPath(request.url.build())
        NetworkLog.debug("→ ${request.method.value} $path")
        try {
            execute(request).also { call ->
                NetworkLog.debug("← ${call.response.status.value} ${request.method.value} $path")
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            NetworkLog.error("× ${request.method.value} $path: network request failed")
            throw error
        }
    }
}

internal fun safeLogPath(url: Url): String = url.encodedPath
