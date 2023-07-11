package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

data class Trip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<Location> = emptyList(),
    var discoveredEntries: List<String> = emptyList(),
)

val TripComparator = Comparator { post1: Post, post2: Post ->
    post2.timestamp.compareTo(post1.timestamp)
}

