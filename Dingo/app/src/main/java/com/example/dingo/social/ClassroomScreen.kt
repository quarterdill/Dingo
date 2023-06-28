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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun ClassroomScreen(
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val dummyClassroomId = "cE1sLWEWj31aFO1CxwZB"
    val feedItems = viewModel.getClassroomFeed(dummyClassroomId).observeAsState()
    val fetchTeachers = viewModel
        .getUsersOfType(dummyClassroomId, UserType.TEACHER)
        .observeAsState()
    val fetchStudents = viewModel
        .getUsersOfType(dummyClassroomId, UserType.STUDENT)
        .observeAsState()

    var showFeed by remember { mutableStateOf(true) }

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
                Text(text = "Class Members")
            }
        }

        // COMMENT THIS OUT
//        Button(
//            onClick = {
//                viewModel.addDummyUsersToClassroom()
//            }
//        ) {
//            Text(text = "add dummy user data")
//        }
        // SEE ABOVE

        if (showFeed) {
            Feed()
        } else {
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

}


@Composable
private fun Feed() {
    Text("this is where the feed should be")
}

private fun getTimeDiffMessage(timestamp: LocalDateTime): String {
    val timeDiff = Duration.between(timestamp, LocalDateTime.now()).toMinutes()
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
private fun ClassroomPost(post: Post) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        if (post.timestamp != null) {
//            var timeDiffMsg = getTimeDiffMessage(post.timestamp!!)
//        }
//
//        Text("${post.username} posted $timeDiffMsg ago")
        Text("${post.textContent}")
    }
}

@Composable
private fun MemberList(
    users: MutableList<User>?,
    userType: UserType,
) {
    LazyColumn(

    ) {
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