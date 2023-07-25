package com.example.dingo.social.social_feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Comment
import com.example.dingo.model.Post
import com.example.dingo.model.PostComparator
import com.example.dingo.model.PostType
import com.example.dingo.model.service.PostService
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
    }

}