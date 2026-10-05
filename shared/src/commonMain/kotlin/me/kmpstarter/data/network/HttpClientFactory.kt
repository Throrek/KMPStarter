package me.kmpstarter.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(private val engine: HttpClientEngine) {
    fun create(): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            defaultRequest { url(API_BASE_URL) }
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(HttpTimeout) {
                requestTimeoutMillis = HTTP_TIMEOUT_MILLIS
                connectTimeoutMillis = HTTP_TIMEOUT_MILLIS
                socketTimeoutMillis = HTTP_TIMEOUT_MILLIS
            }
        }

    companion object {
        private const val HTTP_TIMEOUT_MILLIS = 15_000L
        private const val API_BASE_URL = "https://jsonplaceholder.typicode.com"
    }
}

expect fun createHttpEngine(): HttpClientEngine
