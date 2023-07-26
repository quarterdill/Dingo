package com.example.dingo.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.Trip
import com.example.dingo.model.service.TripService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.service.impl.TripServiceImpl
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

@HiltViewModel
class TripViewModel
@Inject
constructor(
    private val userService: UserService,
    private val tripService: TripService,
) : ViewModel() {

    private val locationListLiveData = MutableLiveData<List<Location>>()
    val discoveredEntries = mutableListOf<String>()
    val picturePaths = mutableListOf<String>()



    fun locationTrackingStopped(locationList: MutableList<LatLng>) : List<LatLng> {

        Log.d("view Model locationList", locationList.toString())
        return locationList as List<LatLng>
    }

    fun getLocationFeed(userId: String): MutableLiveData<List<Location>> {
        return locationListLiveData
    }

    fun createTrip(
        trip: Trip
    ) {
        viewModelScope.launch {

            val tripId = tripService.createTrip(
                trip
            )

            userService.addTripForUser(trip.userId, tripId)

            SessionInfo.trip = null
        }

    }

    fun discardTrip() {
        SessionInfo.trip = null
    }

    fun getTripFeed(userId: String): LiveData<MutableList<Trip>?> {
        Log.d("TripViewModel", "getTripFeed($userId)")

        return liveData(Dispatchers.IO) {
            try {
                tripService.getTripFeed(userId, 50).collect {
                    if (it != null) {
                        Log.d("TripViewModel", "tripService it: $it")

                        val trips = it
                        emit(trips)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("$e")
            }
        }
    }


    private fun getDummyTrips(): List<Trip> {
        return listOf(

        )
    }

//    fun makeDummyTrips() {
//        viewModelScope.launch {
//                val tripId = tripService.createTrip(
//                    userId=SessionInfo.currentUserID,
//                    username=SessionInfo.currentUsername,
//                    locations = dummyTrip1,
//                    discoveredEntries = emptyList())
//
//                Log.d("TripViewModel", " making a trip with tripId: $tripId")
//        }
//    }
}