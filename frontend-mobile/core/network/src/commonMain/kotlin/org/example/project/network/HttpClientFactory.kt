package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(config: GatewayConfig): HttpClient = HttpClient {
    defaultRequest { url(config.baseUrl) }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
