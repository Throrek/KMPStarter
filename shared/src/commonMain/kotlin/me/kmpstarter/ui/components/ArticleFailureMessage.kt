package me.kmpstarter.ui.components

import androidx.compose.runtime.Composable
import me.kmpstarter.domain.article.ArticleReadFailure
import me.kmpstarter.resources.Res
import me.kmpstarter.resources.error_connection
import me.kmpstarter.resources.error_http
import me.kmpstarter.resources.error_not_found
import me.kmpstarter.resources.error_response
import me.kmpstarter.resources.error_timeout
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun articleFailureMessage(reason: ArticleReadFailure): String =
    stringResource(
        when (reason) {
            ArticleReadFailure.Connection -> Res.string.error_connection
            ArticleReadFailure.Timeout -> Res.string.error_timeout
            ArticleReadFailure.Http -> Res.string.error_http
            ArticleReadFailure.InvalidResponse -> Res.string.error_response
            ArticleReadFailure.NotFound -> Res.string.error_not_found
        }
    )
