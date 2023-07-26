package com.example.dingo.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint


data class Trip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<LatLng> = emptyList<LatLng>(),
    var discoveredEntries: MutableList<String> = mutableListOf(),
    var startTime: Timestamp = Timestamp.now(),
    var endTime: Timestamp = Timestamp.now(),
    var timestamp : Timestamp = Timestamp.now(),
    var title : String = "Your Trip",
    var picturePaths: MutableList<String> = mutableListOf(),
)


// This is for converting the Firestore representation of Trips with GeoPoint to the application version of Trips with LatLong
data class GeoTrip (
    val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<HashMap<String, Any>> =emptyList(),
    var discoveredEntries: List<String> = emptyList(),
    var startTime: Timestamp = Timestamp.now(),
    var endTime: Timestamp = Timestamp.now(),
    var timestamp : Timestamp = Timestamp.now(),
    var title : String = "Your Trip",
    var picturePaths: List<String> = emptyList(),

)
