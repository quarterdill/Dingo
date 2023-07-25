package com.example.dingo.social

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Achievement
import com.example.dingo.model.AchievementListings
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.User
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val userService: UserService,
    private val accountService: AccountService,
) : ViewModel() {
    fun getNumUncollectedFlora(): Int {
        val currUser = SessionInfo.currentUser
        if (currUser != null) {
            return currUser.uncollectedFlora.size
        }
        return 0
    }
    fun getNumUncollectedFauna(): Int {
        val currUser = SessionInfo.currentUser
        if (currUser != null) {
            return currUser.uncollectedFauna.size
        }
        return 0
    }

    fun getAchievements(context: Context): List<Achievement> {
        var ret: MutableList<Achievement> = mutableListOf()
        val currUser = SessionInfo.currentUser
        if (currUser != null) {
            val achievementIds = currUser.achievements
            for (i in achievementIds) {
                ret.add(AchievementListings.getInstance(context).achievementList[i])
            }
        }

        return ret
    }

}