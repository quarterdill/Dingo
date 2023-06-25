package com.example.dingo.model.service

import com.example.dingo.model.User
import com.example.dingo.model.Classroom
import com.example.dingo.model.Post
import com.example.dingo.model.UserType
import kotlinx.coroutines.flow.Flow

interface ClassroomService {
    suspend fun getClassroom(classroomId: String): Flow<Classroom?>
    suspend fun addNewClassroom(newClassroom: Classroom)
    suspend fun addUser(classroomId: String, teacherId: String, userType: UserType)
    suspend fun addPost(classroomId: String, post: Post)
    suspend fun deletePost(postId: String)
    suspend fun getPostFeed(classroomId: String): List<Post>
}