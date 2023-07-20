package com.example.dingo.trips

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect

//import androidx.compose.material.Button
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer

@Composable
fun TripScreen(
    viewModel: TripViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX"
    val dummyUsername = "Eric Shang"
    val limit = 50
    val feedItems = viewModel
        .getTripFeed(dummyUserId)
        .observeAsState()

    LaunchedEffect(key1 = true) {
        LocationTrackingService().createNotificationChannel(context)
        LocationTrackingService.locationList.observe(lifeCycleOwner, Observer {
            var locations:List<LatLng> = viewModel.locationTrackingStopped(it)
            Log.d("tripViewScreen", "locations: $locations")
            // TODO:  Get user
//            viewModel.makeDummyTrips(locations)

//            Eric Shang
            var tripFeed = viewModel.getTripFeed("Q0vMYa9VSh7tyFdLTPgX")
            Log.d("tripViewScreen", "tripFeed ${tripFeed}")






        })
    }
    LocationPermissionScreen()
    LocationTrackingScreen()
}
@Composable
fun LocationPermissionScreen() {
    val context = LocalContext.current
    val permissionState = remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
    }

    Column {
        Button(
            onClick = {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        ) {
            Text(text = "Request Location Permission")
        }

        Text(text = "Permission Granted: ${permissionState.value}")
    }
}

@Preview
@Composable
fun LocationTrackingScreen() {
    var isServiceRunning by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column {
        Button(
            onClick = {
                isServiceRunning = !isServiceRunning
                if (isServiceRunning) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, LocationTrackingService::class.java)
                    )
                } else {
                    context.stopService(Intent(context, LocationTrackingService::class.java))
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = if (isServiceRunning) "Stop Tracking" else "Start Tracking")
        }
    }
}

@Composable
fun ComposeDemoApp() {
    val singapore = LatLng(51.52061810406676, -0.12635325270312533)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = singapore),
            title = "London",
            snippet = "Marker in Big Ben"
        )
    }
}
