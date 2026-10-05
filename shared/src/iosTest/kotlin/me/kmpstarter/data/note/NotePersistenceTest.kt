package me.kmpstarter.data.note

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import me.kmpstarter.data.local.StarterDatabase
import me.kmpstarter.data.local.createStarterDatabase
import me.kmpstarter.domain.note.ArticleNote
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

@OptIn(ExperimentalForeignApi::class)
class NotePersistenceTest {
    @Test
    fun `WHEN database reopens THEN latest note is restored`() = runTest {
        // Given
        val directory = NSTemporaryDirectory() + TEST_DIRECTORY_PREFIX + NSUUID().UUIDString
        val files = NSFileManager.defaultManager
        check(
            files.createDirectoryAtPath(
                directory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        )
        val path = "$directory/${StarterDatabase.FILE_NAME}"
        try {
            val database = createStarterDatabase(path)
            try {
                val repository = RoomNoteRepository(database.articleNoteDao())
                repository.saveNote(ArticleNote(7, "First"))
                repository.saveNote(ArticleNote(7, "Updated"))
                repository.saveNote(ArticleNote(8, "Other"))
            } finally {
                database.close()
            }
            // When
            val reopened = createStarterDatabase(path)
            try {
                val repository = RoomNoteRepository(reopened.articleNoteDao())
                // Then
                assertEquals(ArticleNote(7, "Updated"), repository.loadNote(7))
                assertEquals(ArticleNote(8, "Other"), repository.loadNote(8))
            } finally {
                reopened.close()
            }
        } finally {
            check(files.removeItemAtPath(directory, error = null))
        }
    }

    companion object {
        private const val TEST_DIRECTORY_PREFIX = "notes-reopen-"
    }
}
