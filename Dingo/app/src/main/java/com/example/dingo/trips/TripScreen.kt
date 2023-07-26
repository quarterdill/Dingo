package com.example.dingo.trips

import android.content.Intent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Trip
import com.example.dingo.model.service.impl.getTimeDiffMessage
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
    var isServiceRunning by remember { mutableStateOf(false) }
    var trackedLocations : List<LatLng> by remember { mutableStateOf(emptyList()) }
    var selectedTrip: Trip? by remember { mutableStateOf(null) }

    val permissionState = remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
    }


    LaunchedEffect(key1 = true) {
        LocationTrackingService().createNotificationChannel(context)
        LocationTrackingService.locationList.observe(lifeCycleOwner, Observer {
            var locations:List<LatLng> = viewModel.locationTrackingStopped(it)
            trackedLocations= locations
            Log.d("tripViewScreen", "locations: $locations")
//                        viewModel.makeDummyTrips(locations)
        })
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (!permissionState.value) {
                                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                                isServiceRunning = !isServiceRunning
                                if (isServiceRunning) {
                                    ContextCompat.startForegroundService(
                                        context,
                                        Intent(context, LocationTrackingService::class.java)
                                    )
                                    Log.d("tripScreen", "trackedLocations: $trackedLocations")
                                    navController.navigate(TripNavigationItem.TrackTrip.route)
                                } else {
                                    context.stopService(Intent(context, LocationTrackingService::class.java))
                                    navController.navigate(TripNavigationItem.TripPostFeed.route)
                                }
                            },
                        ) {
                            Text(text = if (isServiceRunning) "Stop Tracking Permission Granted: ${permissionState.value}" else "Start Tracking Permission Granted: ${permissionState.value}")

                        }
                    }
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
                }
            }
            composable(TripNavigationItem.TrackTrip.route) {
                val dummyTripId = "dummy"
                LocationPermissionScreen()
                tripMap(trackedLocations, true)
                val context = LocalContext.current
                Column {
                    Button(
                        onClick = {
                            if (!permissionState.value) {
                                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            isServiceRunning = !isServiceRunning
                            if (isServiceRunning) {
                                ContextCompat.startForegroundService(
                                    context,
                                    Intent(context, LocationTrackingService::class.java)
                                )
                                Log.d("tripScreen", "trackedLocations: $trackedLocations")

                                navController.navigate(TripNavigationItem.CreatePost.route)

                            } else {
                                context.stopService(Intent(context, LocationTrackingService::class.java))
                                navController.navigate(TripNavigationItem.CreatePost.route)

                            }
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = if (isServiceRunning) "Stop Tracking Permission Granted: ${permissionState.value}" else "Start Tracking Permission Granted: ${permissionState.value}")
                    }
                }
            }
            composable(TripNavigationItem.CreatePost.route) {
                Log.d("tripScreen", "trackedLocations: $trackedLocations")
                tripMap(trackedLocations, true)
                PostTripModal(navController = navController,  locations = trackedLocations, discoveredEntries = emptyList() )
            }
            composable(TripNavigationItem.TripDetails.route) {
                Log.d("tripScreen", "Trip Details selectedTrip: $selectedTrip")
                if (selectedTrip != null) {
                    val trip = selectedTrip as Trip
                    Column(modifier = Modifier.fillMaxSize()) {

                        CenterAlignedTopAppBar(
                            title = { Text(text = "Map Title",
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
//                    Box(modifier = Modifier.fillMaxSize()) {
//
//                    TopAppBar(
//                        title = { Text(text = "Map Title") },
//                        navigationIcon = {
//                            IconButton(onClick = { /* Handle back action here */ }) {
//                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                            }
//                        }
//                    )
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(text = "day, time, km, animals discovered", fontSize = 16.sp)
//                        Box(modifier = Modifier.fillMaxSize()) {
//                            tripMap(trip.locations, true)
//                        }
//                    }
                }
            }
//                TripDetailsModal(navController = navController, tripId = "", userId = "" , username = "", locations = trackedLocations )
//            }
        }
    }
}

@Composable
private fun TripPost(trip: Trip, navController: NavHostController, onTripSelected : (Trip) -> Unit ) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ClickableText(
            text = AnnotatedString("${trip.username} posted ${trip.locations} with ${trip.discoveredEntries} entrie(s)"),
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
            Text(text = if (isServiceRunning) "Stop Tracking" else "Start Tracking ")
        }
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
private fun TripPost(
    post: Trip,
    navController: NavHostController,
    viewModel: TripViewModel,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var timeDiffMsg = getTimeDiffMessage(post.timestamp)
        Column(

        ) {
            Text(
                modifier = Modifier.height(20.dp),
                fontSize = 12.sp,
                color = Color.Gray,
                text="${post.username} posted $timeDiffMsg ago")
            Text(
                modifier = Modifier.padding(all = 12.dp),
                text = "${post.discoveredEntries.size}"
            )
            ClickableText(
                text = AnnotatedString("${post.discoveredEntries.size} comment(s)"),
                onClick = {
                    navController.navigate(TripNavigationItem.ViewComments.route)
                }
            )
            Divider(
                thickness = 1.dp,
                color = Color.Gray,
            )

        }
    }
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
    locations : List<LatLng>,
    discoveredEntries: List<String>,

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
                Log.d("tripScreen", "Discard Current Trip Button, trackedLocations: $locations")
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Discard Current Trip")
        }
        Button(
            onClick = {
//                viewModel.makeDummyTrips(trackedLocations)
                val currUser = SessionInfo.currentUser
                if (currUser != null) {
                    val tripId = viewModel.createTrip(userId= currUser.id,username= currUser.username,locations=locations,

                        )
                }


//                TODO: post as trip and create a post of the given trip ID
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Post Trip")
        }
    }
}


