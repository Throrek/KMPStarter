package me.kmpstarter.domain.note

class NoteStorageException(cause: Throwable) : Exception("Note storage failed", cause)
