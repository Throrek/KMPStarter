package me.kmpstarter.ui.articledetails

import androidx.lifecycle.ViewModelStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
import me.kmpstarter.domain.articledetails.LoadArticleDetailsUseCase
import me.kmpstarter.domain.note.ArticleNote
import me.kmpstarter.domain.note.NoteStorageException
import me.kmpstarter.domain.note.SaveNoteUseCase
import me.kmpstarter.fixtures.FakeArticleRepository
import me.kmpstarter.fixtures.FakeNoteRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleNoteSavingTest {
    private val dispatcher = StandardTestDispatcher()
    private val articles = FakeArticleRepository()
    private val notes = FakeNoteRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN save is pending THEN saving is visible`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val gate = CompletableDeferred<Unit>()
            notes.beforeSave = { gate.await() }
            viewModel.onNoteChanged("Draft")
            // When
            viewModel.onSave()
            // Then
            assertEquals(ready("Draft", NoteSaveStatus.Saving), viewModel.state.value)
            assertEquals(emptyList(), notes.writes)
            runCurrent()
            assertEquals(ready("Draft", NoteSaveStatus.Saving), viewModel.state.value)
            assertEquals(listOf(ArticleNote(7, "Draft")), notes.writes)
            assertNull(notes.storedNote)
            gate.complete(Unit)
            runCurrent()
        }

    @Test
    fun `WHEN save is repeated THEN only one write occurs`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val gate = CompletableDeferred<Unit>()
            notes.beforeSave = { gate.await() }
            viewModel.onNoteChanged("Draft")
            // When
            viewModel.onSave()
            viewModel.onSave()
            runCurrent()
            viewModel.onSave()
            runCurrent()
            // Then
            assertEquals(listOf(ArticleNote(7, "Draft")), notes.writes)
            assertEquals(ready("Draft", NoteSaveStatus.Saving), viewModel.state.value)
            gate.complete(Unit)
            runCurrent()
            assertEquals(ArticleNote(7, "Draft"), notes.storedNote)
        }

    @Test
    fun `WHEN note changes during save THEN draft is unchanged`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val gate = CompletableDeferred<Unit>()
            notes.beforeSave = { gate.await() }
            viewModel.onNoteChanged("Original")
            // When
            viewModel.onSave()
            viewModel.onNoteChanged("Too early")
            runCurrent()
            viewModel.onNoteChanged("Too late")
            // Then
            assertEquals(ready("Original", NoteSaveStatus.Saving), viewModel.state.value)
            gate.complete(Unit)
            runCurrent()
            assertEquals(ready("Original", NoteSaveStatus.Saved), viewModel.state.value)
            assertEquals(ArticleNote(7, "Original"), notes.storedNote)
        }

    @Test
    fun `WHEN save fails THEN draft is retained`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            notes.beforeSave = { throw NoteStorageException(IllegalStateException("disk")) }
            viewModel.onNoteChanged("  Keep exact draft  ")
            // When
            viewModel.onSave()
            runCurrent()
            // Then
            assertEquals(
                ready("  Keep exact draft  ", NoteSaveStatus.Failed),
                viewModel.state.value,
            )
            assertNull(notes.storedNote)
        }

    @Test
    fun `WHEN save completes THEN saved state contains normalized note`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val gate = CompletableDeferred<Unit>()
            notes.beforeSave = { gate.await() }
            viewModel.onNoteChanged("  Normalized \n")
            viewModel.onSave()
            runCurrent()
            assertEquals(ready("  Normalized \n", NoteSaveStatus.Saving), viewModel.state.value)
            // When
            gate.complete(Unit)
            runCurrent()
            // Then
            assertEquals(ready("Normalized", NoteSaveStatus.Saved), viewModel.state.value)
            assertEquals(ArticleNote(7, "Normalized"), notes.storedNote)
        }

    @Test
    fun `WHEN saved note is edited THEN success message clears`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            viewModel.onNoteChanged("Saved")
            viewModel.onSave()
            runCurrent()
            assertEquals(ready("Saved", NoteSaveStatus.Saved), viewModel.state.value)
            // When
            viewModel.onNoteChanged("New draft")
            // Then
            assertEquals(ready("New draft", NoteSaveStatus.Idle), viewModel.state.value)
            assertEquals(ArticleNote(7, "Saved"), notes.storedNote)
        }

    @Test
    fun `WHEN text is blank THEN validation appears without write`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            viewModel.onNoteChanged(" \n\t ")
            // When
            viewModel.onSave()
            runCurrent()
            // Then
            assertEquals(ready(" \n\t ", NoteSaveStatus.Invalid), viewModel.state.value)
            assertEquals(emptyList(), notes.writes)
            assertNull(notes.storedNote)
        }

    @Test
    fun `WHEN ViewModel is cleared THEN no save success is published`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val store = ViewModelStore().apply { put("details", viewModel) }
            val gate = CompletableDeferred<Unit>()
            notes.beforeSave = { gate.await() }
            viewModel.onNoteChanged("Draft")
            viewModel.onSave()
            runCurrent()
            assertEquals(ready("Draft", NoteSaveStatus.Saving), viewModel.state.value)
            // When
            store.clear()
            gate.complete(Unit)
            runCurrent()
            // Then
            assertEquals(ready("Draft", NoteSaveStatus.Saving), viewModel.state.value)
            assertNull(notes.storedNote)
        }

    @Test
    fun `WHEN cleared note save completes late THEN success is not published`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val store = ViewModelStore().apply { put("details", viewModel) }
            val gate = CompletableDeferred<Unit>()
            var operationCompleted = false
            notes.beforeSave = {
                withContext(NonCancellable) { gate.await() }
                operationCompleted = true
            }
            viewModel.onNoteChanged("  Draft  ")
            viewModel.onSave()
            runCurrent()
            assertEquals(ready("  Draft  ", NoteSaveStatus.Saving), viewModel.state.value)
            assertNull(notes.storedNote)

            // When
            store.clear()
            runCurrent()
            assertFalse(operationCompleted)
            gate.complete(Unit)
            runCurrent()

            // Then
            assertTrue(operationCompleted)
            assertEquals(ArticleNote(7, "Draft"), notes.storedNote)
            assertEquals(ready("  Draft  ", NoteSaveStatus.Saving), viewModel.state.value)
        }

    @Test
    fun `WHEN cleared note save fails late THEN error is not published`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            val store = ViewModelStore().apply { put("details", viewModel) }
            val gate = CompletableDeferred<Unit>()
            var operationCompleted = false
            notes.beforeSave = {
                withContext(NonCancellable) { gate.await() }
                operationCompleted = true
                throw NoteStorageException(IllegalStateException("disk"))
            }
            viewModel.onNoteChanged("  Draft  ")
            viewModel.onSave()
            runCurrent()
            assertEquals(ready("  Draft  ", NoteSaveStatus.Saving), viewModel.state.value)
            assertNull(notes.storedNote)

            // When
            store.clear()
            runCurrent()
            assertFalse(operationCompleted)
            gate.complete(Unit)
            runCurrent()

            // Then
            assertTrue(operationCompleted)
            assertNull(notes.storedNote)
            assertEquals(ready("  Draft  ", NoteSaveStatus.Saving), viewModel.state.value)
        }

    @Test
    fun `WHEN failed save is retried THEN draft is saved without reloading`() =
        runTest(dispatcher) {
            // Given
            val viewModel = readyViewModel()
            notes.beforeSave = { throw NoteStorageException(IllegalStateException("disk")) }
            viewModel.onNoteChanged("  Keep draft  ")
            viewModel.onSave()
            runCurrent()
            assertEquals(ready("  Keep draft  ", NoteSaveStatus.Failed), viewModel.state.value)
            // When
            viewModel.onRetry()
            runCurrent()
            assertEquals(ready("  Keep draft  ", NoteSaveStatus.Failed), viewModel.state.value)
            notes.beforeSave = {}
            viewModel.onSave()
            runCurrent()
            // Then
            assertEquals(ready("Keep draft", NoteSaveStatus.Saved), viewModel.state.value)
            assertEquals(ArticleNote(7, "Keep draft"), notes.storedNote)
            assertEquals(
                listOf(ArticleNote(7, "Keep draft"), ArticleNote(7, "Keep draft")),
                notes.writes,
            )
            assertEquals(listOf(7), articles.detailRequests)
            assertEquals(listOf(7), notes.readRequests)
        }

    private fun readyViewModel(): ArticleDetailsViewModel {
        val viewModel =
            ArticleDetailsViewModel(
                7,
                LoadArticleDetailsUseCase(articles, notes),
                SaveNoteUseCase(notes),
            )
        dispatcher.scheduler.runCurrent()
        return viewModel
    }

    private fun ready(draft: String, status: NoteSaveStatus) =
        ArticleDetailsState.Ready(Article(7, "Title", "Body"), draft, status)
}
