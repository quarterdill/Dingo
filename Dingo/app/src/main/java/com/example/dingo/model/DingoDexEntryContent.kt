package com.example.dingo.model

data class DingoDexEntryContent (
    var id: Int = 0,
    var is_fauna: Boolean = false,
    var name: String = "",
    var scientific_name: String = "",
    var description: String = "",
    var default_picture_name: String = "",
)
object DingoDexScientificToIndex {
    val dingoDexFaunaScientificToIndex: MutableMap<String, Int> = mutableMapOf()
    val dingoDexFloraScientificToIndex: MutableMap<String, Int> = mutableMapOf()
}


object DingoDexEntryListings {
    val floraEntryList: MutableList<DingoDexEntryContent> = mutableListOf()
    val faunaEntryList: MutableList<DingoDexEntryContent> = mutableListOf()
    var dingoDexEntryList: List<DingoDexEntryContent> = emptyList()
}
