package com.example.dingo.model.service.impl

import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.Comment
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.UserService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.Duration
import javax.inject.Inject

class PostServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) : PostService {

    override suspend fun createPost(
        userId: String,
        username: String,
        entry: DingoDexEntry?,
        tripId: String?,
        textContent: String,
        classroomId: String?,
    ): String {
        var post: Post = Post()
        post.userId = userId
        post.username = username
        if (entry != null) {
            post.entryId = entry.id
        }
        post.tripId = tripId
        post.textContent = textContent
        post.timestamp = Timestamp.now()
        post.classroomId = classroomId

        var postId = ""

        firestore.collection(POST_COLLECTIONS)
            .add(post)
            .addOnSuccessListener {
                postId = it.id
            }
            .addOnFailureListener {e ->
                println("Error adding Post document: $e")
            }
            .await()

        if (postId.isEmpty()) {
            println("empty post id for post; early exiting")
        }

        return postId
    }

    override suspend fun getPost(postId: String): Post? {
        return firestore.collection(POST_COLLECTIONS)
            .document(postId)
            .get()
            .await()
            .toObject(Post::class.java)
    }

    override suspend fun getPostFlow(postId: String): Flow<Post?> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePost(postId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setPostPrev(postId: String, prevPostId: String) {
        println("setting post prev for post id $postId")
        firestore.collection(POST_COLLECTIONS)
            .document(postId)
            .update("prevPost", prevPostId)
            .await()
    }

    override suspend fun setPostNext(postId: String, nextPostId: String) {
        firestore.collection(POST_COLLECTIONS)
            .document(postId)
            .update("nextPost", nextPostId)
            .await()
    }

    override suspend fun getComments(postId: String, limit: Int): Flow<MutableList<Comment>?> {
        return callbackFlow {
            if (postId == "") {
                trySend(mutableListOf<Comment>())
            } else {
                val postCollection = firestore.collection(POST_COLLECTIONS)
                    .document(postId)
                val subscription = postCollection.addSnapshotListener { snapshot, e ->
                    if (snapshot == null) {
                        trySend(null)
                    } else if (snapshot!!.exists()) {
                        var post = snapshot.toObject(Post::class.java)
                        var limiter = 0
                        var ret: MutableList<Comment> = mutableListOf<Comment>()
                        if (post != null) {
                            for (commentId in post.comments.reversed()) {
                                if (limiter > limit) {
                                    break
                                }
                                limiter++

                                var comment: Comment? = null

                                runBlocking {
                                    comment = firestore.collection(COMMENT_COLLECTIONS)
                                        .document(commentId)
                                        .get()
                                        .await()
                                        .toObject(Comment::class.java)
                                }

                                if (comment != null) {
                                    ret.add(comment!!)
                                } else {
                                    println("comment $commentId not found!?")
                                }
                            }
                        }
                        trySend(ret)
                    }
                }
                awaitClose { subscription.remove() }
            }
        }
    }
    override suspend fun addComment(postId: String, username: String, commentText: String) {
        var commentId = ""

        val comment: Comment = Comment()
        comment.authorName = username
        comment.textContent = commentText
        comment.timestamp = Timestamp.now()

        firestore.collection(COMMENT_COLLECTIONS)
            .add(comment)
            .addOnSuccessListener {
                commentId = it.id
            }
            .addOnFailureListener {e ->
                println("Error adding Post document: $e")
            }
            .await()

        if (commentId.isEmpty()) {
            println("Error: empty comment id found")
        }

        firestore.collection(POST_COLLECTIONS)
            .document(postId)
            .update("comments", FieldValue.arrayUnion(commentId))
            .addOnSuccessListener {
                println("Successfully added comment id $commentId with test $commentText to post $postId")
            }
            .addOnFailureListener {e ->
                println("Error in adding comment to post: $e")
            }
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        firestore.collection(POST_COLLECTIONS)
            .document(postId)
            .update("comments", FieldValue.arrayRemove(commentId))
            .addOnSuccessListener {
                println("Successfully removed comment id $commentId from post $postId")
            }
            .addOnFailureListener {e ->
                println("Error in removing comment from post: $e")
            }

        firestore.collection(COMMENT_COLLECTIONS)
            .document(commentId)
            .delete()
            .addOnSuccessListener {
                println("Successfully deleted comment!")
            }
            .addOnFailureListener {e ->
                println("Error in deleting comment document: $e")
            }
    }


    companion object {
        private const val POST_COLLECTIONS = "postCollections"
        private const val COMMENT_COLLECTIONS = "commentCollections"
    }
}

fun getTimeDiffMessage(timestamp: Timestamp): String {
    val timeDiff = (Timestamp.now().seconds - timestamp.seconds) / 60
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