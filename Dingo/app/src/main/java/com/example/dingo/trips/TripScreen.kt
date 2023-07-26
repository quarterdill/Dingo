package com.example.dingo.trips
import com.bumptech.glide.Glide

//import androidx.compose.material.Button
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.R
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Trip
import com.example.dingo.model.service.impl.getTimeDiffMessage
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date


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
    viewModel.createNotificationChannel(context)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),
                    ) {

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
                                if (SessionInfo.trip!!.locations.isNotEmpty()) {
                                  tripMap(SessionInfo.trip!!, true)
                                }
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
                                text = "You have collected ${trip.discoveredEntries.size} Dingo(s) ${getTimeDiffMessage(trip.timestamp)} ago",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp
                            )
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (trip != null && trip.locations.isNotEmpty()) {
                                    tripMap(trip, true)
                                }
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
fun tripMap(trip : Trip,  fullSize: Boolean) {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(trip.locations.lastOrNull() ?: LatLng(51.52061810406676, -0.12635325270312533), 15f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        if (trip.locations.lastOrNull() != null) {
            // Convert the timestamp to a Java Date object
            val startDate: Date = trip.startTime.toDate()
            val endDate: Date = trip.endTime.toDate()

            // Convert the timestamp to a Java Date object
            // Create a SimpleDateFormat object with your desired date format
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

            // Format the date as desired

            val formattedStartTime = dateFormat.format(startDate)
            val formattedEndTime = dateFormat.format(endDate)


            Polyline(points = trip.locations)
            Marker(
                state = MarkerState(position = trip.locations.last()),
                title = "Start",
                snippet = "Trip Started at ${formattedStartTime}"
            )
            Marker(
                state = MarkerState(position = trip.locations.first()),
                title = "Finish",
                snippet = "Trip Ended at ${formattedEndTime}"
            )

            // Assuming you have a list of picture paths and picture locations
            val picturePaths: List<String> = trip.picturePaths
            val pictureLocations: List<LatLng> = trip.pictureLocations

//            val icon = bitmapDescriptorFromVector(
//                LocalContext.current, R.drawable.pin
//            )
            // Iterate through the picture paths and picture locations
            picturePaths.forEachIndexed { index, picturePath ->
                val pictureLocation = pictureLocations[index]
                // You can customize the marker icon, if desired
//                Marker(
//                    state = MarkerState(position = pictureLocation),
//                    title = "Picture $index",
//                    snippet = "Location: ${pictureLocation.latitude}, ${pictureLocation.longitude}",
//                    icon = BitmapDescriptorFactory.fromResource(R.drawable.fauna_placeholder)
//                )
                MarkerInfoWindow(
                    state = MarkerState(position = pictureLocation),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.fauna_placeholder),
                ) { marker ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(35.dp, 35.dp, 35.dp, 35.dp)
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.fauna_placeholder),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth(),

                                )
                            //.........................Spacer
                            Spacer(modifier = Modifier.height(24.dp))
                            //.........................Text: title
                            Text(
                                text = "Marker Title",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            //.........................Text : description
                            Text(
                                text = "Customizing a marker's info window",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                                    .fillMaxWidth(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            //.........................Spacer
                            Spacer(modifier = Modifier.height(24.dp))

                        }

                    }


                    // Add the marker to the map
                }
                }
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
            label = { Text("") }
        )
        Button(
            onClick = {
                Log.d(
                    "tripScreen",
                    "Discard Current Trip Button, trackedLocations: ${trip?.locations}"
                )
                viewModel.discardTrip()
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Discard Current Trip")
        }
        Button(
            onClick = {
                if (trip != null) {
                    trip.title = textContentState
                    trip.username = SessionInfo.currentUsername
                    trip.userId = SessionInfo.currentUserID
                    val tripId = viewModel.createTrip(trip = trip)
//                    val tripId = viewModel.makeDummyTrip(trip = trip)

                }

//                TODO: post as trip and create a post of the given trip ID
                navController.navigate(TripNavigationItem.TripPostFeed.route)
            }
        ) {
            Text(text = "Post Trip")
        }
    }
}


