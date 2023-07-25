package com.example.dingo.common

import com.example.dingo.model.Achievement
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
    var nameToStat: Map<String, Stat> = emptyMap()
}

// each stat is an observable and has a list of corresponding achievements
// i.e. the achievements are observers
// when a stat is updated, it sends the update method to all observers
