package com.example.dingo.model

import android.content.Context
import android.se.omapi.Session
import com.example.dingo.common.IObserver
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.SingletonHolder
import com.example.dingo.model.service.UserService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject

// Achievements are given in the assets/achievements.json folder
// This is an array where each element has the following format:
// {
//    "name": "Dingo Diver",
//    "description": "Make a Dingo account",
//    "condition_field": "Logins",
//    "condition_value": 1
// }
// it will be activated once the condition field has at least the specified value
// retrieved from the user's stats

data class Achievement
@Inject
constructor(
    val achievementId: Int,
    val name: String,
    val description: String,
    val conditionField: String,
    val conditionValue: Int,
    private val userService: UserService,
): IObserver {
    override fun update() {
        val currUser = SessionInfo.currentUser
        if (currUser != null) {
            val currStats = currUser.stats
            if (currStats.containsKey(conditionField)) {
                val statVal = currStats[conditionField]
                if (statVal != null && statVal >= conditionValue) {
                    runBlocking {
                        currUser.achievements.add(achievementId)
                        userService.addAchievementForUser(currUser, achievementId)
                    }
                }
            }
        }
    }
}

class AchievementListings private constructor(context: Context) {
    var achievementList: List<Achievement> = emptyList()
    // from https://www.bezkoder.com/kotlin-android-read-json-file-assets-gson/
    private fun getJsonDataFromAsset(context: Context, fileName: String) {
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
        achievementList = gson.fromJson(jsonString, listAchievementType)
        achievementList.forEach {
            println(it)
        }

    }
    init {
        println("Initialized achivements")
        getJsonDataFromAsset(context, "achievements.json")
    }

    companion object : SingletonHolder<AchievementListings, Context>(::AchievementListings)
}
