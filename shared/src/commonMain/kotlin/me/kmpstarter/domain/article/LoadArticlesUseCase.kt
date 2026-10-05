package me.kmpstarter.domain.article

import dev.zacsweers.metro.Inject

@Inject
class LoadArticlesUseCase(private val repository: ArticleRepository) {
    suspend operator fun invoke(): List<Article> = repository.loadArticles()
}
