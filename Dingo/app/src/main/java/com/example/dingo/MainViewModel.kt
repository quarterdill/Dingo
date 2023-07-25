package com.example.dingo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.DingoDexScientificToIndex
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.UserService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val repo: AccountService,
    private val userService: UserService
): ViewModel() {
    init {
        getAuthState()
    }

    fun getAuthState() = repo.getAuthState(viewModelScope)

    fun getUser() {
        viewModelScope.launch {
            userService.getCurrentUser()
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
            println(it)
            if (it.is_fauna) {
                DingoDexEntryListings.faunaEntryList.add(it)
                DingoDexScientificToIndex.dingoDexFaunaScientificToIndex[it.scientific_name] = it.id
            } else {
                DingoDexEntryListings.floraEntryList.add(it)
                DingoDexScientificToIndex.dingoDexFloraScientificToIndex[it.scientific_name] = it.id
            }
        }
    }

    val isEmailVerified get() = repo.currentUser?.isEmailVerified ?: false

}