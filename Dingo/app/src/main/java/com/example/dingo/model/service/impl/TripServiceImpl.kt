package com.example.dingo.model.service.impl

import android.util.Log
import com.example.dingo.model.Trip
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.TripService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


data class GeoTrip (
    val id: String = "",
    var userId: String = "",
    var username: String = "",
    var locations: List<HashMap<String, Any>> =emptyList(),
    var discoveredEntries: List<String> = emptyList(),
)


data class GeoPointList(val locations: List<HashMap<String, Any>>): ArrayList<HashMap<String, Any>>(locations)


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



            val geoTripDeserializer = { snapshot: QueryDocumentSnapshot ->
                val id = snapshot.id
                val userId = snapshot.getString("userId") ?: ""
                val username = snapshot.getString("username") ?: ""
//                snapshot.toObject()
//                val geoPointList = snapshot.get("locations", GeoPointList::class.java)
//                val locations = geoPointList?.map { location ->
//                    val latitude = location["latitude"] as? Double ?: 0.0
//                    val longitude = location["longitude"] as? Double ?: 0.0
//                    LatLng(latitude, longitude)
//                } ?: emptyList()

//                val locations = snapshot.get("locations", List::class.java)?.mapNotNull { location ->
//                    if (location is HashMap<*, *>) {
//                        val latitude = location["latitude"] as? Double ?: 0.0
//                        val longitude = location["longitude"] as? Double ?: 0.0
//                        LatLng(latitude, longitude)
//                    } else {
//                        null
//                    }
//                } ?: emptyList()

//                val locations = snapshot.get("locations", object : GenericTypeIndicator<List<HashMap<String, Any>>>() {})
//                    ?.flatMap { it.values }
//                    ?.mapNotNull { location ->
//                        if (location is HashMap<*, *>) {
//                            val latitude = location["latitude"] as? Double ?: 0.0
//                            val longitude = location["longitude"] as? Double ?: 0.0
//                            LatLng(latitude, longitude)
//                        } else {
//                            null
//                        }
//                    } ?: emptyList()

//                val locations = snapshot.get("locations", object : GenericTypeIndicator<List<HashMap<String, Any>>>() {})

//                val discoveredEntries : List<String?> = emptyList()
//                Trip(id, userId, username, locations, discoveredEntries)
            }


            val subscription = query.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (!snapshot.isEmpty) {
                    Log.d("TripService", "snapshot TRIPS: $snapshot")
                    Log.d("TripService", "snapshot.documents!: ${snapshot.documents}")

                    val ret : Any = snapshot.toObjects(GeoTrip::class.java)
                    Log.d("TripService", "toObjects!: ${ret}")

//                    val geoTrips = snapshot.documents.map { document ->
//                        val queryDocumentSnapshot = document as QueryDocumentSnapshot
//                        geoTripDeserializer(queryDocumentSnapshot)
//                    }
//                    trySend(geoTrips.toMutableList())

                    trySend(mutableListOf())
                }

            }
            awaitClose { subscription.remove() }
        }
    }





    companion object {
        private const val TRIP_COLLECTIONS = "tripCollections"
    }
}

//private fun QueryDocumentSnapshot.getLatLngList(s: String, genericTypeIndicator: GenericTypeIndicator<List<HashMap<String, Any>>>): List<LatLng?> {
//    val geoPointList : GenericTypeIndicator<List<HashMap<String, Any>>> = this.get(s) as GenericTypeIndicator<List<HashMap<String, Any>>>
//    return geoPointList?.map { location ->
//        val latitude = location?.latitude ?: 0.0
//        val longitude = location?.longitude ?: 0.0
//        LatLng(latitude, longitude)
//    } ?: emptyList()
//}

//private fun <T> GenericTypeIndicator<T>.map(function: (LatLng?) -> LatLng): List<LatLng?>? {
//    return emptyList()
//}

