package com.example.dingo.model.service

import com.example.dingo.model.Post
import kotlinx.coroutines.flow.Flow

interface PostService {
    suspend fun createPost(
        userId: String,
        username: String,
        entryIds: List<String>,
        tripId: String?,
        textContent: String,
    ): Post

    suspend fun getPost(postId: String): Post?

    suspend fun getPostFlow(postId: String): Flow<Post?>

    suspend fun deletePost(postId: String)
}