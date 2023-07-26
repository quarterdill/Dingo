package com.example.dingo.social.social_feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.CustomDialog
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Comment
import com.example.dingo.model.Post
import com.google.firebase.Timestamp

@Composable
fun SocialFeedScreen(
    viewModel: SocialFeedViewModel = hiltViewModel(),
) {
    val currentUser = SessionInfo.currentUser
    var currentPostId = remember { mutableStateOf("") }
    val feedItems = viewModel
        .getFeedForUser(SessionInfo.currentUserID)
    var createNewPost = remember { mutableStateOf(false) }
    if (createNewPost.value) {
        CreatePostModal(
            viewModel,
            SessionInfo.currentUserID,
            SessionInfo.currentUsername,
        ) {
            createNewPost.value = false
        }
    }
    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.weight(1.0f, true)
            ) {
                var posts = feedItems
                if (posts != null) {
                    items(posts.size) {
                        SocialPost(posts[it], currentPostId)
                    }
                }
            }
            if (feedItems == null || feedItems.size == 0) {
                Text("No posts found... try adding some friends")
            }
        }
        FloatingActionButton(
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
private fun SocialPost(
    post: Post,
    currentPostId: MutableState<String>,
) {
    var commentDialogState = remember { mutableStateOf(false) }
    if (commentDialogState.value) {
        CommentsDialog(currentPostId) {
            commentDialogState.value = false
        }
    }
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
private fun CreatePostModal(
    viewModel: SocialFeedViewModel = hiltViewModel(),
    userId: String,
    username: String,
    onDismissRequest: () -> Unit,
) {
    CustomDialog(onDismissRequest = onDismissRequest) {
        var textContentState by remember { mutableStateOf("") }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Post",
                fontSize = UIConstants.SUBTITLE2_TEXT
            )
            TextField(
                modifier = Modifier.padding(vertical = UIConstants.MEDIUM_PADDING),
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
                        viewModel.makePost(
                            userId,
                            username,
                            textContentState,
                            mutableListOf<String>(),
                            null,
                        )
                        onDismissRequest()
                    }
                ) {
                    Text(text = "Create Post")
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
private fun CommentsDialog(
    currentPostId: MutableState<String>,
    viewModel: SocialFeedViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
) {
    val fetchComments = viewModel
        .getCommentsForPost(currentPostId.value)
        .observeAsState()
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
                    Text(
                        "No comments yet. Be the first to comment!",
                        textAlign = TextAlign.Center,
                    )
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
                modifier = Modifier.padding(vertical = UIConstants.MEDIUM_PADDING),
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
    var timeDiffMsg = com.example.dingo.model.service.impl.getTimeDiffMessage(comment.timestamp)
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