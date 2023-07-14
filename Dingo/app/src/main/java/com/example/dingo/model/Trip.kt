package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

data class Trip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<LocationTime> = emptyList(),
    var discoveredEntries: List<String> = emptyList(),
)


