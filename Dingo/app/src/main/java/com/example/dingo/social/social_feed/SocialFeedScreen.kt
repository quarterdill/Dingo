package com.example.dingo.social.social_feed
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.CustomDialog
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.dingodex.DingoDexViewModel
import com.example.dingo.model.Comment
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.model.Post
import com.example.dingo.model.Trip
import com.example.dingo.trips.TripViewModel
import com.example.dingo.trips.tripMap
import com.example.dingo.ui.theme.color_on_primary
import com.example.dingo.ui.theme.color_on_secondary
import com.example.dingo.ui.theme.color_primary
import com.example.dingo.ui.theme.color_secondary
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Composable
fun SocialFeedScreen(
    viewModel: SocialFeedViewModel = hiltViewModel(),
    dingoDexViewModel: DingoDexViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val currentUser = SessionInfo.currentUser
    val currentUserId = SessionInfo.currentUserID
    val tripFeedItems = tripViewModel
        .getTripFeed(currentUserId)
        .observeAsState()
    val myDingoDexFauna = dingoDexViewModel
        .getDingoDexCollectedEntries(true, SessionInfo.currentUserID)
        .observeAsState()
    val myDingoDexFlora = dingoDexViewModel
        .getDingoDexCollectedEntries(false, SessionInfo.currentUserID)
        .observeAsState()
    var currentPostId = remember { mutableStateOf("") }
    val feedItems = viewModel.userFeed.observeAsState()
    var createNewPost = remember { mutableStateOf(false) }

    val isLoading = viewModel.isLoading.observeAsState()


    if (createNewPost.value) {
        var trips = mutableListOf<Trip>()
        if (tripFeedItems.value != null) {
            trips = tripFeedItems.value!!
        }
        println("MY TRIPS: $trips")
        CreatePostModal(
            viewModel,
            SessionInfo.currentUserID,
            SessionInfo.currentUsername,
            tripFeedItems = trips as List<Trip>,
            myDingoDexFauna.value as MutableList<DingoDexEntry>,
            myDingoDexFlora.value as MutableList<DingoDexEntry>,
        ) {
            createNewPost.value = false
        }
    }
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (feedItems.value.isNullOrEmpty()) {

            if (isLoading.value!!) {
                CircularProgressIndicator()
            } else {
                Text("No posts found... try adding some friends")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                var posts = feedItems.value
                if (posts != null) {
                    println("test rebuild ${posts.size}")
                    items(posts.size) {
                        val post = posts[it].first
                        val trip = posts[it].second
                        SocialPost(post,trip, currentPostId)
                    }
                }
            }
        }

        FloatingActionButton(
            containerColor = color_on_primary,
            contentColor = color_primary,
            modifier = Modifier
                .padding(UIConstants.MEDIUM_PADDING)
                .align(alignment = Alignment.BottomEnd),
            onClick = { createNewPost.value = true }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Post")
        }
    }
}

@Composable
private fun entryShower(
    entryId: String,
    viewModel: DingoDexViewModel = hiltViewModel(),
) {
    println("GETTING ENTRY PIC: $entryId")
    var entry = viewModel.getEntryById(entryId)
    var entryPath = ""
    if (entry != null) {
        if (entry.displayPicture != "default") {
            entryPath = entry.displayPicture
        }
    }
    println("got entry path: $entryPath")
    if (entryPath != "") {
        val storageRef = FirebaseStorage.getInstance().reference.child(entryPath)

        val assetManager: AssetManager = LocalContext.current.assets
        var defaultBitmap = BitmapFactory.decodeStream(assetManager.open("images/white_tailed_deer.jpg"))
        var bitmap: Bitmap by remember {mutableStateOf(defaultBitmap)}

//        storageRef.getBytes(15000000).addOnSuccessListener {
//            println("bitmap")
//            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//            println(bitmap)
//            println("bitmap come")
//        }.addOnFailureListener {
//            println("Error occurred when downloading user's DingoDex image from Firebase $it")
//        }
//        println("making image rn")
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "entry picture",
        )
//        println("done making image rn")
    }
}

