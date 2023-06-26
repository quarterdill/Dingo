package com.example.dingo.model

import java.time.LocalDateTime

data class Comment (
    var textContent: String,
    var timestamp: LocalDateTime,
)

data class Post (
    var entries: List<String>,
    var tripId: String,
    var textContent: String,
    var comments: List<Comment>,
    var timestamp: LocalDateTime
)