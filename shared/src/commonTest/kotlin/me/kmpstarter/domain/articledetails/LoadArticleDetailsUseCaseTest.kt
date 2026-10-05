package me.kmpstarter.domain.articledetails

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.article.ArticleRepository
import me.kmpstarter.domain.note.ArticleNote
import me.kmpstarter.domain.note.NoteRepository
import me.kmpstarter.domain.note.NoteStorageException

@OptIn(ExperimentalCoroutinesApi::class)
class LoadArticleDetailsUseCaseTest {
    @Test
    fun `WHEN details are loaded THEN note belongs to requested article`() = runTest {
        // Given
        val calls = mutableListOf<String>()
        val articleGate = CompletableDeferred<Unit>()
        val articles = FakeArticles(calls, articleGate)
        val notes = FakeNotes(calls, ArticleNote(7, "My note"))
        val load = LoadArticleDetailsUseCase(articles, notes)
        // When
        val request = async { load(7) }
        runCurrent()
        // Then
        assertEquals(listOf("article:7"), calls)
        articleGate.complete(Unit)
        assertEquals(
            ArticleDetails(Article(7, "Kotlin", "Shared code"), ArticleNote(7, "My note")),
            request.await(),
        )
        assertEquals(listOf("article:7", "note:7"), calls)
    }

    @Test
    fun `WHEN no note exists THEN details have no note`() = runTest {
        // Given
        val calls = mutableListOf<String>()
        val load = LoadArticleDetailsUseCase(FakeArticles(calls), FakeNotes(calls))
        // When
        val details = load(7)
        // Then
        assertEquals(ArticleDetails(Article(7, "Kotlin", "Shared code"), null), details)
    }

    @Test
    fun `WHEN note read fails THEN details load fails`() = runTest {
        // Given
        val calls = mutableListOf<String>()
        val notes =
            FakeNotes(calls, failure = NoteStorageException(IllegalStateException("Disk failure")))
        val load = LoadArticleDetailsUseCase(FakeArticles(calls), notes)
        // When
        assertFailsWith<NoteStorageException> { load(7) }
        // Then
        assertEquals(listOf("article:7", "note:7"), calls)
    }

    @Test
    fun `WHEN article ID is invalid THEN no repository is called`() = runTest {
        // Given
        val calls = mutableListOf<String>()
        val load = LoadArticleDetailsUseCase(FakeArticles(calls), FakeNotes(calls))
        // When
        for (id in listOf(0, -7)) {
            assertFailsWith<IllegalArgumentException> { load(id) }
        }
        // Then
        assertEquals(emptyList(), calls)
    }

    private class FakeArticles(
        private val calls: MutableList<String>,
        private val gate: CompletableDeferred<Unit>? = null,
    ) : ArticleRepository {
        override suspend fun loadArticles(): List<Article> = error("Unexpected list read")

        override suspend fun loadArticle(id: Int): Article {
            calls += "article:$id"
            gate?.await()
            return Article(7, "Kotlin", "Shared code")
        }
    }

    private class FakeNotes(
        private val calls: MutableList<String>,
        private val note: ArticleNote? = null,
        private val failure: NoteStorageException? = null,
    ) : NoteRepository {
        override suspend fun loadNote(articleId: Int): ArticleNote? {
            calls += "note:$articleId"
            failure?.let { throw it }
            return note
        }

        override suspend fun saveNote(note: ArticleNote): Unit = error("Unexpected write")
    }
}
