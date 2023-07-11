package com.example.dingo.model.service

import com.example.dingo.model.Location
import com.example.dingo.model.Trip
import kotlinx.coroutines.flow.Flow
interface TripService {
    // returns post id
    suspend fun createTrip(
        userId: String,
        username: String,
        locations: List<Location>,
        discoveredEntries: List<String>,
    ): String

    suspend fun getTrip(tripId: String): Trip?

    suspend fun deleteTrip(postId: String)

}