package com.example.dingo.trips

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
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

//import androidx.compose.material.Button
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Trip
import com.example.dingo.model.service.impl.getTimeDiffMessage
import com.google.firebase.Timestamp
import com.google.maps.android.compose.Polyline


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current


    val currUser = SessionInfo.currentUser

    Log.d("TripScreen", "currUser $currUser")
    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX"
    val dummyUsername = "Eric Shang"
    val limit = 50
    val feedItems = viewModel
        .getTripFeed(currUser?.id ?: dummyUserId)
        .observeAsState()


    val navController = rememberNavController()

    var selectedTrip: Trip? by remember { mutableStateOf(null) }
    var trackedLocations : List<LatLng> by remember { mutableStateOf(emptyList()) }
    LocationTrackingService().createNotificationChannel(context)
    LocationTrackingService.locationList.observe(lifeCycleOwner, Observer {
        trackedLocations = it
        if (SessionInfo.trip != null) {
            SessionInfo.trip!!.locations = it
        }
    })

    val permissionState = remember { mutableStateOf(false) }
    permissionState.value = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val isServiceRunning = remember { mutableStateOf(false) }
    isServiceRunning.value = SessionInfo.trip != null

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isServiceRunning.value = true
        SessionInfo.trip = Trip()
        SessionInfo.trip!!.startTime = Timestamp.now()
        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationTrackingService::class.java)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(
            navController = navController,
            startDestination = TripNavigationItem.TripPostFeed.route
        ) {
            composable(TripNavigationItem.TripPostFeed.route) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),

                    ) {
                    Text(
                        text = "Trips",
                        fontSize = UIConstants.TITLE_TEXT,
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        var trips =  feedItems.value
                        if (trips != null) {
                            items(trips.size) {
//                            TODO: Make Trip Screen for entry space
                                Log.d("tripScreen", "trips size: ${trips.size} trip: ${trips[it]}")
                                TripPost(trips[it], navController, onTripSelected = { selectedTrip = it })
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Button(
                            onClick = {
                                if (!isServiceRunning.value) {
                                    if (!permissionState.value) {
                                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    } else {
                                        isServiceRunning.value = true
                                        SessionInfo.trip = Trip()
                                        SessionInfo.trip!!.startTime = Timestamp.now()
                                        ContextCompat.startForegroundService(
                                            context,
                                            Intent(context, LocationTrackingService::class.java)
                                        )
                                    }
                                } else {
                                    isServiceRunning.value = false
                                    context.stopService(Intent(context, LocationTrackingService::class.java))
                                    SessionInfo.trip!!.endTime = Timestamp.now()
                                    Log.d("here", "stopped trip")
                                    navController.navigate(TripNavigationItem.CreatePost.route)
                                }
                            },
                        ) {
                            Text(text = if (isServiceRunning.value) "Stop Tracking Permission Granted: ${permissionState.value}" else "Start Tracking Permission Granted: ${permissionState.value}")

                        }
                    }
                }
            }

            composable(TripNavigationItem.CreatePost.route) {
                var textContentState by remember { mutableStateOf("") }
                if (SessionInfo.trip != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),) {

                                TextField(
                                    value = textContentState,
                                    onValueChange = { textContentState = it },
                                    placeholder = { Text("Name your trip")},
                                    isError = textContentState.isEmpty()
                                )

                        Column(modifier = Modifier.weight(6.0f, true)) {
                            Text(
                                text = "Description",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp
                            )
                            Box(modifier = Modifier.fillMaxSize()) {
                                tripMap(SessionInfo.trip!!.locations, true)
                            }
                        }
                        Row(modifier = Modifier.weight(1.0f, true), horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    viewModel.discardTrip()
                                    navController.navigate(TripNavigationItem.TripPostFeed.route)
                                }
                            ) {
                                Text(text = "Discard Current Trip")
                            }
                            Button(
                                enabled = !textContentState.isEmpty(),
                                onClick = {
                                    if (SessionInfo.trip != null) {
                                        SessionInfo.trip!!.title = textContentState
                                        SessionInfo.trip!!.username = SessionInfo.currentUsername
                                        SessionInfo.trip!!.userId = SessionInfo.currentUserID

                                        val tripId = viewModel.createTrip(trip = SessionInfo.trip!!)
                                    }
                                    navController.navigate(TripNavigationItem.TripPostFeed.route)
                                }
                            ) {
                                Text(text = "Post Trip")
                            }
                        }
                    }
                }
