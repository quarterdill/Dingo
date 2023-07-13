package com.example.dingo.trips

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

private const val CHANNEL_ID = "my_channel_id"
private const val CHANNEL_NAME = "My Channel"
private const val CHANNEL_DESCRIPTION = "This is my notification channel"

class LocationTrackingService : LifecycleService() {

    private val notificationId = 1
    private var isTracking by mutableStateOf(false)
    private val locationList = mutableListOf<Location>()


    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        try {
            startForeground(notificationId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isTracking = true
        startLocationTracking()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isTracking = false
        stopLocationTracking()
    }
    private fun stopLocationTracking() {
        // Stop location tracking
        // ...

        // Return the list of locations
        val intent = Intent(ACTION_LOCATION_TRACKING_STOPPED).apply {
            putExtra(EXTRA_LOCATION_LIST, locationList.toTypedArray())
        }
        sendBroadcast(intent)
    }

    private fun startLocationTracking() {
        lifecycleScope.launch {
            while (isTracking) {
                val location = getCurrentLocation()
                locationList.add(location)
                Log.d("asdfadf", location.toString())
//                println(location)

                // Process the location data as needed
                // For example, update a mutable state variable with the latest location
                // locationLiveData.value = location
                delay(1000) // Delay between location updates
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation():  Location {
        // Use the appropriate location provider to get the current location
        // For example, use FusedLocationProviderClient in Android
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this@LocationTrackingService)
            val locationResult = fusedLocationClient.lastLocation.await()
         return locationResult
    }

    private fun createNotification(): Notification {
        // Create a notification for the foreground service
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking your location")
//            .setSmallIcon(R.drawable.ic_notification)
            .build()

        return notification
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_LOCATION_TRACKING_STOPPED = "com.example.dingo.trips.ACTION_LOCATION_TRACKING_STOPPED"
        const val EXTRA_LOCATION_LIST = "com.example.dingo.trips.EXTRA_LOCATION_LIST"
    }
}