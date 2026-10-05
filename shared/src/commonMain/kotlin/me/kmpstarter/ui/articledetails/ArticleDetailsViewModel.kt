package me.kmpstarter.ui.articledetails

import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleReadException
import me.kmpstarter.domain.articledetails.LoadArticleDetailsUseCase
import me.kmpstarter.domain.note.NoteStorageException
import me.kmpstarter.domain.note.SaveNoteResult
import me.kmpstarter.domain.note.SaveNoteUseCase
import me.kmpstarter.presentation.BaseViewModel

@AssistedInject
class ArticleDetailsViewModel(
    @Assisted private val articleId: Int,
    private val loadDetails: LoadArticleDetailsUseCase,
    private val saveNote: SaveNoteUseCase,
) : BaseViewModel<ArticleDetailsState>(ArticleDetailsState.Loading) {
    @AssistedFactory
    fun interface Factory {
        fun create(articleId: Int): ArticleDetailsViewModel
    }

    init {
        load()
    }

    fun onRetry() {
        if (state.value is ArticleDetailsState.Failed) load()
    }

    fun onNoteChanged(text: String) {
        val current = state.value as? ArticleDetailsState.Ready ?: return
        if (current.saveStatus == NoteSaveStatus.Saving) return
        updateState { current.copy(draft = text, saveStatus = NoteSaveStatus.Idle) }
    }

    fun onSave() {
        val current = state.value as? ArticleDetailsState.Ready ?: return
        if (current.saveStatus == NoteSaveStatus.Saving) return
        updateState { current.copy(saveStatus = NoteSaveStatus.Saving) }
        viewModelScope.launch {
            val result =
                try {
                    saveNote(articleId, current.draft)
                } catch (_: NoteStorageException) {
                    null
                }
            ensureActive()
            val next =
                when (result) {
                    is SaveNoteResult.Saved ->
                        current.copy(draft = result.note.text, saveStatus = NoteSaveStatus.Saved)
                    SaveNoteResult.InvalidText -> current.copy(saveStatus = NoteSaveStatus.Invalid)
                    null -> current.copy(saveStatus = NoteSaveStatus.Failed)
                }
            updateState { next }
        }
    }

    private fun load() {
        if (articleId < Article.MIN_ID) {
            updateState { ArticleDetailsState.Failed(ArticleDetailsFailure.InvalidArticleId) }
            return
        }
        updateState { ArticleDetailsState.Loading }
        viewModelScope.launch {
            var failure: ArticleDetailsFailure? = null
            val details =
                try {
                    loadDetails(articleId)
                } catch (error: ArticleReadException) {
                    failure = ArticleDetailsFailure.Article(error.reason)
                    null
                } catch (_: NoteStorageException) {
                    failure = ArticleDetailsFailure.NoteStorage
                    null
                }
            ensureActive()
            val next =
                if (details != null)
                    ArticleDetailsState.Ready(details.article, details.note?.text.orEmpty())
                else ArticleDetailsState.Failed(checkNotNull(failure))
            updateState { next }
        }
    }
}
