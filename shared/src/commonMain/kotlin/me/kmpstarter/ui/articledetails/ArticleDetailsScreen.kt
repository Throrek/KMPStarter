package me.kmpstarter.ui.articledetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.kmpstarter.resources.*
import me.kmpstarter.ui.components.ScreenError
import me.kmpstarter.ui.components.ScreenLoading
import me.kmpstarter.ui.components.ScreenScaffold
import me.kmpstarter.ui.components.articleFailureMessage
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArticleDetailsScreen(
    state: ArticleDetailsState,
    onRetry: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val saving = (state as? ArticleDetailsState.Ready)?.saveStatus == NoteSaveStatus.Saving
    ScreenScaffold(
        title = stringResource(Res.string.details_title),
        onBack = onBack,
        backEnabled = !saving,
        contentModifier = Modifier.imePadding(),
    ) {
        when (state) {
            ArticleDetailsState.Loading -> ScreenLoading()
            is ArticleDetailsState.Failed -> {
                ScreenError(
                    message =
                        when (val error = state.error) {
                            is ArticleDetailsFailure.Article -> articleFailureMessage(error.reason)
                            ArticleDetailsFailure.NoteStorage ->
                                stringResource(Res.string.error_note_read)
                            ArticleDetailsFailure.InvalidArticleId ->
                                stringResource(Res.string.error_article_id)
                        },
                    onRetry =
                        onRetry.takeUnless {
                            state.error == ArticleDetailsFailure.InvalidArticleId
                        },
                )
            }
            is ArticleDetailsState.Ready -> {
                Column(
                    modifier =
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(state.article.title, style = MaterialTheme.typography.headlineSmall)
                    Text(state.article.body)
                    OutlinedTextField(
                        value = state.draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.note_label)) },
                        enabled = !saving,
                        minLines = 4,
                        isError = state.saveStatus == NoteSaveStatus.Invalid,
                        supportingText = {
                            if (state.saveStatus == NoteSaveStatus.Invalid) {
                                Text(stringResource(Res.string.note_required))
                            }
                        },
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = onSave, enabled = !saving) {
                            Text(stringResource(Res.string.save))
                        }
                        if (saving) {
                            Row(
                                modifier =
                                    Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text(stringResource(Res.string.saving))
                            }
                        }
                    }
                    Column(modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                        when (state.saveStatus) {
                            NoteSaveStatus.Saved -> Text(stringResource(Res.string.saved))
                            NoteSaveStatus.Failed ->
                                Text(
                                    stringResource(Res.string.error_note_save),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            NoteSaveStatus.Saving,
                            NoteSaveStatus.Idle,
                            NoteSaveStatus.Invalid -> Unit
                        }
                    }
                }
            }
        }
    }
}
