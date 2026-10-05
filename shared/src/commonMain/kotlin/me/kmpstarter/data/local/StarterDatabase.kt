package me.kmpstarter.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.kmpstarter.data.note.local.ArticleNoteDao
import me.kmpstarter.data.note.local.ArticleNoteEntity

@Database(
    entities = [ArticleNoteEntity::class],
    version = StarterDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
@ConstructedBy(StarterDatabaseConstructor::class)
abstract class StarterDatabase : RoomDatabase() {
    abstract fun articleNoteDao(): ArticleNoteDao

    companion object {
        const val FILE_NAME = "starter.db"
        const val SCHEMA_VERSION = 1
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object StarterDatabaseConstructor : RoomDatabaseConstructor<StarterDatabase> {
    override fun initialize(): StarterDatabase
}

internal fun buildStarterDatabase(builder: RoomDatabase.Builder<StarterDatabase>): StarterDatabase =
    builder.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
