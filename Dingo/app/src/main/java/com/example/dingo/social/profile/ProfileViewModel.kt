package com.example.dingo.social.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.StatName
import com.example.dingo.common.incrementStat
import com.example.dingo.model.Achievement
import com.example.dingo.model.AchievementListings
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.Post
import com.example.dingo.model.PostComparator
import com.example.dingo.model.User
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.DingoDexEntryService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.UserService
import com.example.dingo.model.service.impl.UserServiceImpl
import com.example.dingo.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.PriorityQueue
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val userService: UserService,
    private val accountService: AccountService,
    private val dingoDexService: DingoDexEntryService,
) : ViewModel() {

    suspend fun onSignOutClick(navController: NavHostController) {
        Log.d("STATE", "signing out")
        val successfulSignOut = accountService.signOut();
        if (successfulSignOut) {
            navController.navigate(Screen.LoginScreen.route)
        }
    }

    fun getFriendsForUser(userId: String): LiveData<MutableList<User>?> {
        return liveData(Dispatchers.IO) {
            try {
                userService.getUserFlow(userId).collect {
                    val ret = mutableListOf<User>()
                    if (it != null) {
                        var numFriends = it.friends.size
                        for (i in 0 until numFriends) {
                                val friend = userService.getUser(it.friends[i])
                                if (friend != null) {
                                    ret.add(friend)
                            }
                        }
                    }
                    emit(ret)
                }
            }  catch (e: Exception) {
                emit(mutableListOf())
            }
        }
    }

    fun sendFriendReq(senderId: String, receiverName: String): Boolean {
        var receiverUser: User? = null
        var friendReqOk: Boolean = false
        runBlocking {
            receiverUser = userService.getUserByUsername(receiverName)
        }
        runBlocking{
            if (receiverUser != null) {
                friendReqOk = userService.sendFriendReq(senderId, receiverUser!!.id)
            }
        }
        return friendReqOk
    }

    fun acceptFriendReq(senderId: String, receiverId: String): String {
        var msg: String = "Something went wrong..."
        runBlocking{
            msg = userService.acceptFriendReq(senderId, receiverId)
        }
        incrementStat(StatName.NUM_FRIENDS_ACCEPTED)
        return msg
    }

    fun declineFriendReq(senderId: String, receiverId: String): String {
        var msg: String = "Something went wrong..."
        runBlocking{
            msg = userService.declineFriendReq(senderId, receiverId)
        }
        incrementStat(StatName.NUM_FRIENDS_DECLINED)
        return msg
    }

    fun getPendingFriendReqs(userId: String): LiveData<MutableList<User>?>{
        return liveData(Dispatchers.IO) {
            try {
                userService.getPendingFriendReqs(userId).collect {
                    if (it != null) {
                        val pending = it
                        emit(pending)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: java.lang.Exception) {
                // Do nothing
                println("$e")
            }
        }
    }

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
                ret.add(AchievementListings.achievementList[i])
            }
        }

        return ret
    }

}