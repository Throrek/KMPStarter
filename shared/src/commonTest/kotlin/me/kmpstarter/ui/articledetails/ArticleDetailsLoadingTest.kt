package me.kmpstarter.ui.articledetails

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
import me.kmpstarter.domain.articledetails.LoadArticleDetailsUseCase
import me.kmpstarter.domain.note.ArticleNote
import me.kmpstarter.domain.note.NoteStorageException
import me.kmpstarter.domain.note.SaveNoteUseCase
import me.kmpstarter.fixtures.FakeArticleRepository
import me.kmpstarter.fixtures.FakeNoteRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailsLoadingTest {
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
    fun `WHEN note is loading THEN editor is not enabled`() =
        runTest(dispatcher) {
            // Given
            val articles = FakeArticleRepository()
            val gate = CompletableDeferred<Unit>()
            val notes =
                FakeNoteRepository().apply {
                    onLoadNote = {
                        gate.await()
                        ArticleNote(7, "Saved note")
                    }
                }
            // When
            val viewModel = viewModel(7, articles, notes)
            runCurrent()
            viewModel.onNoteChanged("Premature edit")
            viewModel.onSave()
            // Then
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)
            assertEquals(listOf(7), notes.readRequests)
            assertEquals(emptyList(), notes.writes)
            gate.complete(Unit)
            runCurrent()
            assertEquals(
                ArticleDetailsState.Ready(Article(7, "Title", "Body"), "Saved note"),
                viewModel.state.value,
            )
        }

    @Test
    fun `WHEN note read fails THEN editor is not shown`() =
        runTest(dispatcher) {
            // Given
            val notes =
                FakeNoteRepository().apply {
                    onLoadNote = { throw NoteStorageException(IllegalStateException("disk")) }
                }
            val viewModel = viewModel(7, FakeArticleRepository(), notes)
            // When
            runCurrent()
            viewModel.onNoteChanged("Unsafe overwrite")
            viewModel.onSave()
            runCurrent()
            // Then
            assertEquals(
                ArticleDetailsState.Failed(ArticleDetailsFailure.NoteStorage),
                viewModel.state.value,
            )
            assertEquals(emptyList(), notes.writes)
        }

    @Test
    fun `WHEN article ID is invalid THEN failure appears without IO`() =
        runTest(dispatcher) {
            // Given
            val articles = FakeArticleRepository()
            val notes = FakeNoteRepository()
            // When
            val viewModels = listOf(viewModel(0, articles, notes), viewModel(-7, articles, notes))
            viewModels.forEach {
                it.onRetry()
                it.onSave()
            }
            runCurrent()
            // Then
            viewModels.forEach {
                assertEquals(
                    ArticleDetailsState.Failed(ArticleDetailsFailure.InvalidArticleId),
                    it.state.value,
                )
            }
            assertEquals(emptyList(), articles.detailRequests)
            assertEquals(emptyList(), notes.readRequests)
            assertEquals(emptyList(), notes.writes)
        }

    @Test
    fun `WHEN details open directly THEN article and saved note are loaded by ID`() =
        runTest(dispatcher) {
            // Given
            val articles = FakeArticleRepository()
            val notes = FakeNoteRepository().apply { onLoadNote = { ArticleNote(it, "Stored") } }
            // When
            val viewModel = viewModel(42, articles, notes)
            runCurrent()
            // Then
            assertEquals(
                ArticleDetailsState.Ready(Article(42, "Title", "Body"), "Stored"),
                viewModel.state.value,
            )
            assertEquals(0, articles.listRequests)
            assertEquals(listOf(42), articles.detailRequests)
            assertEquals(listOf(42), notes.readRequests)
        }

    @Test
    fun `WHEN retry is called with unsaved draft THEN draft is not reloaded`() =
        runTest(dispatcher) {
            // Given
            val articles = FakeArticleRepository()
            val notes = FakeNoteRepository().apply { onLoadNote = { ArticleNote(7, "Old") } }
            val viewModel = viewModel(7, articles, notes)
            runCurrent()
            viewModel.onNoteChanged("Local draft")
            // When
            viewModel.onRetry()
            runCurrent()
            // Then
            assertEquals(
                ArticleDetailsState.Ready(Article(7, "Title", "Body"), "Local draft"),
                viewModel.state.value,
            )
            assertEquals(listOf(7), articles.detailRequests)
            assertEquals(listOf(7), notes.readRequests)
        }

    @Test
    fun `WHEN article is not found THEN failure appears without reading note`() =
        runTest(dispatcher) {
            // Given
            val articles =
                FakeArticleRepository().apply {
                    onLoadArticle = { throw ArticleReadException(ArticleReadFailure.NotFound) }
                }
            val notes = FakeNoteRepository()
            // When
            val viewModel = viewModel(7, articles, notes)
            runCurrent()
            // Then
            assertEquals(
                ArticleDetailsState.Failed(
                    ArticleDetailsFailure.Article(ArticleReadFailure.NotFound)
                ),
                viewModel.state.value,
            )
            assertEquals(emptyList(), notes.readRequests)
        }

    @Test
    fun `WHEN details retry is pending THEN no second read starts`() =
        runTest(dispatcher) {
            // Given
            val articles = FakeArticleRepository()
            val notes =
                FakeNoteRepository().apply {
                    onLoadNote = { throw NoteStorageException(IllegalStateException("disk")) }
                }
            val viewModel = viewModel(7, articles, notes)
            runCurrent()
            assertEquals(
                ArticleDetailsState.Failed(ArticleDetailsFailure.NoteStorage),
                viewModel.state.value,
            )
            val gate = CompletableDeferred<Unit>()
            notes.onLoadNote = {
                gate.await()
                null
            }
            // When
            viewModel.onRetry()
            viewModel.onRetry()
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)
            runCurrent()
            viewModel.onRetry()
            // Then
            assertEquals(listOf(7, 7), articles.detailRequests)
            assertEquals(listOf(7, 7), notes.readRequests)
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)
            gate.complete(Unit)
            runCurrent()
            assertEquals(
                ArticleDetailsState.Ready(Article(7, "Title", "Body"), ""),
                viewModel.state.value,
            )
        }

    @Test
    fun `WHEN cleared details load completes late THEN editor is not published`() =
        runTest(dispatcher) {
            // Given
            val gate = CompletableDeferred<Unit>()
            var operationCompleted = false
            val notes =
                FakeNoteRepository().apply {
                    onLoadNote = {
                        withContext(NonCancellable) { gate.await() }
                        operationCompleted = true
                        ArticleNote(7, "Late note")
                    }
                }
            val viewModel = viewModel(7, FakeArticleRepository(), notes)
            val store = ViewModelStore().apply { put("details", viewModel) }
            runCurrent()
            assertEquals(listOf(7), notes.readRequests)
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)

            // When
            store.clear()
            runCurrent()
            assertFalse(operationCompleted)
            gate.complete(Unit)
            runCurrent()

            // Then
            assertTrue(operationCompleted)
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)
        }

    @Test
    fun `WHEN cleared details load fails late THEN error is not published`() =
        runTest(dispatcher) {
            // Given
            val gate = CompletableDeferred<Unit>()
            var operationCompleted = false
            val notes =
                FakeNoteRepository().apply {
                    onLoadNote = {
                        withContext(NonCancellable) { gate.await() }
                        operationCompleted = true
                        throw NoteStorageException(IllegalStateException("disk"))
                    }
                }
            val viewModel = viewModel(7, FakeArticleRepository(), notes)
            val store = ViewModelStore().apply { put("details", viewModel) }
            runCurrent()
            assertEquals(listOf(7), notes.readRequests)
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)

            // When
            store.clear()
            runCurrent()
            assertFalse(operationCompleted)
            gate.complete(Unit)
            runCurrent()

            // Then
            assertTrue(operationCompleted)
            assertEquals(ArticleDetailsState.Loading, viewModel.state.value)
        }

    private fun viewModel(id: Int, articles: FakeArticleRepository, notes: FakeNoteRepository) =
        ArticleDetailsViewModel(
            id,
            LoadArticleDetailsUseCase(articles, notes),
            SaveNoteUseCase(notes),
        )
}
