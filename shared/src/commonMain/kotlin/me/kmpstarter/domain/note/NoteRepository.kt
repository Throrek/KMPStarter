package me.kmpstarter.domain.note

interface NoteRepository {
    suspend fun loadNote(articleId: Int): ArticleNote?

    suspend fun saveNote(note: ArticleNote)
}
