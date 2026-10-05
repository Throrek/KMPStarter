package me.kmpstarter.data.note

import androidx.sqlite.SQLiteException
import me.kmpstarter.data.note.local.ArticleNoteDao
import me.kmpstarter.data.note.local.ArticleNoteEntity
import me.kmpstarter.domain.note.ArticleNote
import me.kmpstarter.domain.note.NoteRepository
import me.kmpstarter.domain.note.NoteStorageException

class RoomNoteRepository(private val dao: ArticleNoteDao) : NoteRepository {
    override suspend fun loadNote(articleId: Int): ArticleNote? =
        try {
            dao.load(articleId)?.let { ArticleNote(it.articleId, it.text) }
        } catch (failure: SQLiteException) {
            throw NoteStorageException(failure)
        }

    override suspend fun saveNote(note: ArticleNote) {
        try {
            dao.upsert(ArticleNoteEntity(note.articleId, note.text))
        } catch (failure: SQLiteException) {
            throw NoteStorageException(failure)
        }
    }
}
