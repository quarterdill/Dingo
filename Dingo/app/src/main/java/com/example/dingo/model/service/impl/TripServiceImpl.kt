package com.example.dingo.model.service.impl

import android.util.Log
import com.example.dingo.model.Classroom
import com.example.dingo.model.GeoTrip
import com.example.dingo.model.LocationTime
import com.example.dingo.model.Trip
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.TripService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TripServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) : TripService {

    override suspend fun createTrip(
        userId: String,
        username: String,
        locations: List<LatLng>,
        discoveredEntries: List<String>

    ): String {
        var trip: Trip = Trip()
        trip.userId = userId
        trip.username = username
        trip.locations = locations

//        Default to empty lists for now
//        TODO("Fetch locations using Android API and discoveredEntries from Scanner Service")
//        trip.locations = locations
//        trip.discoveredEntries = discoveredEntries
        var tripId = ""

        firestore.collection(TRIP_COLLECTIONS)
            .add(trip)
            .addOnSuccessListener {
                tripId = it.id
            }
            .addOnFailureListener {e ->
                println("Error adding Trip document: $e")
            }
            .await()

        if (tripId.isEmpty()) {
            println("empty trip id for trip; early exiting")
        }

        return tripId
    }

    override suspend fun getTrip(tripId: String): Trip? {
        return firestore.collection(TRIP_COLLECTIONS)
            .document(tripId)
            .get()
            .await()
            .toObject(Trip::class.java)
    }

    override suspend fun deleteTrip(tripId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getTripFeed(userId: String, limit: Int): Flow<MutableList<Trip>?> {
        Log.d("TripService", "getTripFeed($userId, $limit)")

        return callbackFlow {
            // Get a reference to the trips collection and create a query to filter by userId
            val tripCollection = firestore.collection(TripServiceImpl.TRIP_COLLECTIONS)
            Log.d("TripService", "tripCollection: $tripCollection")

            val query = tripCollection.whereEqualTo("userId", userId).limit(limit.toLong())
            Log.d("TripService", "query: $query")

            val subscription = query.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (!snapshot.isEmpty) {
                    Log.d("TripService", "snapshot: $snapshot")
                    Log.d("TripService", "snapshot.documents: ${snapshot.documents}")

                    val trips = snapshot.toObjects(Trip::class.java)
                    Log.d("TripService", "trips: $trips")

                    trySend(trips)
                }
            }
            awaitClose { subscription.remove() }
        }
    }





    companion object {
        private const val TRIP_COLLECTIONS = "tripCollections"
    }
}