@Composable
private fun SocialPost(
    post: Post,
    trip: Trip?,
    currentPostId: MutableState<String>,
) {
    var commentDialogState = remember { mutableStateOf(false) }
    if (commentDialogState.value) {
        CommentsDialog(currentPostId) {
            commentDialogState.value = false
        }
    }
    Column {
        var timeDiffMsg = getTimeDiffMessage(post.timestamp)

        if (trip != null && trip.locations.isNotEmpty()) {
            tripMap(trip = trip, fullSize = false )
        }
        if (post.entryId != "") {
            entryShower(post.entryId)
        }
        Text(
            modifier = Modifier.height(20.dp),
            fontSize = 12.sp,
            color = Color.Gray,
            overflow= TextOverflow.Ellipsis,
            text="${post.username} posted ${trip?.title ?: ""} $timeDiffMsg ago with tripId: ${post.tripId ?: "none"}"

        )
        Text(
            modifier = Modifier.padding(all = 12.dp),
            text = post.textContent
        )
        ClickableText(
            style = TextStyle(
                color = Color.LightGray,
            ),
            text = AnnotatedString("${post.comments.size} comment(s)"),
            onClick = {
                currentPostId.value = post.id
                commentDialogState.value = true
            }
        )
        Divider(
            thickness = 1.dp,
            color = Color.Gray,
        )
    }
}





@Composable
fun DropdownMenuExample(items: List<Trip>, onTripSelected: (Trip) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    Button(
        onClick = { expanded = true },
    ) {
        Text(
            text = "Add Trip",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {Text("${item.title} with id: ${item.id} posted ${getTimeDiffMessage(item.timestamp)} ago")},
                onClick = {
                    selectedIndex = index
                    onTripSelected(item)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun DropdownEntryMenu(items: List<DingoDexEntry>, onEntrySelected: (DingoDexEntry) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    Button(
        onClick = { expanded = true },
    ) {
        Text(
            text = "Add Entry",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {Text("${item.name}")},
                onClick = {
                    selectedIndex = index
                    onEntrySelected(item)
                    expanded = false
                }
            )
        }
    }
}


@Composable
private fun CreatePostModal(
    viewModel: SocialFeedViewModel = hiltViewModel(),
    userId: String,
    username: String,
    tripFeedItems:List<Trip>,
    myDingoDexFauna: MutableList<DingoDexEntry>,
    myDingoDexFlora: MutableList<DingoDexEntry>,
    onDismissRequest : () -> Unit,
) {
    var selectedTrip : Trip? by remember { mutableStateOf(null) }
    var selectedEntry : DingoDexEntry? by remember { mutableStateOf(null) }

    CustomDialog(onDismissRequest = onDismissRequest) {
        var textContentState by remember { mutableStateOf("") }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (selectedTrip != null) {
                Text(fontSize = UIConstants.SUBTITLE2_TEXT,    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text="Selected trip: ${selectedTrip?.title ?: "none"}",
                )
            } else if (selectedEntry != null) {
                Text(fontSize = UIConstants.SUBTITLE2_TEXT,    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text="Selected entry: ${selectedEntry?.name ?: "none"}",
                )
            } else {
                Text(fontSize = UIConstants.SUBTITLE2_TEXT,    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text="No selected entry or trip",
                )
            }

            TextField(
                modifier = Modifier
                    .padding(vertical = UIConstants.MEDIUM_PADDING)
                    .height(70.dp),
                value = textContentState,
                onValueChange = { textContentState = it },
                label = { Text("") }
            )
            println("TRIP FEED ITEMS: $tripFeedItems")
            if (tripFeedItems.isNotEmpty() && selectedEntry == null) {
                DropdownMenuExample(tripFeedItems, onTripSelected = { newValue ->
                    selectedTrip = newValue
                })
//                if (selectedTrip != null) {
//                    Button(
//                        onClick = {
//                            selectedTrip = null
//                        }
//                    ) {
//                        Text("Remove Trip")
//                    }
//                }
            }
            if (selectedTrip == null) {
                var myDingoDexItems = myDingoDexFauna + myDingoDexFlora

                myDingoDexItems = myDingoDexItems.sortedByDescending {it.timestamp}
                if (myDingoDexItems.isNotEmpty()) {
//                    if (selectedEntry != null) {
//                        Button(
//                            onClick = {
//                                selectedEntry = null
//                            }
//                        ) {
//                            Text("Remove Entry")
//                        }
//                    }
                    DropdownEntryMenu(myDingoDexItems, onEntrySelected = { newValue ->
                        selectedEntry = newValue
                    })
                }
            }

            //   SELECT trip
//            Get trip feed names
//            clickable
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        viewModel.makePost(
                            userId,
                            username,
                            textContentState,
                            selectedEntry,
                            selectedTrip?.id ?: null,
                        )
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                ) {
                    Text(text = "Create Post")
                }
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                ) {
                    Text(text = "Cancel")
                }

            }
//            Column(
//                modifier = Modifier.height(30.dp)
//            ) {
//                if (tripFeedItems.isNotEmpty()) {
//                    DropdownMenuExample(tripFeedItems, onTripSelected = { newValue ->
//                        selectedTrip = newValue
//                    })
//                }
//            }
        }
    }
}

