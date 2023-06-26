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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class PostServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) : PostService {

    override suspend fun createPost(
        userId: String,
        username: String,
        entryIds: List<String>,
        tripId: String?,
        textContent: String
    ): Post {
        var post: Post = Post()
        post.userId = userId
        post.username = username
        post.entryIds = entryIds
        post.tripId = tripId
        post.textContent = textContent
        post.timestamp = LocalDateTime.now()

//        var postId = ""
//
//        runBlocking {
//        firestore.collection(POST_COLLECTIONS)
//            .add(post)
//            .addOnSuccessListener {
//                postId = it.id
//            }
//        }
//
//        return postId
        return post
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


    companion object {
        private const val POST_COLLECTIONS = "postCollections"
    }
}