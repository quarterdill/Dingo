package com.example.dingo.social

import android.util.Log
import android.app.PendingIntent.getActivity
import android.se.omapi.Session
import android.widget.Toast
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.camera.core.impl.utils.ContextUtil.getBaseContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime


sealed class SocialNavigationItem(
    val name: String,
    val route: String,
) {
    object SocialFeed : SocialNavigationItem(
        name = "SocialFeed",
        route = "socialfeed",
    )
    object MyPosts : SocialNavigationItem(
        name = "MyPosts",
        route = "myposts",
    )
    object CreatePost : SocialNavigationItem(
        name = "CreatePost",
        route = "createpost",
    )
    object FriendList : SocialNavigationItem(
        name = "FriendList",
        route = "friendlist",
    )
    object SendFriendReqs : SocialNavigationItem(
        name = "SendFriendReqs",
        route = "sendfriendreqs",
    )
    object AcceptFriendReqs : SocialNavigationItem(
        name = "AcceptFriendReqs",
        route = "acceptfriendreqs",
    )
}

@Composable
fun SocialScreen(
    viewModel: SocialViewModel = hiltViewModel(),
    navControllerSignOut: NavHostController
) {
    val viewModelJob = Job()
    val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
//    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX" // eric shang
//    val dummyUsername = "Eric Shang"
    val dummyUserId = "U47K9DYLoJLJlXHZrU7l"
    val dummyUsername = "Dylan Xiao"
//    val dummyUserId = "XQIfyBwIwQKyAfiIDKggy"
//    val dummyUsername = "Simhon Chourasia"

    var currUserId = dummyUserId
    var currUsername = dummyUsername
//    var currUser = SessionInfo.currentUser
//    if (currUser != null) {
//        currUserId = currUser.id
//    }
    val feedItems = viewModel
        .getFeedForUser(currUserId)
    val myPostItems = viewModel
        .getUsersPosts(currUserId)
    val pendingFriendReqItems = viewModel
        .getPendingFriendReqs(currUserId)
        .observeAsState()
    val friendItems = viewModel
        .getFriendsForUser(currUserId)
        .observeAsState()

    val navController = rememberNavController()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(
            navController = navController,
            startDestination = SocialNavigationItem.SocialFeed.route
        ) {
            composable(SocialNavigationItem.SocialFeed.route) {
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
                                navController.navigate(SocialNavigationItem.CreatePost.route)
                            },
                        ) {
                            Text("Create Post")
                        }
                        Button(
                            onClick = {
                                navController.navigate(SocialNavigationItem.FriendList.route)
                            },
                        ) {
                            Text("My Friends")
                        }
                        Button(
                            onClick = { coroutineScope.launch {
                                viewModel.onSignOutClick(navControllerSignOut)
                            }},
                        ) {
                            Text(text = "Sign Out")
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        var posts =  feedItems
                        if (posts != null) {
                            items(posts.size) {
                                SocialPost(posts[it])
                            }
                        }
                    }
                    if (feedItems == null || feedItems.size == 0) {
                        Text("No posts found... try adding some friends")
                    }
                }
            }
            composable(SocialNavigationItem.MyPosts.route) {
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
                                navController.navigate(SocialNavigationItem.CreatePost.route)
                            },
                        ) {
                            Text("Create Post")
                        }
                        Button(
                            onClick = {
                                navController.navigate(SocialNavigationItem.FriendList.route)
                            },
                        ) {
                            Text("My Friends")
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        var myPosts = myPostItems.value
                        if (myPosts != null) {
                            items(myPosts.size) {
                                SocialPost(myPosts[it])
                            }
                        }
                    }
                }
            }
            composable(SocialNavigationItem.CreatePost.route) {
                CreatePostModal(
                    viewModel,
                    navController,
                    currUserId,
                    currUsername,
                )
            }
            composable(SocialNavigationItem.FriendList.route) {
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
                                navController.navigate(SocialNavigationItem.SendFriendReqs.route)
                            },
                        ) {
                            Text("Send Friend Request")
                        }
                        Button(
                            onClick = {
                                navController.navigate(SocialNavigationItem.AcceptFriendReqs.route)
                            },
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
            }
            composable(SocialNavigationItem.SendFriendReqs.route) {
                SendFriendReqModal(currUserId, viewModel, navController)
            }
            composable(SocialNavigationItem.AcceptFriendReqs.route) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pending Friend Requests")
                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        var pending = pendingFriendReqItems.value
                        if (pending != null) {
                            items(pending.size) {
                                PendingFriendReqItem(viewModel, currUserId, pending[it])
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
}


@Composable
private fun Feed(
    posts: MutableList<Post>?,
) {
    LazyColumn(

    ) {
        if (posts != null) {
            items(posts.size) {
                SocialPost(posts[it])
            }
        }
    }
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

@Composable
private fun SocialPost(post: Post) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var timeDiffMsg = getTimeDiffMessage(post.timestamp)

        Text("${post.username} posted $timeDiffMsg ago")
        Text("${post.textContent}")
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
private fun PendingFriendReqItem(
    viewModel: SocialViewModel,
    userId: String,
    pendingUser: User,
) {
    val currentContext = LocalContext.current
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement  = Arrangement.spacedBy(10.dp),
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


@Composable
private fun CreatePostModal(
    viewModel: SocialViewModel = hiltViewModel(),
    navController: NavHostController,
    userId: String,
    username: String,
) {
    var textContentState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Post")
        TextField(
            value = textContentState,
            onValueChange = { textContentState = it },
            label = { Text("")}
        )

        Button(
            onClick = {
                viewModel.makePost(
                    userId,
                    username,
                    textContentState,
                    mutableListOf<String>(),
                    null,
                )
                navController.navigate(SocialNavigationItem.SocialFeed.route)
            }
        ) {
            Text(text = "Create Post")
        }

        Button(
            onClick = {
                navController.navigate(SocialNavigationItem.SocialFeed.route)
            }
        ) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun SendFriendReqModal(
    userId: String,
    viewModel: SocialViewModel,
    navController: NavHostController,
) {
    val currentContext = LocalContext.current
    var usernameState by remember { mutableStateOf("") }
    Column(

    ) {
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
            onClick = {
                navController.navigate(SocialNavigationItem.FriendList.route)
            }
        ) {
            Text(text = "Back to Friends")
        }

    }
}
