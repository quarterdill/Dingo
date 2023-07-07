package com.example.dingo.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.time.Month

data class Comment (
    var textContent: String = "",
    var timestamp: Timestamp = Timestamp.now(),
)

enum class PostType {
    SOCIAL_POST, CLASSROOM_POST
}

data class Post constructor(
    @DocumentId val id: String = "",
    var userId: String = "",
    var username: String = "",
    var entryIds: List<String> = emptyList(),
    var tripId: String? = null,
    var textContent: String = "",
    var comments: List<Comment> = emptyList(),
    var timestamp: Timestamp = Timestamp.now(),
    var nextPost: String = "",
    var prevPost: String = "",
    var classroomId: String? = null,
)

val PostComparator = Comparator { post1: Post, post2: Post ->
    post2.timestamp.compareTo(post1.timestamp)
}
