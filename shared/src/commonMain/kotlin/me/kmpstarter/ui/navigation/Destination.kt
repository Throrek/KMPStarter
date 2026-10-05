package me.kmpstarter.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object Articles

@Serializable data class ArticleDetails(val articleId: Int)
