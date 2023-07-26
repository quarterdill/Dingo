package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

enum class UserType {
    STUDENT, TEACHER
}

data class Classroom (
    @DocumentId val id: String = "",
    var name: String = "",
    var teachers: MutableList<String> = mutableListOf(),
    var students: MutableList<String> = mutableListOf(),
    var posts: List<String> = emptyList(),
)