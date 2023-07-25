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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Comment
import com.example.dingo.trips.TripScreen
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.impl.getTimeDiffMessage


sealed class ClassroomNavigationItem(
    val name: String,
    val route: String,
) {
    object SelectClassroom : ClassroomNavigationItem(
        name = "SelectClassroom",
        route = "selectclassroom",
    )
    object ClassroomPostFeed : ClassroomNavigationItem(
        name = "ClassroomPostFeed",
        route = "classroompostfeed",
    )
    object CreatePost : ClassroomNavigationItem(
        name = "CreatePost",
        route = "createpost",
    )
    object MemberList : ClassroomNavigationItem(
        name = "MemberList",
        route = "memberlist",
    )
    object AddMember : ClassroomNavigationItem(
        name = "AddMember",
        route = "addmember",
    )
    object ViewComments : ClassroomNavigationItem(
        name = "ViewComments",
        route = "viewcomments",
    )
    object MyProfile : ClassroomNavigationItem(
        name = "MyProfile",
        route = "myprofile",
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    viewModel: ClassroomViewModel = hiltViewModel()
) {
//    val dummyClassroomId = "cE1sLWEWj31aFO1CxwZB"
//    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX"
//    val dummyUsername = "Eric Shang"
//    val dummyUserId = "U47K9DYLoJLJlXHZrU7l"
//    val dummyUsername = "Dylan Xiao"
    var classroomId = remember { mutableStateOf("") }
    var currentPostId = remember { mutableStateOf("") }

    val fetchClassrooms = viewModel
        .getClassrooms()
        .observeAsState()
    var feedItems = viewModel
        .getClassroomFeed(classroomId.value)
        .observeAsState()
    var fetchTeachers = viewModel
        .getUsersOfType(classroomId.value, UserType.TEACHER)
        .observeAsState()
    var fetchStudents = viewModel
        .getUsersOfType(classroomId.value, UserType.STUDENT)
        .observeAsState()
    var fetchComments = viewModel
        .getCommentsForPost(currentPostId.value)
        .observeAsState()

//    fun updateEverything() {
//        feedItems = viewModel
//            .getClassroomFeed(classroomId)
////            .observeAsState()
//        fetchTeachers = viewModel
//            .getUsersOfType(classroomId, UserType.TEACHER)
////            .observeAsState()
//        fetchStudents = viewModel
//            .getUsersOfType(classroomId, UserType.STUDENT)
////            .observeAsState()
//    }
//
////    @Composable
//    fun updateComments(postId: String) {
//        fetchComments = viewModel
//            .getCommentsForPost(postId)
////            .observeAsState()
//    }

    val navController = rememberNavController()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val fetchClassrooms = viewModel
            .getClassrooms()
            .observeAsState()
        var feedItems = viewModel
            .getClassroomFeed(classroomId.value)
            .observeAsState()
        var fetchTeachers = viewModel
            .getUsersOfType(classroomId.value, UserType.TEACHER)
            .observeAsState()
        var fetchStudents = viewModel
            .getUsersOfType(classroomId.value, UserType.STUDENT)
            .observeAsState()
        var fetchComments = viewModel
            .getCommentsForPost(currentPostId.value)
            .observeAsState()
        NavHost(
            navController = navController,
            startDestination = ClassroomNavigationItem.SelectClassroom.route
        ) {
            composable(ClassroomNavigationItem.SelectClassroom.route) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Choose a classroom")
                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        val classrooms = fetchClassrooms.value
                        if (classrooms != null) {
                            println("num classrooms: ${classrooms.size}")
                            items(classrooms.size) { i ->
                                println("got classroom: ${classrooms[i]}")
                                ClickableText(
                                    style = TextStyle(
                                        color = Color.LightGray,
                                        fontSize = 26.sp,
                                    ),
                                    text = AnnotatedString(classrooms[i].name),
                                    onClick = {
                                        viewModel.classroomId.value = classrooms[i].id
                                        classroomId.value = classrooms[i].id
//                                        updateEverything()
                                        navController.navigate(ClassroomNavigationItem.ClassroomPostFeed.route)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            composable(ClassroomNavigationItem.ClassroomPostFeed.route) {
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
                                navController.navigate(ClassroomNavigationItem.CreatePost.route)
                            },
                        ) {
                            Text("Create Post")
                        }
                        Button(
                            onClick = {
                                navController.navigate(ClassroomNavigationItem.MemberList.route)
                            },
                        ) {
                            Text("Students/Teachers")
                        }
//                        Button(
//                            onClick = {
//                                navController.navigate(ClassroomNavigationItem.MyProfile.route)
//                            },
//                        ) {
//                            Text("MyProfile")
//                        }
                    }
//                COMMENT THIS OUT
//              Button(
//              onClick = {
//                  viewModel.makeDummyPosts()
//              }
//              ) {
//                  Text(text = "add dummy user data")
//              }
//              SEE ABOVE
                    LazyColumn(
                        modifier = Modifier.weight(1.0f, true)
                    ) {
                        var posts = feedItems.value
                        if (posts != null) {
                            items(posts.size) {
                                ClassroomPost(posts[it], navController, viewModel, currentPostId)
                                println("post content: ${posts[it].textContent}")
                            }
                        }
                    }
                }
            }
            composable(ClassroomNavigationItem.CreatePost.route) {
                CreatePostModal(
                    viewModel,
                    navController,
                    classroomId.value,
                    SessionInfo.currentUserID,
                    SessionInfo.currentUsername
                )
            }
            composable(ClassroomNavigationItem.MemberList.route) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    MemberList(fetchTeachers.value, UserType.TEACHER)
                    Divider(
                        modifier = Modifier
                            .height(3.dp)
                            .width(200.dp),
                        color = Color.Gray,
                    )
                    MemberList(fetchStudents.value, UserType.STUDENT)
                }
            }
            composable(ClassroomNavigationItem.AddMember.route) {
                AddMemberModal(viewModel, navController)
            }
            composable(ClassroomNavigationItem.ViewComments.route) {
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
                                        classroomId.value,
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


@Composable
private fun ClassroomPost(
    post: Post,
    navController: NavHostController,
    viewModel: ClassroomViewModel,
    currentPostId: MutableState<String>,
//    updateComments: (String) -> Unit,
//    fetchComments: State<MutableList<Comment>?>,
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
                    navController.navigate(ClassroomNavigationItem.ViewComments.route)
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
private fun MemberList(
    users: MutableList<User>?,
    userType: UserType,
) {
    LazyColumn() {
        if (users != null) {
            items(users.size) {
                ClassroomMemberItem(users[it], userType)
            }
        }
    }
}

@Composable
private fun ClassroomMemberItem(
    user: User,
    userType: UserType,
) {
    val userTypeStr = if (userType == UserType.STUDENT) {
        "Student"
    } else {
        "Teacher"
    }
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement  = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.email)
        Divider(
            modifier = Modifier
                .height(30.dp)
                .width(1.dp),
            color = Color.Gray,
        )
        Text(text = userTypeStr)
        Divider(
            modifier = Modifier
                .height(30.dp)
                .width(1.dp),
            color = Color.Gray,
        )
    }
}

@Composable
private fun CreatePostModal(
    viewModel: ClassroomViewModel = hiltViewModel(),
    navController: NavHostController,
    classroomId: String,
    userId: String,
    username: String,
) {
    var textContentState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Post to Classroom")
        TextField(
            value = textContentState,
            onValueChange = { textContentState = it },
            label = { Text("")}
        )
        Button(
            onClick = {
                navController.navigate(ClassroomNavigationItem.ClassroomPostFeed.route)
            }
        ) {
            Text(text = "Cancel")
        }
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
                navController.navigate(ClassroomNavigationItem.ClassroomPostFeed.route)
            }
        ) {
            Text(text = "Create Post")
        }
    }
}

@Composable
private fun AddMemberModal(
    viewModel: ClassroomViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    Text("Cannot add new members as a student")
}

@Composable
private fun Comments(
    viewModel: ClassroomViewModel,
    postId: String
) {
    val commentList = viewModel.getCommentsForPost(postId).observeAsState()

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
        text = "${comment.textContent}"
    )

    Divider(
        thickness = 1.dp,
        color = Color.Gray,
    )
}