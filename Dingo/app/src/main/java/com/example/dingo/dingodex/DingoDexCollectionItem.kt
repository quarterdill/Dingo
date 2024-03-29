package com.example.dingo.dingodex

class DingoDexCollectionItem (
    val id: Int = -1,
    val name: String = "",
    val scientificName: String = "",
    val pictureURL: String = "",
    val numEncounters: Int = 0,
    val isFauna: Boolean = true,
    val pictures: MutableList<String> = mutableListOf()
)