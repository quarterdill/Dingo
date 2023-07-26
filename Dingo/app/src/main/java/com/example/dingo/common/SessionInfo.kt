package com.example.dingo.common

import com.example.dingo.model.Achievement
import com.example.dingo.model.AchievementListings
import com.example.dingo.model.Trip
import com.example.dingo.model.User
import com.example.dingo.model.service.UserService

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
    var trip: Trip? = null
}

// each stat is an observable and has a list of corresponding achievements
// i.e. the achievements are observers
// when a stat is updated, it sends the update method to all observers

enum class StatName {
    LOGINS, SCANS, SUCCESSFUL_SCANS, FLORA_SCANS, FAUNA_SCANS, NUM_SOCIAL_POSTS,
    NUM_CLASSROOM_POSTS, DISTINCT_FLORA, DISTINCT_FAUNA, DISTANCE_WALKED, NUM_TRIPS,
    NUM_COMMENTS, NUM_FRIENDS_ACCEPTED, NUM_FRIENDS_DECLINED,
}

fun newTrip() {
    SessionInfo.trip = Trip()
}

fun addNewEntry(entryId: String) {
    if (SessionInfo.trip != null) {
        var trip = SessionInfo.trip!!
        val hasEntry = trip.discoveredEntries.indexOf(entryId) != -1
        if (!hasEntry) {
            trip.discoveredEntries.add(entryId)
        }
    }

}

fun addPicture(picturePath: String) {
    if (SessionInfo.trip != null) {
        var trip = SessionInfo.trip!!
        trip.picturePaths.add(picturePath)
    }
}

fun initializeStats() {
    val currUser = SessionInfo.currentUser
    println("ACHIEVEMENTS: : initializing stats...")
    if (currUser == null) {
        println("ACHIEVEMENTS: : Can't initialize stats with invalid user!")
    } else {
        for (stat in StatName.values()) {
            println("ACHIEVEMENTS: : initializing stat: ${stat.name}")
            val currStat = Stat(stat.name)
            SessionInfo.nameToStat[stat.name] = currStat
        }
        for (achievement in AchievementListings.achievementList) {
            println("ACHIEVEMENTS: : trying to sort out achievement: ${achievement.name} with condition field ${achievement.conditionField}")
            SessionInfo.nameToStat[achievement.conditionField]!!.add(achievement)
        }
    }

}

fun incrementStat(statName: StatName, increment: Int = 1) {
    if (SessionInfo.currentUser != null) {
        val statValue = SessionInfo.currentUser!!.stats.getOrDefault(statName.name, 0)
        SessionInfo.currentUser!!.stats[statName.name]
        SessionInfo.currentUser!!.stats[statName.name] = statValue + increment
        SessionInfo.nameToStat[statName.name]?.sendUpdate()
        println("ACHIEVEMENTS: : : incremented stat ${statName.name}")
    }
}
