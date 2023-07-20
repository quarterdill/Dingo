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
    //        val user = userService.getUserByEmail("e2shang@uwaterloo.ca")
//
//        Log.d("view Model locationTrackingStopped user", user.toString())
//
//        if (user != null) {
//            Log.d("tripViewModel calling makeTrip with user", user.toString())
//            makeTrip(userId=user.id, username=user.username, locations = locationList as List<LatLng>)
//        } else {
//            Log.d("tripViewModel calling makeTrip without", "u")
//
//        }

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
        return liveData(Dispatchers.IO) {
            try {
                tripService.getTripFeed(userId, 50).collect {
                    if (it != null) {
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


    private fun getDummyTrips(): List<Triple<String, String, UserType>> {
        return listOf(
            Triple("Dylan Xiao", "d29xiao@uwaterloo.ca", UserType.STUDENT),
            Triple("Eric Shang", "e2shang@uwaterloo.ca", UserType.STUDENT),
            Triple("Austin Lin", "a62lin@uwaterloo.ca", UserType.STUDENT),
            Triple("Simhon Chourasia", "s2choura@uwaterloo.ca", UserType.STUDENT),
            Triple("Hitanshu Dalwadi", "hmdalwad@uwaterloo.ca", UserType.STUDENT),
            Triple("Eden Chan", "e223chan@uwaterloo.ca", UserType.STUDENT),
            Triple("Philip Chen", "p242chen@uwaterloo.ca", UserType.TEACHER)
        )
    }

    fun makeDummyTrips(locations: List<LatLng>) {
        viewModelScope.launch {
            val user = userService.getUserByEmail("e2shang@uwaterloo.ca")
            if (user != null) {
                val tripId = tripService.createTrip(
                    user.id,
                    user.username,
                    locations = locations,
                    emptyList(),
                )
                Log.d("TripViewModel making a trip with tripId:", tripId)

                println("successfully posted a trip")
            } else {
                Log.d("TripViewModel", "fail to make trip since no user")

            }
        }

    }

}