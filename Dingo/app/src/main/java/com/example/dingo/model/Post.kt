package com.example.dingo.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.DocumentId
import java.time.LocalDateTime
import java.time.Month

data class Comment (
    var textContent: String = "",
    var timestamp: LocalDateTime = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0),
)

data class Post constructor(
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var entryIds: List<String> = emptyList(),
    var tripId: String? = null,
    var textContent: String = "",
    var comments: List<Comment> = emptyList(),
//    var timestamp: LocalDateTime = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0),
    var timestamp: LocalDateTime? = null,
)