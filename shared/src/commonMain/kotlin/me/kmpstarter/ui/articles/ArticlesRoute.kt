package me.kmpstarter.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.kmpstarter.di.StarterGraph

@Composable
fun ArticlesRoute(graph: StarterGraph, onArticleClick: (Int) -> Unit) {
    val viewModel = viewModel { graph.createArticlesViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    ArticlesScreen(state, viewModel::onRetry, onArticleClick)
}
