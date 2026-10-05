package me.kmpstarter.ui.articles

import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleReadFailure

sealed interface ArticlesState {
    data object Loading : ArticlesState

    data class Content(val articles: List<Article>) : ArticlesState

    data class Failed(val error: ArticleReadFailure) : ArticlesState
}