@Composable
private fun CommentsDialog(
    currentPostId: MutableState<String>,
    viewModel: SocialFeedViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
) {
    val fetchComments = viewModel
        .getCommentsForPost(currentPostId.value)
        .observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()
    CustomDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Comments",
                fontSize = UIConstants.SUBTITLE1_TEXT,
                modifier = Modifier.padding(bottom = UIConstants.MEDIUM_PADDING),
            )
            var textContentState by remember { mutableStateOf("") }
            Box(
                modifier = Modifier.weight(1.0f),
                contentAlignment = Alignment.Center,
            ) {
                if (fetchComments.value.isNullOrEmpty()) {
                    if (isLoading.value!!) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            "No comments yet. Be the first to comment!",
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // idk if this will make it crash or smth
                        var comments = fetchComments.value
                        println("comments: $comments")
                        if (comments != null) {
                            items(comments.size) {
                                CommentText(comments[it])
                            }
                        }
                    }
                }
            }

            TextField(
                modifier = Modifier
                    .padding(vertical = UIConstants.MEDIUM_PADDING)
                    .height(100.dp),
                value = textContentState,
                onValueChange = { textContentState = it },
                label = { Text("") }
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (textContentState != "") {
                            viewModel.makeComment(
                                currentPostId.value,
                                textContentState,
                            )
                        }
                        textContentState = ""
                    }
                ) {
                    Text(text = "Comment")
                }
                Button(
                    onClick = onDismissRequest
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Composable
private fun CommentText(comment: Comment){
    var timeDiffMsg = getTimeDiffMessage(comment.timestamp)
    Text(
        modifier = Modifier.height(20.dp),
        fontSize = 10.sp,
        color = Color.Gray,
        text="${comment.authorName} posted $timeDiffMsg ago")
    Text(
        modifier = Modifier.padding(all = 12.dp),
        text = comment.textContent
    )

    Divider(
        thickness = 1.dp,
        color = Color.Gray,
    )
}

private fun getTimeDiffMessage(timestamp: Timestamp): String {
    val timeDiff = (Timestamp.now().seconds - timestamp.seconds) / 60
    if (timeDiff < 1) {
        return "a minute"
    } else if (timeDiff < 60) {
        return "$timeDiff minutes"
    } else if (timeDiff < 60 * 2) {
        return "an hour"
    } else if (timeDiff < 60 * 24) {
        return "${timeDiff / 60} hours"
    } else if (timeDiff < 60 * 24 * 2) {
        return "a day"
    } else if (timeDiff < 60 * 24 * 7) {
        return "${timeDiff / (60 * 24)} days"
    } else if (timeDiff < 60 * 24 * 7 * 2) {
        return "a week"
    } else if (timeDiff < 60 * 24 * 7 * 4) {
        return "${timeDiff / (60 * 24 * 7)} weeks"
    } else if (timeDiff < 60 * 24 * 7 * 4 * 2) {
        return "a month"
    } else if (timeDiff < 60 * 24 * 7 * 4 * 12) {
        return "${timeDiff / (60 * 24 * 7 * 4)} months"
    } else {
        return "${timeDiff / (60 * 24 * 4 * 12)} years"
    }
}