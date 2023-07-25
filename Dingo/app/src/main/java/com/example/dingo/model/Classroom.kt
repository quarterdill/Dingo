package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

enum class UserType {
    STUDENT, TEACHER
}

data class Classroom (
    @DocumentId val id: String = "",
    var name: String = "",
    var teachers: List<String> = emptyList(),
    var students: List<String> = emptyList(),
    var posts: List<String> = emptyList(),
)