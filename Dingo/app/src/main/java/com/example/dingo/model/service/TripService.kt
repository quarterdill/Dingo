package com.example.dingo.model.service

import com.example.dingo.model.Trip
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
interface TripService {
    // returns post id
    suspend fun createTrip(
        trip: Trip
//        userId: String,
//        username: String,
//        locations: List<LatLng>,
//        discoveredEntries: List<String>,
//        startTime: Timestamp = Timestamp.now(),
//        endTime: Timestamp = Timestamp.now(),
//        timestamp : Timestamp = Timestamp.now(),
//        title : String = "Your Trip"
    ): String

    suspend fun getTrip(tripId: String): Trip?

    suspend fun deleteTrip(postId: String)

    suspend fun getTripFeed(userId: String, limit: Int): Flow<MutableList<Trip>?>

}