package com.example.dingo.social.profile

import android.se.omapi.Session
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.dingo.CustomDialog
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),

    navControllerSignOut: NavHostController

) {
    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
    val totalFlora = DingoDexEntryListings.floraEntryList.size
    val totalFauna = DingoDexEntryListings.faunaEntryList.size
    val numFloraFound = totalFlora - viewModel.getNumUncollectedFlora()
    val numFaunaFound = totalFauna - viewModel.getNumUncollectedFauna()
    val achievements = viewModel.getAchievements(LocalContext.current)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "${SessionInfo.currentUsername ?: "Test"}'s Profile",
            fontSize = UIConstants.SUBTITLE1_TEXT,
        )
        Button(onClick = {
            coroutineScope.launch {
                viewModel.onSignOutClick(navControllerSignOut)
            }
        }) {
            Text("Sign out")
        }

        FriendSection()


//      Text(
        Text("Flora: $numFloraFound / $totalFlora")
        Text("Flora: $numFaunaFound / $totalFauna")

        Text("Achievements: ")
        LazyColumn(
            modifier = Modifier.weight(1.0f, true)
        ) {
            items(achievements.size) {
                Text(
                    modifier = Modifier.height(20.dp),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    text ="${achievements[it].name}",
                )
                Text(
                    modifier = Modifier.padding(all = 12.dp),
                    text = achievements[it].description
                )
                Divider(
                    thickness = 1.dp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun FriendSection(
    viewModel: ProfileViewModel = hiltViewModel(),
    ) {
    // todo: loading for friend items
    val friendItems = viewModel
        .getFriendsForUser(SessionInfo.currentUserID)
        .observeAsState()
    val sendFriendReqDialogState = remember { mutableStateOf(false) }
    if (sendFriendReqDialogState.value) {
        SendFriendReqDialog(
            SessionInfo.currentUserID,
        ) {
            sendFriendReqDialogState.value = false
        }
    }
    val pendingFriendReqDialogState = remember { mutableStateOf(false) }
    if (pendingFriendReqDialogState.value) {
        PendingFriendRequestDialog() {
            pendingFriendReqDialogState.value = false
        }
    }
    val friendListDialogState = remember { mutableStateOf(false) }
    if (friendListDialogState.value) {
        FriendList(
            friendItems
        ) {
            friendListDialogState.value = false
        }
    }
    Column(
        modifier = Modifier.padding(UIConstants.MEDIUM_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Friends",
            fontSize = UIConstants.SUBTITLE1_TEXT,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "My Friends (${if (friendItems.value.isNullOrEmpty()) 0 else friendItems.value!!.size})")
            Text(
                text = "See All",
                modifier = Modifier.clickable { friendListDialogState.value = true }
            )
        }
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { sendFriendReqDialogState.value = true },
            ) {
                Text("Send Friend Request")
            }
            Button(
                onClick = { pendingFriendReqDialogState.value = true },
            ) {
                Text(
                    "Pending Friend Requests",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FriendList(
    friendItems: State<MutableList<User>?>,
    onDismissRequest: () -> Unit,
) {
    CustomDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "My Friends",
                fontSize = UIConstants.SUBTITLE1_TEXT,
            )
            LazyColumn {
                val friends = friendItems.value
                if (friends != null) {
                    items(friends.size) {
                        Row(
                            modifier = Modifier.padding(vertical = UIConstants.SMALL_PADDING),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(friends[it].username)
                            Divider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = Color.Gray,
                            )
                            Text(text = "oh yeah")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SendFriendReqDialog(
    userId: String,
    viewModel: ProfileViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
) {
    val currentContext = LocalContext.current
    var usernameState by remember { mutableStateOf("") }
    CustomDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Send Friend Request",
                fontSize = UIConstants.SUBTITLE2_TEXT
            )
            TextField(
                value = usernameState,
                onValueChange = { usernameState = it },
                placeholder = { Text("Friend's username") },
                modifier = Modifier.padding(UIConstants.MEDIUM_PADDING)
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val friendReqOk = viewModel.sendFriendReq(
                            userId,
                            usernameState,
                        )
                        println("friend req ok?!: $friendReqOk")
                        if (friendReqOk) {
                            Toast.makeText(
                                currentContext,
                                "Sent friend request!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismissRequest()
                        } else {
                            Toast.makeText(
                                currentContext,
                                "Invalid username",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                ) {
                    Text(text = "Send request")
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
fun PendingFriendRequestDialog(
    viewModel: ProfileViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
) {
    val pendingFriendReqItems = viewModel
        .getPendingFriendReqs(SessionInfo.currentUserID)
        .observeAsState()
    CustomDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.75f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Pending Friend Requests",
                fontSize = UIConstants.SUBTITLE2_TEXT
            )
            Box(
                modifier = Modifier.weight(1.0f),
                contentAlignment = Alignment.Center,
            ) {
                if (pendingFriendReqItems.value.isNullOrEmpty()) {
                    Text(
                        "No pending friend requests. You're all caught up!",
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        var pending = pendingFriendReqItems.value
                        if (pending != null) {
                            items(pending.size) {
                                PendingFriendReqItem(
                                    viewModel,
                                    SessionInfo.currentUserID,
                                    pending[it]
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingFriendReqItem(
    viewModel: ProfileViewModel = hiltViewModel(),
    userId: String,
    pendingUser: User,
) {
    val currentContext = LocalContext.current
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(pendingUser.username)
        Divider(
            modifier = Modifier
                .height(30.dp)
                .width(1.dp),
            color = Color.Gray,
        )
        Button(
            onClick = {
                println("tryna accept friend req")
                val msg = viewModel.acceptFriendReq(pendingUser.id, userId)
                Toast.makeText(
                    currentContext,
                    msg,
                    Toast.LENGTH_SHORT,
                ).show()

            }
        ) {
            Text("Accept")
        }
        Button(
            onClick = {
                println("tryna decline friend req")
                val msg = viewModel.declineFriendReq(pendingUser.id, userId)
                Toast.makeText(
                    currentContext,
                    msg,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        ) {
            Text("Decline")
        }
    }
}