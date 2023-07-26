package com.example.dingo.trips

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.dingo.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.cancel
import java.sql.Time
import javax.inject.Inject


private const val CHANNEL_ID = "my_channel_id"
private const val CHANNEL_NAME = "My Channel"
private const val CHANNEL_DESCRIPTION = "This is my notification channel"

class LocationTrackingService @Inject constructor(): LifecycleService() {

    private val notificationId = 1
    private lateinit var locationClient: FusedLocationProviderClient
    companion object {
        val locationList = MutableLiveData<MutableList<LatLng>>()
    }

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        locationList.value = mutableListOf()
        startLocationTracking()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
    }
    private fun stopLocationTracking() {
        lifecycleScope.cancel()
        locationClient.removeLocationUpdates(locationCallback)
        locationList.postValue(locationList.value)
    }

    private fun startLocationTracking() {
       lifecycleScope.launch {
           getLocationUpdates()
       }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocationUpdates() {
        val request = LocationRequest().apply {
            interval = 1000L
            fastestInterval = 1000L
            priority = PRIORITY_HIGH_ACCURACY
        }
        locationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            Log.d("LocationTrackingService", "Callback before onLocationResult: $result")

            super.onLocationResult(result)
            result?.locations?.let { locations ->
                for(location in locations) {
                    val pos = LatLng(location.latitude, location.longitude)
                    val time = location.time
                    Log.d("locationCallBack", "pos: $pos; time: $time")
                    locationList.value?.add(pos)
                }
            }
        }
    }

    private fun createNotification(): Notification {
        // Create a notification for the foreground service
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Trip in Progess...")
            .setContentText("Tracking your location")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
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
}