package com.example.dingo.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint


data class Trip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<LatLng?> = emptyList<LatLng>(),
    var discoveredEntries: List<String?> = emptyList(),
)


data class GeoTrip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<LatLng?>? = emptyList<LatLng>(),
    var discoveredEntries: List<String> = emptyList(),
)
