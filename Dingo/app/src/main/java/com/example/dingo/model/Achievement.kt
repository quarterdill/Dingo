package com.example.dingo.model

import android.content.Context
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

data class Achievement (
    var name: String = "",
    var description: String = "",
    var conditions: List<Pair<String, Int>> = emptyList(),
)

object Achievements {

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
        val listAchievementType = object : TypeToken<List<Achievement>>() {}.type
        var achievementList: List<Achievement> = gson.fromJson(jsonString, listAchievementType)
        achievementList.forEach {
            println(it)
        }

    }
    init {
        println("Initialized user stats")
    }
}
