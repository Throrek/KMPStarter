package me.kmpstarter.domain.article

interface ArticleRepository {
    suspend fun loadArticles(): List<Article>

    suspend fun loadArticle(id: Int): Article
}
