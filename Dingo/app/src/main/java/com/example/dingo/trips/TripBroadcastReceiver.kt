package com.example.dingo.trips

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.dingo.model.Location
import com.example.dingo.model.Trip

class LocationTrackingStoppedReceiver(private val onLocationTrackingStopped: (Array<Location>) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationTrackingService.ACTION_LOCATION_TRACKING_STOPPED) {
            val locationArray = intent.getParcelableArrayExtra(LocationTrackingService.EXTRA_LOCATION_LIST) as Array<Location>?
            if (locationArray != null) {
                onLocationTrackingStopped(locationArray)
            }
        }
    }
}

