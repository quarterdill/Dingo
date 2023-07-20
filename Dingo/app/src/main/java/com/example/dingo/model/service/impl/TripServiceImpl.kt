package com.example.dingo.model.service.impl

import android.util.Log
import com.example.dingo.model.Trip
import com.example.dingo.model.TripDeserializer
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.TripService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.gson.*

import java.lang.reflect.Type

class TripDeserializer : JsonDeserializer<Trip> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Trip {
        val jsonObject = json?.asJsonObject
        val id = jsonObject?.get("id")?.asString ?: ""
        val userId = jsonObject?.get("userId")?.asString ?: ""
        val username = jsonObject?.get("username")?.asString ?: ""
        val discoveredEntries = jsonObject?.get("discoveredEntries")?.asJsonArray?.map { it.asString } ?: emptyList()

        val locations = jsonObject?.get("locations")?.asJsonArray?.map {
            val geoPoint = it.asJsonObject
            val latitude = geoPoint.get("latitude").asDouble
            val longitude = geoPoint.get("longitude").asDouble
            LatLng(latitude, longitude)
        } ?: emptyList()

        return Trip(id, userId, username, locations, discoveredEntries)
    }
}

class GeoPointDeserializer : JsonDeserializer<LatLng> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LatLng {
        val jsonObject = json?.asJsonObject
        val latitude = jsonObject?.get("latitude")?.asDouble ?: 0.0
        val longitude = jsonObject?.get("longitude")?.asDouble ?: 0.0
        return LatLng(latitude, longitude)
    }
}

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


    fun convertGeoPointsToLatLngs(geoPoints: List<GeoPoint>): List<LatLng> {
        val latLngs = mutableListOf<LatLng>()

        for (geoPoint in geoPoints) {

            val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
            latLngs.add(latLng)
        }

        return latLngs as List<LatLng>
    }

    override suspend fun getTripFeed(userId: String, limit: Int): Flow<MutableList<Trip>?> {
        Log.d("TripService", "getTripFeed($userId, $limit)")

        val gson = GsonBuilder()
            .registerTypeAdapter(GeoPoint::class.java, GeoPointDeserializer())
            .create()

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
                    Log.d("TripService", "snapshot TRIPS: $snapshot")
                    Log.d("TripService", "snapshot.documents!: ${snapshot.documents}")


//                    DEBUG THIS

                    val gson = GsonBuilder()
                        .registerTypeAdapter(LatLng::class.java, GeoPointDeserializer())
                        .create()

                    val gson2: Gson = GsonBuilder()
                        .registerTypeAdapter(Trip::class.java, TripDeserializer())
                        .create()

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