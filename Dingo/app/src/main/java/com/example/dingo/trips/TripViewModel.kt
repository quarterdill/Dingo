package com.example.dingo.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.Trip
import com.example.dingo.model.UserType
import com.example.dingo.model.service.TripService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.dingo.model.User
import com.google.android.gms.maps.model.LatLng

@HiltViewModel
class TripViewModel
@Inject
constructor(
    private val userService: UserService,
    private val tripService: TripService,
) : ViewModel() {

    private val locationListLiveData = MutableLiveData<List<Location>>()




    fun locationTrackingStopped(locationList: MutableList<LatLng>) : List<LatLng> {

        Log.d("view Model locationList", locationList.toString())
        return locationList as List<LatLng>
    }

    fun getLocationFeed(userId: String): MutableLiveData<List<Location>> {
        return locationListLiveData
    }

    fun makeTrip(
        userId: String,
        username: String,
        locations: List<LatLng>
    ) {
        var discoveredEntries = emptyList<String>()
        viewModelScope.launch {

            val tripId = tripService.createTrip(
                userId,
                username,
                locations,
                discoveredEntries
            )
            Log.d("TripViewModel making a trip with tripId:", tripId)

//            classroomService.addPost(classroomId, postId)
//            userService.addClassroomPost(userId, postId)
        }

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

    fun makeDummyTrips(locations: List<LatLng>) {
        viewModelScope.launch {
            Log.d("TripViewModel", "makeDummyTrips($locations)")
            val user = userService.getUserByEmail("e2shang@uwaterloo.ca")
            if (user != null) {
                val tripId = tripService.createTrip(
                    user.id,
                    user.username,
                    locations = locations,
                    emptyList(),
                )
                Log.d("TripViewModel", " making a trip with tripId: $tripId")
            } else {
                Log.d("TripViewModel", "fail to make trip since no user")

            }
        }

    }

}