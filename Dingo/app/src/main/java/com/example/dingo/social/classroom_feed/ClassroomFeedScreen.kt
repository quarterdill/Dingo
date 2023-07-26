package com.example.dingo.social.classroom_feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.example.dingo.model.AccountType
import com.example.dingo.model.Comment
import com.example.dingo.model.Post
import com.example.dingo.model.service.impl.getTimeDiffMessage
import com.example.dingo.ui.theme.color_background
import com.example.dingo.ui.theme.color_light_transparent
import com.example.dingo.ui.theme.color_on_primary
import com.example.dingo.ui.theme.color_on_secondary
import com.example.dingo.ui.theme.color_post_background
import com.example.dingo.ui.theme.color_primary
import com.example.dingo.ui.theme.color_secondary
import com.example.dingo.ui.theme.color_text_field


@Composable
fun ClassroomFeedScreen(
    classroomId: MutableState<String>,
    viewModel: ClassroomFeedViewModel = hiltViewModel()
) {
    var currentPostId = remember { mutableStateOf("") }
    val feedItems = viewModel
        .getClassroomFeed(classroomId.value)
        .observeAsState()
    var createNewPost = remember { mutableStateOf(false) }
    if (createNewPost.value) {
        CreatePostDialog(
            viewModel,
            classroomId.value,
            SessionInfo.currentUserID,
            SessionInfo.currentUsername
        ) {
            createNewPost.value = false
        }
    }
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (feedItems.value.isNullOrEmpty()) {
            Text(
                "No posts found... try adding some members",
                color = color_primary
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                var posts = feedItems.value
                if (posts != null) {
                    items(posts.size) {
                        ClassroomPost(posts[it], viewModel, currentPostId, classroomId)
                        println("post content: ${posts[it].textContent}")
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
private fun ClassroomPost(
    post: Post,
    viewModel: ClassroomFeedViewModel,
    currentPostId: MutableState<String>,
    classroomId: MutableState<String>,
) {
    var currUserType = AccountType.STUDENT
    val currUser = SessionInfo.currentUser
    if (currUser != null) {
        currUserType = currUser.accountType
    }
    var commentDialogState = remember { mutableStateOf(false) }
    if (commentDialogState.value) {
        CommentsDialog(
            currentPostId,
            classroomId,
        ) {
            commentDialogState.value = false
        }
    }
    Row(
        modifier = Modifier
            .padding(16.dp)
            .background(color_post_background),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var timeDiffMsg = getTimeDiffMessage(post.timestamp)

        Column {
            Text(
                modifier = Modifier.height(20.dp),
                fontSize = 12.sp,
                color = color_secondary,
                text="${post.username} posted $timeDiffMsg ago"
            )
            Text(
                modifier = Modifier.padding(all = 12.dp),
                text = post.textContent,
                color = color_primary,
            )
            ClickableText(
                style = TextStyle(
                    color = color_secondary,
                ),
                text = AnnotatedString("${post.comments.size} comment(s)"),
                onClick = {
                    currentPostId.value = post.id
                    commentDialogState.value = true
                }
            )
            if (currUserType == AccountType.INSTRUCTOR) {
                IconButton(
                    onClick = {
                        viewModel.removePost(classroomId.value, post.id)
                    },
                    colors = IconButtonDefaults.iconButtonColors(contentColor =  color_primary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Post",
                    )
                }
            }
            Divider(
                thickness = 1.dp,
                color = color_primary,
            )

        }
    }
}

@Composable
private fun CommentsDialog(
    currentPostId: MutableState<String>,
    classroomId: MutableState<String>,
    viewModel: ClassroomFeedViewModel = hiltViewModel(),
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
                color = color_primary,
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
                        color = color_primary,
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
                                CommentText(
                                    comments[it],
                                    currentPostId.value,
                                    viewModel
                                )
                            }
                        }
                    }
                }
            }
            TextField(
                modifier = Modifier
                    .padding(vertical = UIConstants.MEDIUM_PADDING)
                    .background(color = color_text_field)
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
                                classroomId.value,
                                currentPostId.value,
                                textContentState,
                            )
                        }
                        textContentState = ""
                    },
                    colors =
                    ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                ) {
                    Text(text = "Comment")
                }
                Button(
                    onClick = onDismissRequest,
                    colors =
                    ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}


@Composable
private fun CreatePostDialog(
    viewModel: ClassroomFeedViewModel = hiltViewModel(),
    classroomId: String,
    userId: String,
    username: String,
    onDismissRequest : () -> Unit,
) {
    var textContentState by remember { mutableStateOf("") }
    CustomDialog(onDismissRequest = onDismissRequest) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Post to Classroom",
                fontSize = UIConstants.SUBTITLE2_TEXT
            )
            TextField(
                modifier = Modifier
                    .padding(vertical = UIConstants.MEDIUM_PADDING)
                    .background(color = color_text_field)
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
                        viewModel.makePost(
                            classroomId,
                            userId,
                            username,
                            textContentState,
                            mutableListOf<String>(),
                            null,
                        )
                        onDismissRequest()
                    },
                    colors =
                    ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                ) {
                    Text(text = "Create Post")
                }
                Button(
                    onClick = onDismissRequest,
                    colors =
                    ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Composable
private fun CommentText(
    comment: Comment,
    postId: String,
    viewModel: ClassroomFeedViewModel,
){
    var timeDiffMsg = getTimeDiffMessage(comment.timestamp)
    Text(
        modifier = Modifier.height(20.dp),
        fontSize = 10.sp,
        color = color_secondary,
        text="${comment.authorName} posted $timeDiffMsg ago")
    Text(
        modifier = Modifier.padding(all = 12.dp),
        color = color_primary,
        text = comment.textContent
    )

    if (SessionInfo.currentUser!!.accountType == AccountType.INSTRUCTOR) {
        IconButton(
            onClick = {
                viewModel.removeComment(postId, comment.id)
            },
            colors =
            IconButtonDefaults.iconButtonColors(contentColor =  color_primary)
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete Post",
            )
        }
    }

    Divider(
        thickness = 1.dp,
        color = Color.Gray,
    )

}