package com.example.dingo.social.classroom_feed

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
import com.example.dingo.model.DingoDexEntry
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
import javax.inject.Inject

@HiltViewModel
class ClassroomFeedViewModel
@Inject
constructor(
    private val classroomService: ClassroomService,
    private val userService: UserService,
    private val postService: PostService,
) : ViewModel() {
    val classroomId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getClassrooms(): LiveData<MutableList<Classroom>?> {
        println("getting classrooms for user: ${SessionInfo.currentUserID}")
        println("this user: ${SessionInfo.currentUser}")
        return liveData(Dispatchers.IO) {
            try {
                userService.getClassrooms(SessionInfo.currentUserID, 50).collect {
                    if (it != null) {
                        val classrooms = it
                        emit(classrooms)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("error getting classrooms: $e")
            }
        }
    }

     fun getClassroomFeed(classroomId: String): LiveData<MutableList<Post>?> {
        return liveData(Dispatchers.IO) {
            try {
                if (classroomId == "") {
                    emit(mutableListOf())
                } else {
                    classroomService.getPostFeed(classroomId, 50).collect {
                        if (it != null) {
                            val posts = it
                            emit(posts)
                        } else {
                            emit(null)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("error getting classroom feed: $e")
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

    fun makePost(
        classroomId: String,
        userId: String,
        username: String,
        textContent: String,
        entry: DingoDexEntry?,
        tripId: String?
    ) {
        viewModelScope.launch {
            val postId = postService.createPost(
                userId,
                username,
                entry,
                tripId,
                textContent,
                classroomId,
            )
            println("in viewmodel scope, postid is $postId")
            classroomService.addPost(classroomId, postId)
            userService.addClassroomPost(userId, postId)
        }
        incrementStat(StatName.NUM_CLASSROOM_POSTS)
    }

    fun makeComment(
        classroomId: String,
        postId: String,
        textContent: String,
    ) {
        viewModelScope.launch {
            postService.addComment(postId, SessionInfo.currentUsername, textContent)
        }
        incrementStat(StatName.NUM_COMMENTS)
    }

    fun removePost(
        classroomId: String,
        postId: String,
    ) {
        viewModelScope.launch {
            classroomService.deletePost(classroomId, postId)
        }
    }

    fun removeComment(
        postId: String,
        commentId: String,
    ) {
        viewModelScope.launch {
            postService.deleteComment(postId, commentId)
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
                    null,
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
                    null,
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
                    null,
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
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("error getting users for classroom: $e")
            }
        }
    }

    fun getCommentsForPost(postId: String): LiveData<MutableList<Comment>?> {
        return liveData(Dispatchers.IO) {
            try {
                if (postId == "") {
                    emit(mutableListOf<Comment>())
                } else {
                    postService.getComments(postId, 50).collect {
                        if (it != null) {
                            emit(it)
                        } else {
                            emit(null)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("error getting comments in classroom post: $e")
            }
        }
    }

    fun createClassroom(
        creatorUserId: String,
        classroomName: String,
    ) {
        viewModelScope.launch {
            var newClassroom: Classroom = Classroom()
            newClassroom.name = classroomName
            var classroomId = ""
            var job = launch {
                classroomId = classroomService.addNewClassroom(newClassroom)
            }
            job.join()
            classroomService.addUser(classroomId, creatorUserId, UserType.TEACHER)

        }
    }
}