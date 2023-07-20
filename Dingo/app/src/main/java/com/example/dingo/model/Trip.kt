package com.example.dingo.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId

data class Trip (
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<LatLng> = emptyList<LatLng>(),
    var discoveredEntries: List<String> = emptyList(),
)


