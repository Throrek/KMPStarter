package me.kmpstarter.ui.articledetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.kmpstarter.di.StarterGraph

@Suppress("DEPRECATION") // Same common back dispatcher as Navigation Compose 2.9.2.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArticleDetailsRoute(graph: StarterGraph, articleId: Int, onBack: () -> Unit) {
    val viewModel = viewModel { graph.articleDetailsViewModelFactory.create(articleId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val guardedBack = {
        // Save publishes synchronously; check current state even before recomposition.
        if (
            (viewModel.state.value as? ArticleDetailsState.Ready)?.saveStatus !=
                NoteSaveStatus.Saving
        ) {
            onBack()
        }
    }
    BackHandler(onBack = guardedBack)
    ArticleDetailsScreen(
        state = state,
        onRetry = viewModel::onRetry,
        onDraftChange = viewModel::onNoteChanged,
        onSave = viewModel::onSave,
        onBack = guardedBack,
    )
}
