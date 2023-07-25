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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.R
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Comment
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
    object MyProfile : SocialNavigationItem(
        name = "MyProfile",
        route = "myprofile",
    )
    object ViewComments : SocialNavigationItem(
        name = "ViewComments",
        route = "view comments",
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
//    val dummyUserId = "U47K9DYLoJLJlXHZrU7l"
//    val dummyUsername = "Dylan Xiao"
//    val dummyUserId = "XQIfyBwIwQKyAfiIDKggy"
//    val dummyUsername = "Simhon Chourasia"

//    var currUserId = dummyUserId
//    var currUsername = dummyUsername
//    var currUser = SessionInfo.currentUser
//    if (currUser != null) {
//        currUserId = currUser.id
//    }
    var currentPostId = remember { mutableStateOf("") }

    val feedItems = viewModel
        .getFeedForUser(SessionInfo.currentUserID)
    val myPostItems = viewModel
        .getUsersPosts(SessionInfo.currentUserID)
    val pendingFriendReqItems = viewModel
        .getPendingFriendReqs(SessionInfo.currentUserID)
        .observeAsState()
    val friendItems = viewModel
        .getFriendsForUser(SessionInfo.currentUserID)
        .observeAsState()
    val fetchComments = viewModel
        .getCommentsForPost(currentPostId.value)
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate(SocialNavigationItem.CreatePost.route)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Create Post",
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate(SocialNavigationItem.FriendList.route)
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_group_24),
                                contentDescription = "Friend List",
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate(SocialNavigationItem.MyProfile.route)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "My Profile"
                            )
                        }
                        IconButton(
                            onClick = { coroutineScope.launch {
                                viewModel.onSignOutClick(navControllerSignOut)
                            }},
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Sign Out"
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        var posts =  feedItems
                        if (posts != null) {
                            items(posts.size) {
                                SocialPost(posts[it], navController, currentPostId)
                            }
                        }
                    }
                    if (feedItems == null || feedItems.size == 0) {
                        Text("No posts found... try adding some friends")
                    }
                }
            }
            composable(SocialNavigationItem.MyProfile.route) {
                ProfileScreen()
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
                                SocialPost(myPosts[it], navController, currentPostId)
                            }
                        }
                    }
                }
            }
            composable(SocialNavigationItem.CreatePost.route) {
                CreatePostModal(
                    viewModel,
                    navController,
                    SessionInfo.currentUserID,
                    SessionInfo.currentUsername,
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
                SendFriendReqModal(SessionInfo.currentUserID, viewModel, navController)
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
                                PendingFriendReqItem(viewModel, SessionInfo.currentUserID, pending[it])
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
            composable(SocialNavigationItem.ViewComments.route) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Comments")
                    var textContentState by remember { mutableStateOf("") }
                    LazyColumn(
                        modifier = Modifier.weight(0.7f, true)
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
                    Row(
                        modifier = Modifier.weight(0.3f, true)
                    ) {
                        TextField(
                            value = textContentState,
                            onValueChange = { textContentState = it },
                            label = { Text("") }
                        )
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
                    }
                }
            }
        }
    }
}


//@Composable
//private fun Feed(
//    posts: MutableList<Post>?,
//) {
//    LazyColumn(
//
//    ) {
//        if (posts != null) {
//            items(posts.size) {
//                SocialPost(posts[it])
//            }
//        }
//    }
//}

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
private fun SocialPost(
    post: Post,
    navController: NavHostController,
    currentPostId: MutableState<String>,
) {
    Column(

    ) {
        var timeDiffMsg = getTimeDiffMessage(post.timestamp)

        Text(
            modifier = Modifier.height(20.dp),
            fontSize = 12.sp,
            color = Color.Gray,
            text="${post.username} posted $timeDiffMsg ago"
        )
        Text(
            modifier = Modifier.padding(all = 12.dp),
            text = "${post.textContent}"
        )
        ClickableText(
            style = TextStyle(
                color = Color.LightGray,
            ),
            text = AnnotatedString("${post.comments.size} comment(s)"),
            onClick = {
                currentPostId.value = post.id
                navController.navigate(SocialNavigationItem.ViewComments.route)
            }
        )
        Divider(
            thickness = 1.dp,
            color = Color.Gray,
        )
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

@Composable
private fun CommentText(comment: Comment){
    var timeDiffMsg = com.example.dingo.model.service.impl.getTimeDiffMessage(comment.timestamp)
    Text(
        modifier = Modifier.height(20.dp),
        fontSize = 10.sp,
        color = Color.Gray,
        text="${comment.authorName} posted $timeDiffMsg ago")
    Text(
        modifier = Modifier.padding(all = 12.dp),
        text = "${comment.textContent}"
    )

    Divider(
        thickness = 1.dp,
        color = Color.Gray,
    )
}
