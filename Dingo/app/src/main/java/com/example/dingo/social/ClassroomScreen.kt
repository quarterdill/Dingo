package com.example.dingo.social

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.CustomDialog
import com.example.dingo.CustomSwitch
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.AccountType
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.social.classroom_feed.ClassroomFeedScreen
import com.example.dingo.social.classroom_feed.ClassroomFeedViewModel
import com.example.dingo.social.classroom_member.ClassroomMembersScreen


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
    object MemberList : ClassroomNavigationItem(
        name = "MemberList",
        route = "memberlist",
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    viewModel: ClassroomFeedViewModel = hiltViewModel()
) {
//    val dummyClassroomId = "cE1sLWEWj31aFO1CxwZB"
//    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX"
//    val dummyUsername = "Eric Shang"
//    val dummyUserId = "U47K9DYLoJLJlXHZrU7l"
//    val dummyUsername = "Dylan Xiao"
    var classroomId = remember { mutableStateOf("") }
    val navController = rememberNavController()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val fetchClassrooms = viewModel
            .getClassrooms()
            .observeAsState()
        Box (
            modifier = Modifier.fillMaxWidth()
        ) {
            if (classroomId.value != "") {
                IconButton(
                    onClick = {
                        classroomId.value = ""
                        navController.navigate(ClassroomNavigationItem.SelectClassroom.route)
                    },
                ) {
                    Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "back")
                }
            }
            Text(
                text = "Classroom",
                fontSize = UIConstants.TITLE_TEXT,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (classroomId.value != "") {
            CustomSwitch(
                "Feed", "Members",
                modifier = Modifier.padding(UIConstants.MEDIUM_PADDING),
            ) {
                if (it) {
                    navController.navigate(ClassroomNavigationItem.MemberList.route)
                } else {
                    navController.navigate(ClassroomNavigationItem.ClassroomPostFeed.route)
                }
            }
        }
        NavHost(
            navController = navController,
            startDestination = ClassroomNavigationItem.SelectClassroom.route
        ) {
            composable(ClassroomNavigationItem.SelectClassroom.route) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val classrooms = fetchClassrooms.value
                    var createClassroomDialogState = remember { mutableStateOf(false) }
                    if (createClassroomDialogState.value) {
                        CreateClassroomDialog(
                            viewModel, SessionInfo.currentUserID
                        ) {
                            createClassroomDialogState.value = false
                        }
                    }
                    Text(
                        "Choose a classroom",
                        modifier = Modifier.padding(UIConstants.MEDIUM_PADDING),
                        fontSize = UIConstants.SUBTITLE1_TEXT,
                    )
                    if (SessionInfo.currentUser != null) {
                        if (SessionInfo.currentUser!!.accountType == AccountType.INSTRUCTOR) {
                            Button(
                                onClick = {
                                    createClassroomDialogState.value = true
                                }
                            ) {
                                Text("Create new classroom")
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1.0f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (classrooms.isNullOrEmpty()) {
                            Text("No classrooms available...")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                println("num classrooms: ${classrooms.size}")
                                items(classrooms.size) { i ->
                                    println("got classroom: ${classrooms[i]}")
                                    ClickableText(
                                        modifier = Modifier.padding(UIConstants.SMALL_PADDING),
                                        style = TextStyle(
                                            color = Color.LightGray,
                                            fontSize = 26.sp,
                                        ),
                                        text = AnnotatedString(classrooms[i].name),
                                        onClick = {
                                            viewModel.classroomId.value = classrooms[i].id
                                            classroomId.value = classrooms[i].id
    //                                        updateEverything()
                                            println("testseet pressed")
                                            navController.navigate(ClassroomNavigationItem.ClassroomPostFeed.route)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            composable(ClassroomNavigationItem.ClassroomPostFeed.route) {
                ClassroomFeedScreen(classroomId)
            }
            composable(ClassroomNavigationItem.MemberList.route) {
                ClassroomMembersScreen(classroomId)
            }
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
private fun CreateClassroomDialog (
    viewModel: ClassroomFeedViewModel = hiltViewModel(),
    creatorUserId: String,
    onDismissRequest : () -> Unit,
) {
    var textContentState by remember { mutableStateOf("") }
    CustomDialog(onDismissRequest = onDismissRequest) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Create classroom",
                fontSize = UIConstants.SUBTITLE2_TEXT
            )
            TextField(
                modifier = Modifier
                    .padding(vertical = UIConstants.MEDIUM_PADDING),
                value = textContentState,
                singleLine = true,
                onValueChange = { textContentState = it },
                label = { Text("") }
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        viewModel.createClassroom(
                            creatorUserId,
                            textContentState,
                        )
                        onDismissRequest()
                    }
                ) {
                    Text(text = "Create Classroom")
                }
                Button(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Composable
private fun AddMemberModal(
    viewModel: ClassroomFeedViewModel = hiltViewModel(),
    navController: NavHostController,
    classroomId: String
) {
    var textContentState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val currentContext = LocalContext.current
        Text(text = "Add Student")
        TextField(
            value = textContentState,
            onValueChange = { textContentState = it },
            label = { Text("")}
        )
        Button(
            onClick = {
                val ok = viewModel.addStudent(classroomId, textContentState)
                if (ok) {
                    Toast.makeText(
                        currentContext,
                        "Added student!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        currentContext,
                        "Couldn't find student...",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        ) {
            Text(text = "Add")
        }
    }
}

