package com.example.dingo.model.service

import com.example.dingo.model.Entry
import com.example.dingo.model.Post
import kotlinx.coroutines.flow.Flow

interface StorageService {
    val entries: Flow<List<Entry>>
    suspend fun getEntry(entryId: String): Entry?
    suspend fun saveEntry(entry: Entry): String
    suspend fun updateEntry(entry: Entry)
    suspend fun deleteEntry(entryId: String)

    val posts: Flow<List<Post>>
    suspend fun getPost(postId: String): Post?
    suspend fun savePost(post: Post): String
    suspend fun updatePost(post: Post)
    suspend fun deletePost(post: Post)
}