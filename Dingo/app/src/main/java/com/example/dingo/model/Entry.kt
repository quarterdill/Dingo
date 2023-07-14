package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

enum class EntryStatus {
    NOT_OBTAINED, SHOW_DEFAULT_PICTURE, SHOW_TAKEN_PICTURE
}

data class Entry (
    @DocumentId val id: String = "",
    var location: LocationTime,
    var defaultPictureUrl: String = "",
    var takenPictureUrl: String = "",
    var entryStatus: EntryStatus = EntryStatus.NOT_OBTAINED,
)