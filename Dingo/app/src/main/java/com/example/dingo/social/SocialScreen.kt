package com.example.dingo.social

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.google.firebase.Timestamp
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun SocialScreen(
    viewModel: SocialViewModel = hiltViewModel()
) {
    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX" // eric shang
    val feedItems = viewModel
        .getFeedForUser(dummyUserId)
    val friendItems = viewModel
        .getFriendsForUser(dummyUserId)
        .observeAsState()

    var showFeed by remember { mutableStateOf(true) }
    var makingPost by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showFeed = true },
            ) {
                Text(text = "Classroom Feed")
            }
            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = Color.Gray,
            )
            Button(
                onClick = { showFeed = false }
            ) {
                Text(text = "My Friends")
            }
        }

        // COMMENT THIS OUT
//        Button(
//            onClick = {
//                viewModel.makeDummyPosts()
//            }
//        ) {
//            Text(text = "add dummy user data")
//        }
        // SEE ABOVE

        if (showFeed) {
            CreatePostButton()
            if (makingPost) {

            } else {
                Feed(feedItems)
            }
        } else {
            FriendList(friendItems.value)
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
private fun FriendList(
    friends: MutableList<User>?,
) {
    LazyColumn(

    ) {
        if (friends != null) {
            items(friends.size) {
                FriendListItem(friends[it])
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
private fun CreatePostButton() {
    FloatingActionButton(onClick = {
        println("yummy in my tummy")
    },
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add Social Post",
            tint = Color.White,
        )
    }
}
