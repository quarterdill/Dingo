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

class Achievement
@Inject
constructor(
    var userService: UserService,
    val achievementId: Int,
    val name: String,
    val description: String,
    val conditionField: String,
    val conditionValue: Int,
): IObserver {
    override fun update() {
        println("ACHIEVEMENTS: : got update in achievement!")
        val currUser = SessionInfo.currentUser
        if (currUser != null) {
            val currStats = currUser.stats
            if (currStats.containsKey(conditionField)) {
                val statVal = currStats[conditionField]
                println("ACHIEVEMENTS: : relevant stat value: $conditionField is $statVal")
                if (statVal != null && statVal >= conditionValue && !currUser.achievements.contains(achievementId)) {
                    runBlocking {
                        currUser.achievements.add(achievementId)
                        userService.updateStats()
                        println("ACHIEVEMENTS: added achievement: $name")
                        userService.addAchievementForUser(currUser, achievementId)
                    }
                }
            }
        }
    }
}

object AchievementListings {
    var achievementList: List<Achievement> = emptyList()
}
