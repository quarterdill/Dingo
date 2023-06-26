package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

enum class AccountType {
    STANDARD, EDUCATION
}

data class User (
    @DocumentId val id: String = "",
    var username: String = "",
    var password: String = "",
    var email: String = "",
    var accountType: AccountType = AccountType.STANDARD,
    var dingoDexId: String = "",
    var incomingFriendRequests: List<String>,
    var outgoingFriendRequests: List<String>,
    var classroomId: String = "",
    var posts: List<String>,
    var trips: List<String>
)