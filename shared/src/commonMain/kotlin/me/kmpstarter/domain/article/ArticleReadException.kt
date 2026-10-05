package me.kmpstarter.domain.article

enum class ArticleReadFailure {
    Connection,
    Timeout,
    Http,
    InvalidResponse,
    NotFound,
}

class ArticleReadException(val reason: ArticleReadFailure, cause: Throwable? = null) :
    Exception("Article read failed: $reason", cause)
