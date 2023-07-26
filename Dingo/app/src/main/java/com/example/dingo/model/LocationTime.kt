package com.example.dingo.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp


data class LocationTime (
    var geoPoint: GeoPoint,
    var time: Timestamp = Timestamp.now(),
)