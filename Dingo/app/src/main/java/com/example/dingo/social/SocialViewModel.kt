package com.example.dingo.social

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.dingo.MainActivity
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.StatName
import com.example.dingo.common.incrementStat
import com.example.dingo.common.isValidEmail
import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.Comment
import com.example.dingo.model.Post
import com.example.dingo.model.PostComparator
import com.example.dingo.model.PostType
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.UserService
import com.example.dingo.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.PriorityQueue
import javax.inject.Inject

@HiltViewModel
class SocialViewModel
@Inject
constructor(
    private val userService: UserService,
    private val postService: PostService,
    private val accountService: AccountService,
) : ViewModel() {

    suspend fun onSignOutClick(navController: NavHostController) {
        Log.d("STATE", "signing out")
        val successfulSignOut = accountService.signOut();
        if (successfulSignOut) {
            navController.navigate(Screen.LoginScreen.route)
        }
    }

    fun makePost(
        userId: String,
        username: String,
        textContent: String,
        entryIds: List<String>,
        tripId: String?
    ) {
        viewModelScope.launch {
            var postId = postService.createPost(
                userId,
                username,
                entryIds,
                tripId,
                textContent,
            )

            var user = userService.getUser(userId)

            println("making post with post id $postId for user: $user")

            if (user != null) {
                val currPostHead = user.postHead
                if (currPostHead.isNotEmpty()) {
                    postService.setPostNext(currPostHead, postId)
                }
                postService.setPostPrev(postId, currPostHead)
                userService.setPostHeadForUser(userId, postId, PostType.SOCIAL_POST)
            } else {
                println("Could not find user with user id $userId when making post")
            }

        }
        incrementStat(StatName.NUM_SOCIAL_POSTS)
    }

    fun getFeedForUser(userId: String, limit: Int = 10): MutableList<Post> {
        var ret = mutableListOf<Post>()

        runBlocking {
            var user = withContext(Dispatchers.Default) {
                userService.getUser(userId)
            }

            if (user != null) {
                val postQueueByTimestamp = PriorityQueue(PostComparator)
                var friendsAndMe = user.friends + listOf(userId)
                for (friendId in friendsAndMe) {
                    val friend = withContext(Dispatchers.Default) {
                        userService.getUser(friendId)
                    }
                    if (friend != null) {
                        if (friend.postHead != "") {
                            val friendPost = withContext(Dispatchers.Default) {
                                postService.getPost(friend.postHead)
                            }
                            if (friendPost != null) {
                                postQueueByTimestamp.add(friendPost)
                            }

                        }
                    }
                }

                var currFeedLength = 0
                while (postQueueByTimestamp.isNotEmpty() && currFeedLength < limit) {
                    val toAdd = postQueueByTimestamp.remove()
                    ret.add(toAdd)
                    currFeedLength++
                    if (toAdd.prevPost != "") {
                        val prevFriendPost = withContext(Dispatchers.Default) {
                            postService.getPost(toAdd.prevPost)
                        }
                        if (prevFriendPost != null) {
                            postQueueByTimestamp.add(prevFriendPost)
                        }
                    }
                }
            }
        }

        return ret
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
                println("$e")
            }
        }
    }

    fun makeComment(
        postId: String,
        textContent: String,
    ) {
        viewModelScope.launch {
            postService.addComment(postId, SessionInfo.currentUsername, textContent)
        }
        incrementStat(StatName.NUM_COMMENTS)
    }

    fun getUsersPosts(userId: String): LiveData<MutableList<Post>?> {
        return liveData(Dispatchers.IO) {
            try {
                userService.getUsersPosts(userId).collect {
                    if (it != null) {
                        val posts = it
                        emit(posts)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("Error in getting user's own posts: $e")
            }
        }
    }

    fun getFriendsForUser(userId: String): LiveData<MutableList<User>?> {
        return liveData(Dispatchers.IO) {
            try {
                userService.getFriends(userId).collect {
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

    fun sendFriendReq(senderId: String, receiverName: String): Boolean {
        var receiverUser: User? = null
        var friendReqOk: Boolean = false
        runBlocking {
            receiverUser = userService.getUserByUsername(receiverName)
        }
        runBlocking{
            if (receiverUser != null) {
                friendReqOk = userService.sendFriendReq(senderId, receiverUser!!.id)
            }
        }
        return friendReqOk
    }

    fun acceptFriendReq(senderId: String, receiverId: String): String {
        var msg: String = "Something went wrong..."
        runBlocking{
            msg = userService.acceptFriendReq(senderId, receiverId)
        }
        incrementStat(StatName.NUM_FRIENDS_ACCEPTED)
        return msg
    }

    fun declineFriendReq(senderId: String, receiverId: String): String {
        var msg: String = "Something went wrong..."
        runBlocking{
            msg = userService.declineFriendReq(senderId, receiverId)
        }
        incrementStat(StatName.NUM_FRIENDS_DECLINED)
        return msg
    }

    fun getPendingFriendReqs(userId: String): LiveData<MutableList<User>?>{
        return liveData(Dispatchers.IO) {
            try {
                userService.getPendingFriendReqs(userId).collect {
                    if (it != null) {
                        val pending = it
                        emit(pending)
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