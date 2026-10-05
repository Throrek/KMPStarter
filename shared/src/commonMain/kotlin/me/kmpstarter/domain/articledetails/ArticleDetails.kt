package me.kmpstarter.domain.articledetails

import me.kmpstarter.domain.article.Article
import me.kmpstarter.domain.note.ArticleNote

data class ArticleDetails(val article: Article, val note: ArticleNote?)
