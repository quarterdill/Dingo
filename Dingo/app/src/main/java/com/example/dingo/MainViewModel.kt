package com.example.dingo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.Achievement
import com.example.dingo.model.AchievementListings
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.DingoDexScientificToIndex
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.UserService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val repo: AccountService,
    private val userService: UserService
): ViewModel() {
    var isLoading = MutableLiveData<Boolean>(false)

    init {
        getAuthState()
    }

    fun getAuthState() = repo.getAuthState(viewModelScope)

    fun getUser() {
        viewModelScope.launch {
            isLoading.value = true
            runBlocking {
                userService.getCurrentUser()
            }
            isLoading.value = false
        }
    }

    fun updateUserStats() {
        viewModelScope.launch {
            userService.updateStats()
        }
    }

    fun setUpAchievements(context: Context) {
        val jsonString: String
        try {
            jsonString = context.assets.open("achievements.json").bufferedReader().use {
                it.readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        val gson = Gson()
        val listAchievementType = object : TypeToken<List<Achievement>>() {}.type
        AchievementListings.achievementList = gson.fromJson(jsonString, listAchievementType)
        for (i in AchievementListings.achievementList.indices) {
            AchievementListings.achievementList[i].userService = userService
        }
    }

    fun setUpDingoDex(context: Context) {
        val jsonString: String
        try {
            jsonString = context.assets.open("florafauna.json").bufferedReader().use {
                it.readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val gson = Gson()
        val listDingoDexEntryType = object : TypeToken<List<DingoDexEntryContent>>() {}.type
        DingoDexEntryListings.dingoDexEntryList = gson.fromJson(jsonString, listDingoDexEntryType)
        DingoDexEntryListings.dingoDexEntryList.forEach {
            if (it.is_fauna) {
                DingoDexEntryListings.faunaEntryList.add(it)
                DingoDexScientificToIndex.dingoDexFaunaScientificToIndex[it.scientific_name] = it.id
            } else {
                DingoDexEntryListings.floraEntryList.add(it)
                DingoDexScientificToIndex.dingoDexFloraScientificToIndex[it.scientific_name] = it.id
            }
        }
    }
}