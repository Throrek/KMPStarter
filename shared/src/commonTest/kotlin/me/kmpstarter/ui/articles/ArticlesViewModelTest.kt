package me.kmpstarter.ui.articles

import androidx.lifecycle.ViewModelStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleReadException
import me.kmpstarter.domain.article.ArticleReadFailure
import me.kmpstarter.domain.article.LoadArticlesUseCase
import me.kmpstarter.fixtures.FakeArticleRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN request is pending THEN loading is visible`() =
        runTest(dispatcher) {
            // Given
            val gate = CompletableDeferred<Unit>()
            val repository =
                FakeArticleRepository().apply {
                    onLoadArticles = {
                        gate.await()
                        listOf(Article(7, "Title", "Body"))
                    }
                }
            // When
            val viewModel = ArticlesViewModel(LoadArticlesUseCase(repository))
            runCurrent()
            // Then
            assertEquals(ArticlesState.Loading, viewModel.state.value)
            assertEquals(1, repository.listRequests)
            gate.complete(Unit)
            runCurrent()
        }

    @Test
    fun `WHEN articles are loaded THEN content is visible`() =
        runTest(dispatcher) {
            // Given
            val repository = FakeArticleRepository()
            // When
            val viewModel = ArticlesViewModel(LoadArticlesUseCase(repository))
            runCurrent()
            // Then
            assertEquals(
                ArticlesState.Content(listOf(Article(7, "Title", "Body"))),
                viewModel.state.value,
            )
        }

    @Test
    fun `WHEN request fails THEN retry can load articles`() =
        runTest(dispatcher) {
            // Given
            val repository =
                FakeArticleRepository().apply {
                    onLoadArticles = { throw ArticleReadException(ArticleReadFailure.Connection) }
                }
            val viewModel = ArticlesViewModel(LoadArticlesUseCase(repository))
            runCurrent()
            assertEquals(ArticlesState.Failed(ArticleReadFailure.Connection), viewModel.state.value)
            repository.onLoadArticles = { listOf(Article(7, "Title", "Body")) }
            // When
            viewModel.onRetry()
            assertEquals(ArticlesState.Loading, viewModel.state.value)
            runCurrent()
            // Then
            assertEquals(
                ArticlesState.Content(listOf(Article(7, "Title", "Body"))),
                viewModel.state.value,
            )
            assertEquals(2, repository.listRequests)
        }

    @Test
    fun `WHEN retry is already running THEN no second request starts`() =
        runTest(dispatcher) {
            // Given
            val repository =
                FakeArticleRepository().apply {
                    onLoadArticles = { throw ArticleReadException(ArticleReadFailure.Timeout) }
                }
            val viewModel = ArticlesViewModel(LoadArticlesUseCase(repository))
            runCurrent()
            val gate = CompletableDeferred<Unit>()
            repository.onLoadArticles = {
                gate.await()
                listOf(Article(7, "Title", "Body"))
            }
            // When
            viewModel.onRetry()
            viewModel.onRetry()
            runCurrent()
            viewModel.onRetry()
            runCurrent()
            // Then
            assertEquals(ArticlesState.Loading, viewModel.state.value)
            assertEquals(2, repository.listRequests)
            gate.complete(Unit)
            runCurrent()
        }

    @Test
    fun `WHEN cleared list load completes late THEN content is not published`() =
        runTest(dispatcher) {
            // Given
            val gate = CompletableDeferred<Unit>()
            var operationCompleted = false
            val repository =
                FakeArticleRepository().apply {
                    onLoadArticles = {
                        withContext(NonCancellable) { gate.await() }
                        operationCompleted = true
                        listOf(Article(7, "Late title", "Late body"))
                    }
                }
            val viewModel = ArticlesViewModel(LoadArticlesUseCase(repository))
            val store = ViewModelStore().apply { put("articles", viewModel) }
            runCurrent()
            assertEquals(1, repository.listRequests)
            assertEquals(ArticlesState.Loading, viewModel.state.value)

            // When
            store.clear()
            runCurrent()
            assertFalse(operationCompleted)
            gate.complete(Unit)
            runCurrent()

            // Then
            assertTrue(operationCompleted)
            assertEquals(ArticlesState.Loading, viewModel.state.value)
        }

    @Test
    fun `WHEN cleared list load fails late THEN error is not published`() =
        runTest(dispatcher) {
            // Given
            val gate = CompletableDeferred<Unit>()
            var operationCompleted = false
            val repository =
                FakeArticleRepository().apply {
                    onLoadArticles = {
                        withContext(NonCancellable) { gate.await() }
                        operationCompleted = true
                        throw ArticleReadException(ArticleReadFailure.Connection)
                    }
                }
            val viewModel = ArticlesViewModel(LoadArticlesUseCase(repository))
            val store = ViewModelStore().apply { put("articles", viewModel) }
            runCurrent()
            assertEquals(1, repository.listRequests)
            assertEquals(ArticlesState.Loading, viewModel.state.value)

            // When
            store.clear()
            runCurrent()
            assertFalse(operationCompleted)
            gate.complete(Unit)
            runCurrent()

            // Then
            assertTrue(operationCompleted)
            assertEquals(ArticlesState.Loading, viewModel.state.value)
        }
}
