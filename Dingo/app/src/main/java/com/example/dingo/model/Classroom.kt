package com.example.dingo.model
enum class UserType {
    STUDENT, TEACHER
}

class Classroom (
    var teachers: List<String>,
    var students: List<String>,
    var posts: List<String>,
)