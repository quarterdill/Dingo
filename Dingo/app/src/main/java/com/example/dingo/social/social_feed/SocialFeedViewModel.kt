package com.example.dingo.social.social_feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.StatName
import com.example.dingo.common.incrementStat
import com.example.dingo.common.isValidEmail
import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.Comment
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.PostComparator
import com.example.dingo.model.PostType
import com.example.dingo.model.Trip
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.TripService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.PriorityQueue
import javax.inject.Inject

@HiltViewModel
class SocialFeedViewModel
@Inject
constructor(
    private val userService: UserService,
    private val postService: PostService,
    private val tripService: TripService,

    ) : ViewModel() {

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

            println("making post with post id $postId for user: $user with tripId $tripId")

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
    fun getFeedForUser(userId: String, limit: Int = 10): LiveData<MutableList<Pair<Post, Trip?>>> {
        return liveData(Dispatchers.IO) {
            try {
                userService.getUserFlow(userId).collect {
                    val ret = mutableListOf<Pair<Post,Trip?>>()
                    if (it != null) {
                        val postQueueByTimestamp = PriorityQueue(PostComparator)
                        var friendsAndMe = it.friends + listOf(userId)
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

                            var trip: Trip? = withContext(Dispatchers.Default){
                                tripService.getTrip(toAdd.tripId ?: "")
                            }
                            Log.d("SocialFeedScreen","tripService.getTrip() = ${trip}")
                            Log.d("SocialFeedScreen","toAdd = ${toAdd}")


                            ret.add(Pair(toAdd,trip))
                            Log.d("SocialFeedScreen","ret = ${ret}")

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
                    Log.d("SocialFeedScreen","final ret = ${ret}")

                    emit(ret)
                }
            } catch (e: Exception) {
                Log.d("SocialFeedScreen","final ret error= ${e}")
                emit(mutableListOf())
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
                println("error getting comments for post: $e")
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
}