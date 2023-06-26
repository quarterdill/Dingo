package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

data class DingoDexEntry (
    @DocumentId val id: String = "",
    var location: String,
    var picture: List<String>,
    // Not too sure about obtained default showon
)