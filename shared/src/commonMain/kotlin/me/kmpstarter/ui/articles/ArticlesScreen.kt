package me.kmpstarter.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.kmpstarter.resources.*
import me.kmpstarter.ui.components.ScreenError
import me.kmpstarter.ui.components.ScreenLoading
import me.kmpstarter.ui.components.ScreenScaffold
import me.kmpstarter.ui.components.articleFailureMessage
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArticlesScreen(state: ArticlesState, onRetry: () -> Unit, onArticleClick: (Int) -> Unit) {
    ScreenScaffold(title = stringResource(Res.string.articles_title)) {
        when (state) {
            ArticlesState.Loading -> ScreenLoading()
            is ArticlesState.Failed -> ScreenError(articleFailureMessage(state.error), onRetry)
            is ArticlesState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state.articles.isEmpty()) {
                        item { Text(stringResource(Res.string.articles_empty)) }
                    }
                    items(state.articles, key = { it.id }) { article ->
                        Card(
                            onClick = { onArticleClick(article.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(article.title, style = MaterialTheme.typography.titleMedium)
                                Text(article.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
