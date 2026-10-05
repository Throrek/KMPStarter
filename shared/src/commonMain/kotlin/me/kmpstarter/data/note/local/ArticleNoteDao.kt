package me.kmpstarter.data.note.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ArticleNoteDao {
    @Query("SELECT * FROM ${ArticleNoteEntity.TABLE_NAME} WHERE articleId = :articleId")
    suspend fun load(articleId: Int): ArticleNoteEntity?

    @Upsert suspend fun upsert(note: ArticleNoteEntity)
}
