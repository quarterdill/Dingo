package com.example.dingo.model.service

import com.example.dingo.model.Post
import kotlinx.coroutines.flow.Flow

interface TripService {
    // returns post id
    suspend fun createPost(
        userId: String,
        username: String,
        entryIds: List<String>,
        tripId: String?,
        textContent: String,
        classroomId: String? = null,
    ): String

    suspend fun getPost(postId: String): Post?

    suspend fun getPostFlow(postId: String): Flow<Post?>

    suspend fun deletePost(postId: String)

    suspend fun setPostPrev(postId: String, prevPostId: String)
    suspend fun setPostNext(postId: String, nextPostId: String)
}