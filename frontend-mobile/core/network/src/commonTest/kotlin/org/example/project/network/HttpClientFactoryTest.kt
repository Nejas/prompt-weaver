package org.example.project.network

import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpClientFactoryTest {
    @Test
    fun safeLogPathOmitsQueryParameters() {
        val path = safeLogPath(Url("https://gateway.example/api/v1/templates?token=secret&limit=20"))

        assertEquals("/api/v1/templates", path)
    }
}
