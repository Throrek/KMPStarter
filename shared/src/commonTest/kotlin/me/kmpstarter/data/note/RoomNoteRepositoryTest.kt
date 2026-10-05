package me.kmpstarter.data.note

import androidx.sqlite.SQLiteException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import me.kmpstarter.data.note.local.ArticleNoteDao
import me.kmpstarter.data.note.local.ArticleNoteEntity
import me.kmpstarter.domain.note.ArticleNote
import me.kmpstarter.domain.note.NoteStorageException

class RoomNoteRepositoryTest {
    @Test
    fun `WHEN no row exists THEN no note is returned`() = runTest {
        // Given
        val repository = RoomNoteRepository(FakeDao())
        // When
        val note = repository.loadNote(7)
        // Then
        assertNull(note)
    }

    @Test
    fun `WHEN read fails with SQLite error THEN storage failure is reported`() = runTest {
        // Given
        val sqliteFailure = SQLiteException("Disk failure")
        val repository = RoomNoteRepository(FakeDao(sqliteFailure))
        // When
        val failure = assertFailsWith<NoteStorageException> { repository.loadNote(7) }
        // Then
        assertSame(sqliteFailure, failure.cause)
    }

    @Test
    fun `WHEN write fails with SQLite error THEN storage failure is reported`() = runTest {
        // Given
        val sqliteFailure = SQLiteException("Disk full")
        val repository = RoomNoteRepository(FakeDao(sqliteFailure))
        // When
        val failure =
            assertFailsWith<NoteStorageException> { repository.saveNote(ArticleNote(7, "My note")) }
        // Then
        assertSame(sqliteFailure, failure.cause)
    }

    @Test
    fun `WHEN DAO is cancelled THEN cancellation propagates`() = runTest {
        // Given
        val cancellation = CancellationException("Screen left")
        val repository = RoomNoteRepository(FakeDao(cancellation))
        // When
        val readFailure = assertFailsWith<CancellationException> { repository.loadNote(7) }
        val writeFailure =
            assertFailsWith<CancellationException> {
                repository.saveNote(ArticleNote(7, "My note"))
            }
        // Then
        assertSame(cancellation, readFailure)
        assertSame(cancellation, writeFailure)
    }

    @Test
    fun `WHEN DAO has programming failure THEN failure propagates`() = runTest {
        // Given
        val programmingFailure = IllegalStateException("DAO invariant failed")
        val repository = RoomNoteRepository(FakeDao(programmingFailure))
        // When
        val failure = assertFailsWith<IllegalStateException> { repository.loadNote(7) }
        // Then
        assertEquals("DAO invariant failed", failure.message)
    }

    private class FakeDao(private val failure: Throwable? = null) : ArticleNoteDao {
        override suspend fun load(articleId: Int): ArticleNoteEntity? {
            failure?.let { throw it }
            return null
        }

        override suspend fun upsert(note: ArticleNoteEntity) {
            failure?.let { throw it }
        }
    }
}
