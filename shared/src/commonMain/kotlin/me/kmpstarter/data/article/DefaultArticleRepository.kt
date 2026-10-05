package me.kmpstarter.data.article

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import me.kmpstarter.data.article.network.ArticleDto
import me.kmpstarter.data.article.network.ArticleRemoteDataSource
import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleReadException
import me.kmpstarter.domain.article.ArticleReadFailure
import me.kmpstarter.domain.article.ArticleRepository

class DefaultArticleRepository(private val remote: ArticleRemoteDataSource) : ArticleRepository {
    override suspend fun loadArticles(): List<Article> = read {
        val articles = remote.loadArticles().map { it.toArticle() }
        if (articles.map { it.id }.distinct().size != articles.size) {
            throw ArticleReadException(ArticleReadFailure.InvalidResponse)
        }
        articles
    }

    override suspend fun loadArticle(id: Int): Article =
        read(details = true) {
            val article = remote.loadArticle(id).toArticle()
            if (article.id != id) {
                throw ArticleReadException(ArticleReadFailure.InvalidResponse)
            }
            article
        }

    private fun ArticleDto.toArticle(): Article {
        if (id < Article.MIN_ID || title.isBlank()) {
            throw ArticleReadException(ArticleReadFailure.InvalidResponse)
        }
        return Article(id, title, body)
    }

    private suspend fun <T> read(details: Boolean = false, block: suspend () -> T): T =
        try {
            block()
        } catch (failure: Exception) {
            val reason = failure.readFailure(details) ?: throw failure
            throw ArticleReadException(reason, failure)
        }

    private fun Exception.readFailure(details: Boolean): ArticleReadFailure? =
        when (this) {
            is CancellationException -> null

            is HttpRequestTimeoutException,
            is ConnectTimeoutException,
            is SocketTimeoutException -> ArticleReadFailure.Timeout

            is ResponseException ->
                if (details && response.status == HttpStatusCode.NotFound) {
                    ArticleReadFailure.NotFound
                } else {
                    ArticleReadFailure.Http
                }

            is JsonConvertException,
            is NoTransformationFoundException -> ArticleReadFailure.InvalidResponse

            is IOException -> ArticleReadFailure.Connection

            else -> null
        }
}
