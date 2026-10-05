package me.kmpstarter.ui.articledetails

import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleReadFailure

sealed interface ArticleDetailsState {
    data object Loading : ArticleDetailsState

    data class Ready(
        val article: Article,
        val draft: String,
        val saveStatus: NoteSaveStatus = NoteSaveStatus.Idle,
    ) : ArticleDetailsState

    data class Failed(val error: ArticleDetailsFailure) : ArticleDetailsState
}

sealed interface ArticleDetailsFailure {
    data class Article(val reason: ArticleReadFailure) : ArticleDetailsFailure

    data object NoteStorage : ArticleDetailsFailure

    data object InvalidArticleId : ArticleDetailsFailure
}

sealed interface NoteSaveStatus {
    data object Idle : NoteSaveStatus

    data object Saving : NoteSaveStatus

    data object Saved : NoteSaveStatus

    data object Invalid : NoteSaveStatus

    data object Failed : NoteSaveStatus
}
