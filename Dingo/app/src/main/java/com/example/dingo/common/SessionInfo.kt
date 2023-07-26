package com.example.dingo.common

import com.example.dingo.model.Achievement
import com.example.dingo.model.AchievementListings
import com.example.dingo.model.User

class Stat constructor (statName: String): IObservable {
    val name: String = statName
    override val observers: ArrayList<IObserver> = ArrayList()
}

object SessionInfo {
    var currentUserAuthId: String? = null
    var currentUser: User? = null
    var currentUserID: String = ""
    var currentUsername: String = ""
    var nameToStat: MutableMap<String, Stat> = mutableMapOf()
}

// each stat is an observable and has a list of corresponding achievements
// i.e. the achievements are observers
// when a stat is updated, it sends the update method to all observers

enum class StatName {
    LOGINS, SCANS, SUCCESSFUL_SCANS, FLORA_SCANS, FAUNA_SCANS, NUM_SOCIAL_POSTS,
    NUM_CLASSROOM_POSTS, DISTINCT_FLORA, DISTINCT_FAUNA, DISTANCE_WALKED, NUM_TRIPS,
    NUM_COMMENTS, NUM_FRIENDS_ACCEPTED, NUM_FRIENDS_DECLINED,
}

fun initializeStats() {
    val currUser = SessionInfo.currentUser
    if (currUser == null) {
        println("Can't initialize stats with invalid user!")
    } else {
        for (stat in StatName.values()) {
            val currStat = Stat(stat.name)
            for (achievement in AchievementListings.achievementList) {
                if (achievement.conditionField == stat.name) {
                    currStat.add(achievement)
                }
            }
            SessionInfo.nameToStat[stat.name] = currStat
        }
    }

}

fun incrementStat(statName: StatName) {
    if (SessionInfo.currentUser != null) {
        val statValue = SessionInfo.currentUser!!.stats.getOrDefault(statName.name, 0)
        SessionInfo.currentUser!!.stats[statName.name]
        SessionInfo.currentUser!!.stats[statName.name] = statValue + 1
        SessionInfo.nameToStat[statName.name]?.sendUpdate()
    }
}
