package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

data class DingoDexEntry (
    @DocumentId val id: String = "",
    val userId: String = "",
    val dingoDexId: String = "",
    val name: String = "",
    val isFauna: Boolean = true,
    var numEncounters: Int = 0,
    var location: String = "",
    var pictures: List<String> = emptyList(),
    var displayPicture: String = "",
)