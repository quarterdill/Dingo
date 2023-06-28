package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

data class Trip (
    @DocumentId val id: String = "",
    var locations: List<Location>,
    var discoveredEntries: List<String>
)