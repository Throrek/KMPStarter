package me.kmpstarter.data.local

import android.content.Context
import androidx.room.Room

fun createStarterDatabase(context: Context, filePath: String? = null): StarterDatabase {
    val applicationContext = context.applicationContext
    val path =
        filePath ?: applicationContext.getDatabasePath(StarterDatabase.FILE_NAME).absolutePath
    return buildStarterDatabase(
        Room.databaseBuilder<StarterDatabase>(
            context = applicationContext,
            name = path,
            factory = StarterDatabaseConstructor::initialize,
        )
    )
}
