package com.example.dingo.model

import com.google.firebase.firestore.DocumentId

enum class AccountType {
    STANDARD, EDUCATION
}

data class User (
    @DocumentId val id: String = "",
    var username: String = "",
    var email: String = "",
    var accountType: AccountType = AccountType.STANDARD,
    var dingoDexId: String = "",
    var incomingFriendRequests: List<String> = emptyList(),
    var outgoingFriendRequests: List<String> = emptyList(),
    var classroomIds: List<String> = emptyList(),
    var posts: List<String> = emptyList(),
    var trips: List<String> = emptyList(),
)