package me.kmpstarter.data.article.network

import kotlinx.serialization.Serializable

@Serializable data class ArticleDto(val id: Int, val title: String, val body: String)
