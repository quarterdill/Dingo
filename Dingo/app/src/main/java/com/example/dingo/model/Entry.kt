package com.example.dingo.model

import java.time.LocalDateTime

enum class EntryStatus {
    NOT_OBTAINED, SHOW_DEFAULT_PICTURE, SHOW_TAKEN_PICTURE
}

data class Entry (
    var location: Location,
    var defaultPictureUrl: String,
    var takenPictureUrl: String,
    var entryStatus: EntryStatus = EntryStatus.NOT_OBTAINED,
)