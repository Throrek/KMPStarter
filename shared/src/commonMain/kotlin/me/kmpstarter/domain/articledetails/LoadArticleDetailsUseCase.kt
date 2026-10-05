package me.kmpstarter.domain.articledetails

import dev.zacsweers.metro.Inject
import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleRepository
import me.kmpstarter.domain.note.NoteRepository

@Inject
class LoadArticleDetailsUseCase(
    private val articles: ArticleRepository,
    private val notes: NoteRepository,
) {
    suspend operator fun invoke(articleId: Int): ArticleDetails {
        require(articleId >= Article.MIN_ID) { "Article ID must be positive" }
        val article = articles.loadArticle(articleId)
        val note = notes.loadNote(articleId)
        return ArticleDetails(article, note)
    }
}
