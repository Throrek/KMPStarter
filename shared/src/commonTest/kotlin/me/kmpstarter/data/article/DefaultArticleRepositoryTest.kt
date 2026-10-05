package me.kmpstarter.data.article

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import me.kmpstarter.data.article.network.ArticleRemoteDataSource
import me.kmpstarter.data.network.HttpClientFactory
import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleReadException
import me.kmpstarter.domain.article.ArticleReadFailure

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultArticleRepositoryTest {
    @Test
    fun `WHEN response is valid THEN articles are loaded`() = runTest {
        // Given
        withRepository({ request ->
            assertEquals("https://jsonplaceholder.typicode.com/posts", request.url.toString())
            assertEquals(HttpMethod.Get, request.method)
            respond(
                """[{"userId":1,"id":7,"title":"Kotlin","body":"Shared code"}]""",
                headers = jsonHeaders,
            )
        }) { repository ->
            // When
            val articles = repository.loadArticles()

            // Then
            assertEquals(listOf(Article(7, "Kotlin", "Shared code")), articles)
        }
    }

    @Test
    fun `WHEN list is empty THEN empty list is returned`() = runTest {
        // Given
        withRepository({ respond("[]", headers = jsonHeaders) }) { repository ->
            // When
            val articles = repository.loadArticles()

            // Then
            assertEquals(emptyList(), articles)
        }
    }

    @Test
    fun `WHEN details are requested THEN article is loaded by ID`() = runTest {
        // Given
        withRepository({ request ->
            assertEquals("/posts/7", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)
            respond(
                """{"userId":1,"id":7,"title":"Kotlin","body":"Shared code"}""",
                headers = jsonHeaders,
            )
        }) { repository ->
            // When
            val article = repository.loadArticle(7)

            // Then
            assertEquals(Article(7, "Kotlin", "Shared code"), article)
        }
    }

    @Test
    fun `WHEN server returns 500 THEN HTTP failure is reported`() = runTest {
        // Given
        var requests = 0
        withRepository({
            requests++
            respond("Internal error", HttpStatusCode.InternalServerError)
        }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

            // Then
            assertEquals(ArticleReadFailure.Http, failure.reason)
            assertEquals(1, requests)
        }
    }

    @Test
    fun `WHEN article returns 404 THEN not found is reported`() = runTest {
        // Given
        withRepository({ respond("{}", HttpStatusCode.NotFound, jsonHeaders) }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticle(7) }

            // Then
            assertEquals(ArticleReadFailure.NotFound, failure.reason)
        }
    }

    @Test
    fun `WHEN list returns 404 THEN HTTP failure is reported`() = runTest {
        // Given
        withRepository({ respond("{}", HttpStatusCode.NotFound, jsonHeaders) }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

            // Then
            assertEquals(ArticleReadFailure.Http, failure.reason)
        }
    }

    @Test
    fun `WHEN connection fails THEN connection failure is reported`() = runTest {
        // Given
        withRepository({ throw IOException("Connection refused") }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

            // Then
            assertEquals(ArticleReadFailure.Connection, failure.reason)
        }
    }

    @Test
    fun `WHEN request times out THEN timeout failure is reported`() = runTest {
        // Given
        withRepository({ request -> throw HttpRequestTimeoutException(request) }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

            // Then
            assertEquals(ArticleReadFailure.Timeout, failure.reason)
        }
    }

    @Test
    fun `WHEN transport times out THEN timeout failure is reported`() = runTest {
        // Given
        val timeouts =
            listOf(
                ConnectTimeoutException("Connection timed out"),
                SocketTimeoutException("Read timed out"),
            )
        for (timeout in timeouts) {
            withRepository({ throw timeout }) { repository ->
                // When
                val failure = assertFailsWith<ArticleReadException> { repository.loadArticle(7) }

                // Then
                assertEquals(ArticleReadFailure.Timeout, failure.reason)
            }
        }
    }

    @Test
    fun `WHEN request exceeds 15 seconds THEN timeout failure is reported`() = runTest {
        // Given
        val started = CompletableDeferred<Unit>()
        withRepository({
            started.complete(Unit)
            awaitCancellation()
        }) { repository ->
            val failure = async {
                assertFailsWith<ArticleReadException> { repository.loadArticles() }
            }
            started.await()

            // When
            advanceTimeBy(14_999)
            runCurrent()
            assertTrue(failure.isActive)
            advanceTimeBy(1)
            runCurrent()

            // Then
            assertEquals(ArticleReadFailure.Timeout, failure.await().reason)
            assertEquals(15_000, testScheduler.currentTime)
        }
    }

    @Test
    fun `WHEN payload is malformed THEN invalid response is reported`() = runTest {
        // Given
        val payloads = listOf("not JSON", """[{"id":7,"title":"Kotlin"}]""", "{}")
        for (payload in payloads) {
            withRepository({ respond(payload, headers = jsonHeaders) }) { repository ->
                // When
                val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

                // Then
                assertEquals(ArticleReadFailure.InvalidResponse, failure.reason)
            }
        }
    }

    @Test
    fun `WHEN payload has invalid articles THEN invalid response is reported`() = runTest {
        // Given
        val payloads =
            listOf(
                """[{"id":0,"title":"Kotlin","body":"Shared code"}]""",
                """[{"id":-1,"title":"Kotlin","body":"Shared code"}]""",
                """[{"id":7,"title":"","body":"Shared code"}]""",
                """[{"id":7,"title":"  ","body":"Shared code"}]""",
                """[{"id":7,"title":"Kotlin","body":"Shared code"},{"id":7,"title":"Second","body":"Duplicate"}]""",
            )
        for (payload in payloads) {
            withRepository({ respond(payload, headers = jsonHeaders) }) { repository ->
                // When
                val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

                // Then
                assertEquals(ArticleReadFailure.InvalidResponse, failure.reason)
            }
        }
    }

    @Test
    fun `WHEN details response has different ID THEN invalid response is reported`() = runTest {
        // Given
        withRepository({ request ->
            assertEquals("/posts/7", request.url.encodedPath)
            respond("""{"id":8,"title":"Other","body":"Wrong article"}""", headers = jsonHeaders)
        }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticle(7) }

            // Then
            assertEquals(ArticleReadFailure.InvalidResponse, failure.reason)
        }
    }

    @Test
    fun `WHEN response is not JSON THEN invalid response is reported`() = runTest {
        // Given
        withRepository({
            respond("<html>Error</html>", headers = headersOf(HttpHeaders.ContentType, "text/html"))
        }) { repository ->
            // When
            val failure = assertFailsWith<ArticleReadException> { repository.loadArticles() }

            // Then
            assertEquals(ArticleReadFailure.InvalidResponse, failure.reason)
        }
    }

    @Test
    fun `WHEN request is cancelled THEN cancellation propagates`() = runTest {
        // Given
        val started = CompletableDeferred<Unit>()
        val propagated = CompletableDeferred<CancellationException>()
        val cancellation = CancellationException("Screen left")
        withRepository({
            started.complete(Unit)
            awaitCancellation()
        }) { repository ->
            val request = async {
                try {
                    repository.loadArticles()
                } catch (failure: CancellationException) {
                    propagated.complete(failure)
                    throw failure
                }
            }
            started.await()

            // When
            request.cancel(cancellation)
            request.join()

            // Then
            assertTrue(propagated.isCompleted)
            assertEquals("Screen left", propagated.await().message)
            assertFailsWith<CancellationException> { request.await() }
        }
    }

    @Test
    fun `WHEN programming failure occurs THEN failure propagates`() = runTest {
        // Given
        val programmingFailure = IllegalStateException("Broken engine configuration")
        withRepository({ throw programmingFailure }) { repository ->
            // When
            val failure = assertFailsWith<IllegalStateException> { repository.loadArticles() }

            // Then
            assertEquals("Broken engine configuration", failure.message)
        }
    }

    private suspend fun TestScope.withRepository(
        handler: MockRequestHandler,
        block: suspend (DefaultArticleRepository) -> Unit,
    ) {
        val engine =
            MockEngine(
                MockEngineConfig().apply {
                    dispatcher = StandardTestDispatcher(testScheduler)
                    addHandler(handler)
                }
            )
        val client = HttpClientFactory(engine).create()
        try {
            block(DefaultArticleRepository(ArticleRemoteDataSource(client)))
        } finally {
            client.close()
            engine.close()
        }
    }

    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
}
