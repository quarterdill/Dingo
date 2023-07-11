package com.example.dingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Trip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var locations: List<Location>,
    var discoveredEntries: List<String>
)

val TripComparator = Comparator { trip1: Trip, trip2: Trip ->
    trip2.timestamp.compareTo(trip1.timestamp)
}

