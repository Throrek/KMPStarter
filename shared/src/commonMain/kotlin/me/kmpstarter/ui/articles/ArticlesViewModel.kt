package me.kmpstarter.ui.articles

import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import me.kmpstarter.domain.article.ArticleReadException
import me.kmpstarter.domain.article.ArticleReadFailure
import me.kmpstarter.domain.article.LoadArticlesUseCase
import me.kmpstarter.presentation.BaseViewModel

@Inject
class ArticlesViewModel(private val loadArticles: LoadArticlesUseCase) :
    BaseViewModel<ArticlesState>(ArticlesState.Loading) {
    init {
        load()
    }

    fun onRetry() {
        if (state.value is ArticlesState.Failed) load()
    }

    private fun load() {
        updateState { ArticlesState.Loading }
        viewModelScope.launch {
            var failure: ArticleReadFailure? = null
            val articles =
                try {
                    loadArticles()
                } catch (error: ArticleReadException) {
                    failure = error.reason
                    null
                }
            ensureActive()
            val next =
                if (articles != null) ArticlesState.Content(articles)
                else ArticlesState.Failed(checkNotNull(failure))
            updateState { next }
        }
    }
}
