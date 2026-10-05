package me.kmpstarter.data.local

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun createStarterDatabase(filePath: String? = null): StarterDatabase {
    val path = filePath ?: defaultDatabasePath()
    return buildStarterDatabase(
        Room.databaseBuilder<StarterDatabase>(
            name = path,
            factory = StarterDatabaseConstructor::initialize,
        )
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun defaultDatabasePath(): String {
    val directory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ) ?: error("Application Support directory is unavailable")
    return requireNotNull(directory.path) + "/" + StarterDatabase.FILE_NAME
}
