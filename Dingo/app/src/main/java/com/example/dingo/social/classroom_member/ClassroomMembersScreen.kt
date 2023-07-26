package com.example.dingo.social.classroom_member

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.dingo.CustomDialog
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.AccountType
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.ui.theme.color_primary
import com.example.dingo.ui.theme.color_on_secondary
import com.example.dingo.ui.theme.color_primary
import com.example.dingo.ui.theme.color_secondary

@Composable
fun ClassroomMembersScreen(
    classroomId: MutableState<String>,
    viewModel: ClassroomMemberViewModel = hiltViewModel()
) {
    var fetchTeachers = viewModel
        .getUsersOfType(classroomId.value, UserType.TEACHER)
        .observeAsState()
    var fetchStudents = viewModel
        .getUsersOfType(classroomId.value, UserType.STUDENT)
        .observeAsState()
    var addNewMemberDialog = remember { mutableStateOf(false) }
    if (addNewMemberDialog.value) {
        AddMemberDialog(
            viewModel,
            classroomId.value,
        ) {
            addNewMemberDialog.value = false
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        if (SessionInfo.currentUser != null) {
            if (SessionInfo.currentUser!!.accountType == AccountType.INSTRUCTOR) {
                Button(
                    onClick = {
                        addNewMemberDialog.value = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary),
                ) {
                    Text("Add Students", color = color_primary)
                }
            }
        }
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

@Composable
private fun MemberList(
    users: MutableList<User>?,
    userType: UserType,
) {
    LazyColumn() {
        if (users != null) {
            items(users.size) {
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
                    Text(users[it].email)
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = Color.Gray,
                    )
                    Text(text = userTypeStr)
                }
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    viewModel: ClassroomMemberViewModel = hiltViewModel(),
    classroomId: String,
    onDismissRequest : () -> Unit,
) {
    var textContentState by remember { mutableStateOf("") }
    CustomDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val currentContext = LocalContext.current
            Text(text = "Add Student",
                fontSize = UIConstants.SUBTITLE1_TEXT,
                color = color_primary
            )
            TextField(
                modifier = Modifier
                    .padding(vertical = UIConstants.MEDIUM_PADDING),
                singleLine = true,
                value = textContentState,
                onValueChange = { textContentState = it },
                label = { Text("")}
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val ok = viewModel.addStudent(classroomId, textContentState)
                        if (ok) {
                            Toast.makeText(
                                currentContext,
                                "Added student!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismissRequest()
                        } else {
                            Toast.makeText(
                                currentContext,
                                "Couldn't find student...",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary),
                ) {
                    Text(text = "Add", color=color_primary)
                }
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary),
                ) {
                    Text(text = "Cancel", color=color_primary)
                }
            }
        }
    }
}
