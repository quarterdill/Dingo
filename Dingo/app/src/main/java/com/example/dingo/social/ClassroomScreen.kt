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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.NavBarItem
import com.example.dingo.NoPermission
import com.example.dingo.ScannerScreen
import com.example.dingo.TripsScreen
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.impl.getTimeDiffMessage
import com.google.firebase.Timestamp
import java.time.Duration
import java.time.LocalDateTime

sealed class ClassroomNavigationItem(
    val name: String,
    val route: String,
) {
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val dummyClassroomId = "cE1sLWEWj31aFO1CxwZB"
//    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX"
//    val dummyUsername = "Eric Shang"
    val dummyUserId = "U47K9DYLoJLJlXHZrU7l"
    val dummyUsername = "Dylan Xiao"

    val feedItems = viewModel
        .getClassroomFeed(dummyClassroomId)
        .observeAsState()
    val fetchTeachers = viewModel
        .getUsersOfType(dummyClassroomId, UserType.TEACHER)
        .observeAsState()
    val fetchStudents = viewModel
        .getUsersOfType(dummyClassroomId, UserType.STUDENT)
        .observeAsState()

    val navController = rememberNavController()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(
            navController = navController,
            startDestination = ClassroomNavigationItem.ClassroomPostFeed.route
        ) {
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
                        var posts =  feedItems.value
                        if (posts != null) {
                            items(posts.size) {
                                ClassroomPost(posts[it])
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
                    dummyClassroomId,
                    dummyUserId,
                    dummyUsername
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
        }
    }

}


@Composable
private fun ClassroomPost(post: Post) {
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
