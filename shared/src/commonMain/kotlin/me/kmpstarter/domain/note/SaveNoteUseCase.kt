package me.kmpstarter.domain.note

import dev.zacsweers.metro.Inject
import me.kmpstarter.domain.article.Article

sealed interface SaveNoteResult {
    data class Saved(val note: ArticleNote) : SaveNoteResult

    data object InvalidText : SaveNoteResult
}

@Inject
class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(articleId: Int, text: String): SaveNoteResult {
        require(articleId >= Article.MIN_ID) { "Article ID must be positive" }
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SaveNoteResult.InvalidText
        val note = ArticleNote(articleId, trimmed)
        repository.saveNote(note)
        return SaveNoteResult.Saved(note)
    }
}
