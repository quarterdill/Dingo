package com.example.dingo.model

import com.google.firebase.firestore.DocumentId


data class DingoDexCollection (
    // Make sure ID is the same as the respective userId or have a reference in user data
    @DocumentId val id: String = "",
    // First String to represent ID of the animal/ plant, second int to represent
    //      the number of times the user has seen the animal/plant
    var collectedFauna: List<Map<String, Int>> = emptyList(),
    var collectedFlora: List<Map<String, Int>> = emptyList(),

    // IDs of animal/plants that the user hasn't encountered yet
    var uncollectedFauna : List<String> = emptyList(),
    var uncollectedFlora : List<String> = emptyList(),
)
