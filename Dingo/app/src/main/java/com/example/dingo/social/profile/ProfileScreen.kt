package com.example.dingo.social.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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
    val friendItems = viewModel
        .getFriendsForUser(SessionInfo.currentUserID)
        .observeAsState()
    var sendFriendReqDialogState = remember { mutableStateOf(false) }
    var pendingFriendReqDialogState = remember { mutableStateOf(false) }
    if (sendFriendReqDialogState.value) {
        SendFriendReqDialog(
            SessionInfo.currentUserID,
        ) {
            sendFriendReqDialogState.value = false
        }
    }
    if (pendingFriendReqDialogState.value) {
        PendingFriendRequestDialog() {
            pendingFriendReqDialogState.value = false
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Profile")
        Button(onClick = {
            coroutineScope.launch {
                viewModel.onSignOutClick(navControllerSignOut)
            }
        }) {
            Text("Sign out")
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    Text("Pending Friend Requests")
                }
            }

            Text("My Friends")
            LazyColumn(
                modifier = Modifier.weight(1.0f, true)
            ) {
                val friends = friendItems.value
                if (friends != null) {
                    items(friends.size) {
                        FriendListItem(friends[it])
                    }
                }
            }
        }
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
                    text ="${achievements[it].name})",
                )
                Text(
                    modifier = Modifier.padding(all = 12.dp),
                    text = "${achievements[it].description}"
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
private fun FriendListItem(
    friend: User,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement  = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(friend.username)
        Divider(
            modifier = Modifier
                .height(30.dp)
                .width(1.dp),
            color = Color.Gray,
        )
        Text(text = "oh yeah")
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
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .padding(all = UIConstants.LARGE_PADDING)
                .background(shape = RoundedCornerShape(12.dp), color = Color.White)
        ) {
            Column {
                Text("Send Friend Request")
                TextField(
                    value = usernameState,
                    onValueChange = { usernameState = it },
                    placeholder = { Text("Friend's username") }
                )
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
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .padding(all = UIConstants.LARGE_PADDING)
                .background(shape = RoundedCornerShape(12.dp), color = Color.White)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pending Friend Requests")
                LazyColumn(
                    modifier = Modifier.height(150.dp)
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
                if (
                    pendingFriendReqItems.value == null ||
                    pendingFriendReqItems.value!!.size == 0
                ) {
                    Text("No pending friend requests. You're all caught up!")
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