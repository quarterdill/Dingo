package com.example.dingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class DingoDexEntry (
    @DocumentId val id: String = "",
    val userId: String = "",
    val dingoDexId: Int = -1,
    val name: String = "",
    val isFauna: Boolean = true,
    var numEncounters: Int = 0,
    var location: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var pictures: MutableList<String> = mutableListOf(),
    var displayPicture: String = "",
    var scientificName: String = "",
)