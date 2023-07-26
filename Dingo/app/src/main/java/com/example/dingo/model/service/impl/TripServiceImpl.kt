package com.example.dingo.model.service.impl

import android.util.Log
import com.example.dingo.model.GeoTrip
import com.example.dingo.model.Trip
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.TripService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject




class TripServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) : TripService {

    override suspend fun createTrip(
        userId: String,
        username: String,
        locations: List<LatLng>,
        discoveredEntries: List<String>,
        startTime : Timestamp,
        endTime: Timestamp,
        timestamp:Timestamp,
        title:String

    ): String {
        var trip: Trip = Trip()
        trip.userId = userId
        trip.username = username
        trip.locations = locations
        trip.startTime = startTime
        trip.endTime = Timestamp.now()
        trip.timestamp = Timestamp.now()
        trip.title = title

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
        Log.d("SocialFeedScreen","getTrip($tripId)")

        var geotrip =  listOf(firestore.collection(TRIP_COLLECTIONS)
            .document(tripId)
            .get()
            .await()
            .toObject(GeoTrip::class.java))

        var trip = convertGeoTripsToTrips(geotrip as List<GeoTrip>)
        var ret = trip.firstOrNull()
        Log.d("SocialFeedScreen","getTrip($tripId) = ${ret}")

        return ret

    }

    override suspend fun deleteTrip(tripId: String) {
        TODO("Not yet implemented")
    }



    fun convertGeoTripsToTrips(geoTrips: List<GeoTrip>): List<Trip> {
        val trips = mutableListOf<Trip>()
        for (geoTrip in geoTrips) {

            Log.d("SocialFeedScreen","final ret geotrip = ${geoTrip}")

            val trip = Trip(
                id = geoTrip.id,
                userId = geoTrip.userId,
                username = geoTrip.username,
                locations = geoTrip.locations.map { location ->
                    LatLng(location["latitude"] as Double, location["longitude"] as Double)
                },
                discoveredEntries = geoTrip.discoveredEntries,
                startTime= geoTrip.startTime,
                endTime = geoTrip.endTime,
                timestamp= geoTrip.timestamp,
                title = geoTrip.title
            )
            trips.add(trip)
        }
        return trips
    }


    override suspend fun getTripFeed(userId: String, limit: Int): Flow<MutableList<Trip>?> {
        Log.d("TripService", "getTripFeed($userId, $limit)")

        return callbackFlow {
            // Get a reference to the trips collection and create a query to filter by userId
            val tripCollectionRef = firestore.collection(TripServiceImpl.TRIP_COLLECTIONS)
            Log.d("TripService", "tripCollection: $tripCollectionRef")


            val query = tripCollectionRef.whereEqualTo("userId", userId).limit(limit.toLong())
            Log.d("TripService", "query: $query")


            val subscription = query.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (!snapshot.isEmpty) {
                    Log.d("TripService", "snapshot TRIPS: $snapshot")
                    Log.d("TripService", "snapshot.documents!: ${snapshot.documents}")

                    val geoTripList : List<GeoTrip> = snapshot.toObjects(GeoTrip::class.java)
                    val trips = convertGeoTripsToTrips(geoTripList)

                    Log.d("TripService", "toObjects!: ${geoTripList}")
                    Log.d("TripService", "toObjects after conversion for trips!: ${trips}")


                    trySend(trips as MutableList<Trip>)
                }

            }
            awaitClose { subscription.remove() }
        }
    }


    companion object {
        private const val TRIP_COLLECTIONS = "tripCollections"
    }
}


