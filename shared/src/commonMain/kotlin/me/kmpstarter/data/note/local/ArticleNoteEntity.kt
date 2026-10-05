package me.kmpstarter.data.note.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ArticleNoteEntity.TABLE_NAME)
data class ArticleNoteEntity(@PrimaryKey val articleId: Int, val text: String) {
    companion object {
        const val TABLE_NAME = "article_notes"
    }
}
