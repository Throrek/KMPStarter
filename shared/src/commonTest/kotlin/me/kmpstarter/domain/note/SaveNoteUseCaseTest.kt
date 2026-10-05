package me.kmpstarter.domain.note

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class SaveNoteUseCaseTest {
    @Test
    fun `WHEN text is blank THEN note is not saved`() = runTest {
        // Given
        val repository = FakeNotes()
        val save = SaveNoteUseCase(repository)
        // When
        val result = save(7, " \n ")
        // Then
        assertEquals(SaveNoteResult.InvalidText, result)
        assertEquals(emptyList(), repository.saved)
    }

    @Test
    fun `WHEN text has outer whitespace THEN trimmed note is saved`() = runTest {
        // Given
        val repository = FakeNotes()
        val save = SaveNoteUseCase(repository)
        // When
        val result = save(7, "  My note  ")
        // Then
        assertEquals(SaveNoteResult.Saved(ArticleNote(7, "My note")), result)
        assertEquals(listOf(ArticleNote(7, "My note")), repository.saved)
    }

    @Test
    fun `WHEN article ID is invalid THEN no repository is called`() = runTest {
        // Given
        val repository = FakeNotes()
        val save = SaveNoteUseCase(repository)
        // When
        for (id in listOf(0, -7)) {
            assertFailsWith<IllegalArgumentException> { save(id, "My note") }
        }
        // Then
        assertEquals(emptyList(), repository.saved)
    }

    @Test
    fun `WHEN persistence fails THEN save does not report success`() = runTest {
        // Given
        val repository = FakeNotes(NoteStorageException(IllegalStateException("Disk failure")))
        val save = SaveNoteUseCase(repository)
        // When
        assertFailsWith<NoteStorageException> { save(7, "My note") }
        // Then
        assertEquals(emptyList(), repository.saved)
    }

    private class FakeNotes(private val failure: NoteStorageException? = null) : NoteRepository {
        val saved = mutableListOf<ArticleNote>()

        override suspend fun loadNote(articleId: Int): ArticleNote? = error("Unexpected read")

        override suspend fun saveNote(note: ArticleNote) {
            failure?.let { throw it }
            saved += note
        }
    }
}
