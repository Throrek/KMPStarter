package me.kmpstarter.data.article.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ArticleRemoteDataSource(private val client: HttpClient) {
    suspend fun loadArticles(): List<ArticleDto> = client.get(ARTICLES_PATH).body()

    suspend fun loadArticle(id: Int): ArticleDto = client.get("$ARTICLES_PATH/$id").body()

    companion object {
        private const val ARTICLES_PATH = "/posts"
    }
}
