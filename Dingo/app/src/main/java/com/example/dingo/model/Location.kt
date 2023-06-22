package com.example.dingo.model

import com.google.firebase.firestore.GeoPoint
import java.time.LocalDateTime


data class Location (
    var geoPoint: GeoPoint,
    var time: LocalDateTime,
)