package com.example.dingo.model.service

import com.example.dingo.model.LocationTime
import com.example.dingo.model.Trip
import kotlinx.coroutines.flow.Flow
interface TripService {
    // returns post id
    suspend fun createTrip(
        userId: String,
        username: String,
        locations: List<LocationTime>,
        discoveredEntries: List<String>,
    ): String

    suspend fun getTrip(tripId: String): Trip?

    suspend fun deleteTrip(postId: String)

    suspend fun getTripFeed(userId: String, limit: Int): Flow<MutableList<Trip>?>

}