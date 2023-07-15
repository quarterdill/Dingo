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
import com.google.android.gms.maps.model.LatLng



@HiltViewModel
class TripViewModel
@Inject
constructor(
    private val userService: UserService,
    private val tripService: TripService,
) : ViewModel() {

    private val locationListLiveData = MutableLiveData<List<Location>>()

    fun locationTrackingStopped(locationList: MutableList<LatLng>) {
        Log.d("view Model locationList", locationList.toString())
    }
    fun getLocationFeed(userId: String): MutableLiveData<List<Location>> {
        return locationListLiveData
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

    fun makeDummyTrips() {
        viewModelScope.launch {
            val user = userService.getUserByEmail("e2shang@uwaterloo.ca")
            if (user != null) {
                val trip = tripService.createTrip(
                    user.id,
                    user.username,
                    emptyList(),
                    emptyList(),
                )
                println("successfully posted a trip")
            }
        }

        viewModelScope.launch {
            val user = userService.getUserByEmail("e2shang@uwaterloo.ca")
            if (user != null) {
                val trip = tripService.createTrip(
                    user.id,
                    user.username,
                    emptyList(),
                    emptyList(),
                )
                println("successfully posted another trip!")
            }
        }
    }

}