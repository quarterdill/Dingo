package com.example.dingo.model.service.impl

import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
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
        entryIds: List<String>,
        tripId: String?,
        textContent: String,
        classroomId: String?,
    ): String {
        var post: Post = Post()
        post.userId = userId
        post.username = username
        post.entryIds = entryIds
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


    companion object {
        private const val POST_COLLECTIONS = "postCollections"
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