//                    tripMap(trackedLocations, true)
//                PostTripModal(navController = navController,  trip = SessionInfo.trip )
            }
            composable(TripNavigationItem.TripDetails.route) {
                Log.d("tripScreen", "Trip Details selectedTrip: $selectedTrip")
                if (selectedTrip != null) {
                    val trip = selectedTrip as Trip
                    Column(modifier = Modifier.fillMaxSize()) {

                        CenterAlignedTopAppBar(
                            title = { Text(
                                    text = trip.title,
                                    fontSize = UIConstants.TITLE_TEXT,
                                ) },

                            navigationIcon = {
                                IconButton(onClick = { navController.navigate(TripNavigationItem.TripPostFeed.route) }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Description",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp
                            )
                            Box(modifier = Modifier.fillMaxSize()) {
                                tripMap(trip.locations, true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripPost(trip: Trip, navController: NavHostController, onTripSelected : (Trip) -> Unit) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ClickableText(
            text = AnnotatedString("${trip.username} posted ${trip.title} ${getTimeDiffMessage(trip.timestamp)} ago with ${trip.discoveredEntries.size} entrie(s) and ${trip.locations.size} location points"),
            onClick = {
                onTripSelected(trip)
                navController.navigate(TripNavigationItem.TripDetails.route)
            }
        )

    }
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

@Composable
fun tripMap(points: List<LatLng>, fullSize: Boolean) {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(points.lastOrNull() ?: LatLng(51.52061810406676, -0.12635325270312533), 15f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        if (points.lastOrNull() != null) {
            Polyline(points = points)
            Marker(
                state = MarkerState(position = points.last()),
                title = "Current Location",
                snippet = "You are here"
            )
        }
    }
}


sealed class TripNavigationItem(
    val name: String,
    val route: String,
) {
    object TripPostFeed : TripNavigationItem(
        name = "TripPostFeed",
        route = "Trippostfeed",
    )

    object TrackTrip : TripNavigationItem(
        name = "TrackTrip",
        route = "TrackTrip",
    )
    object CreatePost : TripNavigationItem(
        name = "CreatePost",
        route = "createpost",
    )
    object TripDetails : TripNavigationItem(
        name = "TripDetails",
        route = "TripDetails",
    )
    object AddMember : TripNavigationItem(
        name = "AddMember",
        route = "addmember",
    )
    object ViewComments : TripNavigationItem(
        name = "ViewComments",
        route = "viewcomments",
    )
    object MyProfile : TripNavigationItem(
        name = "MyProfile",
        route = "myprofile",
    )
}



@Composable
private fun TripDetailsModal(
    viewModel: TripViewModel = hiltViewModel(),
    navController: NavHostController,
    tripId: String,
    userId: String,
    username: String,
    locations : List<LatLng>
) {
    var textContentState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Post to Trip")
        TextField(
            value = textContentState,
            onValueChange = { textContentState = it },
            label = { Text("")}
        )
        Button(
            onClick = {
                Log.d("tripScreen", "Discard Current Trip Button, trackedLocations: $locations")
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Return to Trip Feed")
        }
        Button(
            onClick = {
//                viewModel.makeDummyTrips(trackedLocations)
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Return to Trip Feed")
        }
    }
}

@Composable
private fun PostTripModal(
    viewModel: TripViewModel = hiltViewModel(),
    navController: NavHostController,
    trip: Trip?

    ) {
    var textContentState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Name your Trip")
        TextField(
            value = textContentState,
            onValueChange = { textContentState = it },
            label = { Text("")}
        )
        Button(
            onClick = {
                Log.d("tripScreen", "Discard Current Trip Button, trackedLocations: ${trip?.locations}")
                viewModel.discardTrip()
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Discard Current Trip")
        }
        Button(
            onClick = {
//                viewModel.makeDummyTrips(trackedLocations)
                if (trip != null) {
                    trip.title = textContentState
                    trip.username = SessionInfo.currentUsername
                    trip.userId = SessionInfo.currentUserID
//                    val tripId = viewModel.createTrip(trip = trip)
                    val tripId = viewModel.makeDummyTrip(trip = trip)

                }

//                TODO: post as trip and create a post of the given trip ID
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Post Trip")
        }
    }
}


