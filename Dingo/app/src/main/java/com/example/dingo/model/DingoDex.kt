package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

data class DingoDex (
    @DocumentId val id: String = "",
    var name: String = "",
    var entry: String = "",
    var defaultPicture: String = "",
    var isFauna: Boolean = true,
    var description: String = "",
    var notes: String = "",
)