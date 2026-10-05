package me.kmpstarter.fixtures

import me.kmpstarter.domain.note.ArticleNote
import me.kmpstarter.domain.note.NoteRepository

class FakeNoteRepository : NoteRepository {
    val readRequests = mutableListOf<Int>()
    val writes = mutableListOf<ArticleNote>()
    var storedNote: ArticleNote? = null
        private set

    var onLoadNote: suspend (Int) -> ArticleNote? = { null }
    var beforeSave: suspend () -> Unit = {}

    override suspend fun loadNote(articleId: Int): ArticleNote? {
        readRequests += articleId
        return onLoadNote(articleId)
    }

    override suspend fun saveNote(note: ArticleNote) {
        writes += note
        beforeSave()
        storedNote = note
    }
}
