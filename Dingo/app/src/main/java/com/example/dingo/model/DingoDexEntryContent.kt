package com.example.dingo.model

import android.content.Context
import com.example.dingo.common.SingletonHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

// Achievements are given in the assets/achievements.json folder
// This is an array where each element has the following format:
// {
//    "name": "Dingo Diver",
//    "description": "Make a Dingo account",
//    "conditions": [{"field": "Logins", "value": 1}]
// }
// it will be activated once the condition fields have at least the specified value
// retrieved from the user's stats
data class DingoDexEntryContent (
    var is_fauna: Boolean = false,
    var name: String = "",
    var scientific_name: String = "",
    var description: String = "",
    var default_picture_name: String = "",
)

class DingoDexEntryListings private constructor(context: Context){
    val floraEntryList: MutableList<DingoDexEntryContent> = mutableListOf()
    val faunaEntryList: MutableList<DingoDexEntryContent> = mutableListOf()
    var dingoDexEntryList: List<DingoDexEntryContent> = emptyList()
    // from https://www.bezkoder.com/kotlin-android-read-json-file-assets-gson/
    fun getJsonDataFromAsset(context: Context, fileName: String) {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use {
                it.readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val gson = Gson()
        val listDingoDexEntryType = object : TypeToken<List<DingoDexEntryContent>>() {}.type
        dingoDexEntryList = gson.fromJson(jsonString, listDingoDexEntryType)
        dingoDexEntryList.forEach {
            println(it)
            if (it.is_fauna) {
                faunaEntryList.add(it)
            } else {
                floraEntryList.add(it)
            }
        }
    }
    init {
        println("Initial")
        getJsonDataFromAsset(context, "florafauna.json")
    }

    companion object : SingletonHolder<DingoDexEntryListings, Context>(::DingoDexEntryListings)
}
