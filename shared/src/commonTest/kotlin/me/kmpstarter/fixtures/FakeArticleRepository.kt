package me.kmpstarter.fixtures

import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleRepository

class FakeArticleRepository : ArticleRepository {
    var listRequests = 0
        private set

    val detailRequests = mutableListOf<Int>()
    var onLoadArticles: suspend () -> List<Article> = { listOf(Article(7, "Title", "Body")) }
    var onLoadArticle: suspend (Int) -> Article = { Article(it, "Title", "Body") }

    override suspend fun loadArticles(): List<Article> {
        listRequests++
        return onLoadArticles()
    }

    override suspend fun loadArticle(id: Int): Article {
        detailRequests += id
        return onLoadArticle(id)
    }
}
