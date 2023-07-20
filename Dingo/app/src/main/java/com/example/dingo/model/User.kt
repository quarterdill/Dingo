package com.example.dingo.model

import android.content.Context
import com.google.firebase.firestore.DocumentId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

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
    var friends: List<String> = emptyList(),
    var classroomIds: List<String> = emptyList(),
    var classroomPosts: List<String> = emptyList(),
    var postHead: String = "",
    var trips: List<String> = emptyList(),
    var uncollectedFauna : List<String> = emptyList(),
    var uncollectedFlora : List<String> = emptyList(),
    var stats: Map<String, Int> = emptyMap(),
    var achievements: List<Int> = emptyList() // list of achievement ids
)