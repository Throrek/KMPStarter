package me.kmpstarter.domain.article

data class Article(val id: Int, val title: String, val body: String) {
    companion object {
        const val MIN_ID = 1
    }
}
