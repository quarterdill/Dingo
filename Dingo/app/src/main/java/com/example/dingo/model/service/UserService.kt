package com.example.dingo.model.service

import androidx.lifecycle.LiveData
import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.Entry
import com.example.dingo.model.User
import com.example.dingo.model.Post
import com.example.dingo.model.PostType
import kotlinx.coroutines.flow.Flow

interface UserService {
    suspend fun getUser(userId: String = ""): User?
    suspend fun getUserFlow(userId: String = ""): Flow<User?>
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun sendFriendReq(senderId: String, receiverId: String): Boolean
    suspend fun acceptFriendReq(senderId: String, receiverId: String): String
    suspend fun declineFriendReq(senderId: String, receiverId: String): String
    suspend fun createUser(username: String, email: String, accountType: AccountType): String
    suspend fun updateDingoDex(newEntryId: Int, isFauna: Boolean)
    suspend fun setPostHeadForUser(userId: String, postId: String, postType: PostType)
    suspend fun addClassroomPost(userId: String, postId: String)
    suspend fun getPendingFriendReqs(userId: String): Flow<MutableList<User>?>
    suspend fun getUsersPosts(userId: String): Flow<MutableList<Post>?>
    suspend fun addAchievementForUser(user: User, achievementId: Int)
    suspend fun addTripForUser(user: String, tripId: String)


    suspend fun getCurrentUser()
    suspend fun getClassrooms(userId: String, limit: Int = 10): Flow<MutableList<Classroom>?>
    suspend fun updateStats()
}