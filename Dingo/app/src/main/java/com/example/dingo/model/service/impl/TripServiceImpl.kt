package com.example.dingo.model.service.impl

import android.util.Log
import com.example.dingo.model.Trip
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.TripService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.gson.*
import java.lang.reflect.Type
import com.google.firebase.firestore.util.CustomClassMapper
import kotlinx.coroutines.flow.emptyFlow


data class GeoTrip (
    val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<GeoPoint?> = emptyList<GeoPoint>(),
    var discoveredEntries: List<String> = emptyList(),
)


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
            val tripCollectionRef = firestore.collection(TripServiceImpl.TRIP_COLLECTIONS)
            Log.d("TripService", "tripCollection: $tripCollectionRef")


            val query = tripCollectionRef.whereEqualTo("userId", userId).limit(limit.toLong())
            Log.d("TripService", "query: $query")

            val ret: MutableList<Trip> = mutableListOf()

            val geoTripDeserializer = { snapshot: QueryDocumentSnapshot ->
                val id = snapshot.id
                val userId = snapshot.getString("userId") ?: ""
                val username = snapshot.getString("username") ?: ""

                val locations = snapshot.get("locations", List::class.java)
                    ?.map { location ->
                        val locationMap = location as? HashMap<String, Any> ?: hashMapOf()
                        val latitude = locationMap["latitude"] as? Double ?: 0.0
                        val longitude = locationMap["longitude"] as? Double ?: 0.0
                        LatLng(latitude, longitude)
//                        hashMapOf("location" to LatLng(latitude, longitude))
                    } ?: emptyList()

                val discoveredEntries = snapshot.get("discoveredEntries", List::class.java)?.map {entry -> entry as String} ?: emptyList()
                Trip(id, userId, username, locations, discoveredEntries)
            }



            val subscription = query.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (!snapshot.isEmpty) {
                    Log.d("TripService", "snapshot TRIPS: $snapshot")
                    Log.d("TripService", "snapshot.documents!: ${snapshot.documents}")

                    val geoTrips = snapshot.documents.map { document ->
                        val queryDocumentSnapshot = document as QueryDocumentSnapshot
                        geoTripDeserializer(queryDocumentSnapshot)
                    }
                    trySend(geoTrips.toMutableList())
                }
            }
            awaitClose { subscription.remove() }
        }
    }





    companion object {
        private const val TRIP_COLLECTIONS = "tripCollections"
    }
}