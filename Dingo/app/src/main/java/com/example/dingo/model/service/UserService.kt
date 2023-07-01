package com.example.dingo.model.service

import com.example.dingo.model.AccountType
import com.example.dingo.model.Entry
import com.example.dingo.model.User
import com.example.dingo.model.Post
import kotlinx.coroutines.flow.Flow

interface UserService {
    suspend fun getUser(userId: String): User?
    suspend fun getUserFlow(userId: String = ""): Flow<User?>
    suspend fun getUserByEmail(email: String): User?
    suspend fun sendFriendReq(senderId: String, receiverId: String)
    suspend fun acceptFriendReq(senderId: String, receiverId: String)
    suspend fun declineFriendReq(senderId: String, receiverId: String)
    suspend fun createUser(username: String, email: String, accountType: AccountType)
    suspend fun updateDingoDex(userId: String, uncollected: List<String>, isFauna: Boolean)

}