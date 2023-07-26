package com.example.dingo.social.classroom_member

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.StatName
import com.example.dingo.common.incrementStat
import com.example.dingo.model.Classroom
import com.example.dingo.model.Comment
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ClassroomMemberViewModel
@Inject
constructor(
    private val classroomService: ClassroomService,
    private val userService: UserService,
    private val postService: PostService,
) : ViewModel() {
    val classroomId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    private fun getDummyUsers(): List<Triple<String, String, UserType>> {
        return listOf(
            Triple("Dylan Xiao", "d29xiao@uwaterloo.ca", UserType.STUDENT),
            Triple("Eric Shang", "e2shang@uwaterloo.ca", UserType.STUDENT),
            Triple("Austin Lin", "a62lin@uwaterloo.ca", UserType.STUDENT),
            Triple("Simhon Chourasia", "s2choura@uwaterloo.ca", UserType.STUDENT),
            Triple("Hitanshu Dalwadi", "hmdalwad@uwaterloo.ca", UserType.STUDENT),
            Triple("Eden Chan", "e223chan@uwaterloo.ca", UserType.STUDENT),
            Triple("Philip Chen", "p242chen@uwaterloo.ca", UserType.TEACHER)
        )
    }
//    fun addDummyUsers() {
//        val userList = getDummyUsers()
//
//        for (userPair in userList) {
//            viewModelScope.launch {
//                userService.createUser(userPair.first, userPair.second, AccountType.STUDENT)
//            }
//        }
//    }

    fun addDummyUsersToClassroom() {
        val dummyClassroomId = "cE1sLWEWj31aFO1CxwZB"
        val userList = getDummyUsers()

        println("tryna add dummy users to classroom")

        for (userPair in userList) {
            viewModelScope.launch {
                val user = userService.getUserByEmail(userPair.second)
                if (user != null) {
                    val userId = user.id
                    classroomService.addUser(dummyClassroomId, userId, userPair.third)
                    println("Successfully added user $userId to classroom $dummyClassroomId")
                }
            }
        }
    }

    fun addStudent(classroomId: String, studentName: String): Boolean {
        var studentUser: User? = null
        var studentId = ""
        var ok = true
        runBlocking {
            studentUser = userService.getUserByUsername(studentName)
        }
        if (studentUser == null) {
            ok = false
        } else {
            studentId = studentUser!!.id
        }
        if (!ok) {
            return false
        }
        runBlocking {
            classroomService.addUser(classroomId, studentId, UserType.STUDENT)
        }
        return ok
    }

    fun getUsersOfType(classroomId: String, userType: UserType): LiveData<MutableList<User>?> {
        return liveData(Dispatchers.IO) {
            try {
                if (classroomId == "") {
                    emit(mutableListOf())
                } else {
                    classroomService.getClassroom(classroomId).collect {
                        if (it != null) {
                            val ret = mutableListOf<User>()
                            var userIds = if (userType == UserType.TEACHER) {
                                it.teachers
                            } else {
                                // intentionally default to students
                                it.students
                            }
                            for (userId in userIds) {
                                val user = userService.getUser(userId)
                                if (user != null) {
                                    println("got user ${user.email}")
                                    ret.add(user)
                                }
                            }

                            emit(ret)
                        } else {
                            emit(null)
                        }
                    }
                }
            } catch (e: Exception) {
                // Do nothing
                println("error getting users for classroom: $e")
            }
        }
    }
}