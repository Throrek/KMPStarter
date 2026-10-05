package me.kmpstarter.data.note

import android.content.Context
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import me.kmpstarter.data.local.StarterDatabase
import me.kmpstarter.data.local.createStarterDatabase
import me.kmpstarter.domain.note.ArticleNote
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [NotePersistenceTest.SDK_VERSION])
class NotePersistenceTest {
    @Test
    fun `WHEN database reopens THEN latest note is restored`() = runTest {
        // Given
        val context: Context = RuntimeEnvironment.getApplication()
        val directory = File(context.cacheDir, TEST_DIRECTORY_NAME).apply { mkdirs() }
        val path = File(directory, StarterDatabase.FILE_NAME).absolutePath
        try {
            val database = createStarterDatabase(context, path)
            try {
                val repository = RoomNoteRepository(database.articleNoteDao())
                repository.saveNote(ArticleNote(7, "First"))
                repository.saveNote(ArticleNote(7, "Updated"))
                repository.saveNote(ArticleNote(8, "Other"))
            } finally {
                database.close()
            }
            // When
            val reopened = createStarterDatabase(context, path)
            try {
                val repository = RoomNoteRepository(reopened.articleNoteDao())
                // Then
                assertEquals(ArticleNote(7, "Updated"), repository.loadNote(7))
                assertEquals(ArticleNote(8, "Other"), repository.loadNote(8))
            } finally {
                reopened.close()
            }
        } finally {
            directory.deleteRecursively()
        }
    }

    companion object {
        const val SDK_VERSION = 35
        private const val TEST_DIRECTORY_NAME = "notes-reopen"
    }
}
