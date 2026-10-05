package me.kmpstarter.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import me.kmpstarter.data.article.DefaultArticleRepository
import me.kmpstarter.data.article.network.ArticleRemoteDataSource
import me.kmpstarter.data.local.StarterDatabase
import me.kmpstarter.data.note.RoomNoteRepository
import me.kmpstarter.domain.article.ArticleRepository
import me.kmpstarter.domain.note.NoteRepository
import me.kmpstarter.ui.articledetails.ArticleDetailsViewModel
import me.kmpstarter.ui.articles.ArticlesViewModel

@DependencyGraph
interface StarterGraph {
    val articleDetailsViewModelFactory: ArticleDetailsViewModel.Factory

    fun createArticlesViewModel(): ArticlesViewModel

    @Provides
    fun provideArticleRepository(client: HttpClient): ArticleRepository =
        DefaultArticleRepository(ArticleRemoteDataSource(client))

    @Provides
    fun provideNoteRepository(database: StarterDatabase): NoteRepository =
        RoomNoteRepository(database.articleNoteDao())

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides database: StarterDatabase, @Provides client: HttpClient): StarterGraph
    }
}

fun createStarterGraph(database: StarterDatabase, client: HttpClient): StarterGraph =
    createGraphFactory<StarterGraph.Factory>().create(database, client)
