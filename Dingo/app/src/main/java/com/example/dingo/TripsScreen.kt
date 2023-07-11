package com.example.dingo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun TripsScreen() {
    ComposeDemoApp()
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