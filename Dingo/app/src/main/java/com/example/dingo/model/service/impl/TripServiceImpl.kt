package com.example.dingo.model.service.impl

import com.example.dingo.model.Classroom
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
        // TODO("get (limit) most recent posts for feed")

        return callbackFlow {
            // get the Trip collection belonging to userId
            val tripCollection = firestore.collection(TripServiceImpl.TRIP_COLLECTIONS)
                .document(userId)
            val subscription = tripCollection.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot!!.exists()) {
                    var trips = snapshot.toObject(Classroom::class.java)
                    var limiter = 0
                    var ret: MutableList<Trip> = mutableListOf<Trip>()
                    if (trips != null) {
                        for (tripId in trips.posts) {
                            if (limiter > limit) {
                                break
                            }
                            limiter++

                            var trip: Trip? = null

                            runBlocking {
                                trip = firestore.collection(TripServiceImpl.TRIP_COLLECTIONS)
                                    .document(tripId)
                                    .get()
                                    .await()
                                    .toObject(Trip::class.java)
                            }

                            if (trip != null) {
                                ret.add(trip!!)
                            } else {
                                println("Trip $tripId not found!?")
                            }
                        }
                    }
                    trySend(ret)
                }
            }
            awaitClose { subscription.remove() }
        }
    }




    companion object {
        private const val TRIP_COLLECTIONS = "tripCollections"
    }
}