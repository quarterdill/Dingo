package com.example.dingo.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.AccountType
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ClassroomViewModel
@Inject
constructor(
    private val classroomService: ClassroomService,
    private val userService: UserService,
    private val postService: PostService,
) : ViewModel() {

     fun getClassroomFeed(classroomId: String): LiveData<MutableList<Post>?> {
        return liveData(Dispatchers.IO) {
            try {
                classroomService.getPostFeed(classroomId, 50).collect {
                    if (it != null) {
                        val posts = it
                        emit(posts)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("$e")
            }
        }
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
    fun addDummyUsers() {
        val userList = getDummyUsers()

        for (userPair in userList) {
            viewModelScope.launch {
                userService.createUser(userPair.first, userPair.second, AccountType.EDUCATION)
            }
        }
    }

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

    fun makePost(
        classroomId: String,
        userId: String,
        username: String,
        textContent: String,
        entryIds: List<String>,
        tripId: String?
    ) {
        viewModelScope.launch {
            val postId = postService.createPost(
                userId,
                username,
                entryIds,
                tripId,
                textContent,
                classroomId,
            )
            println("in viewmodel scope, postid is $postId")
            classroomService.addPost(classroomId, postId)
            userService.addClassroomPost(userId, postId)
        }

    }


    fun makeDummyPosts() {
        val dummyClassroomId = "cE1sLWEWj31aFO1CxwZB"

        viewModelScope.launch {
            val user = userService.getUserByEmail("e2shang@uwaterloo.ca")
            if (user != null) {
                val post = postService.createPost(
                    user.id,
                    user.username,
                    emptyList(),
                    null,
                    "Hi everyone this is my first post!"
                )
                classroomService.addPost(dummyClassroomId, post)
                println("successfully made post")
            }
        }

        viewModelScope.launch {
            val user = userService.getUserByEmail("a62lin@uwaterloo.ca")
            if (user != null) {
                val post = postService.createPost(
                    user.id,
                    user.username,
                    emptyList(),
                    null,
                    "Look at this cool bird I found"
                )
                classroomService.addPost(dummyClassroomId, post)
                println("successfully made post")
            }
        }

        viewModelScope.launch {
            val user = userService.getUserByEmail("d29xiao@uwaterloo.ca")
            if (user != null) {
                val post = postService.createPost(
                    user.id,
                    user.username,
                    emptyList(),
                    null,
                    "I don't like nature"
                )
                classroomService.addPost(dummyClassroomId, post)
                println("successfully made post")
            }
        }
    }

    fun getUsersOfType(classroomId: String, userType: UserType): LiveData<MutableList<User>?> {
        return liveData(Dispatchers.IO) {
            try {
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
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("$e")
            }
        }
    }

    fun getCommentsForPost(postId: String): LiveData<MutableList<Comment>?> {
        return liveData(Dispatchers.IO) {
            try {
                postService.getComments(postId, 50).collect {
                    if (it != null) {
                        emit(it)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("$e")
            }
        }
    }


}