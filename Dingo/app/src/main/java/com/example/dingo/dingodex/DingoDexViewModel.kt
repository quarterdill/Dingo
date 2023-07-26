package com.example.dingo.dingodex

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.DingoDex
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.service.DingoDexEntryService
import com.example.dingo.model.service.DingoDexStorageService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DingoDexViewModel
@Inject
constructor(
    private val userService: UserService,
    private val dingoDexEntryService: DingoDexEntryService,
    private val dingoDexStorageService: DingoDexStorageService,
) : ViewModel() {


    val uncollectedDingoDexFauna = getDingoDexUncollectedItems(true)
    val uncollectedDingoDexFlora = getDingoDexUncollectedItems(false)
    val collectedDingoDexFauna = getDingoDexCollectedItems(true)
    val collectedDingoDexFlora = getDingoDexCollectedItems(false)
    var selectedEntryName = MutableLiveData<String>("")

    fun selectEntry(entryName: String) {
        selectedEntryName.value = entryName
    }

    fun getEntry(userId: String, entryName: String) : List<DingoDexEntry> {
        return runBlocking {  dingoDexEntryService.getEntry(userId, entryName) }
    }

    private fun getDingoDexUncollectedItems(
        isFauna: Boolean
    ): LiveData<MutableList<DingoDexCollectionItem>> {
        return liveData(Dispatchers.IO) {
            val dingoDexItems = mutableListOf<DingoDexCollectionItem>()
            try {
                userService.getUserFlow().collect {
                    if (it != null) {

                        val uncollectedDingoDex = if (isFauna) {
                            it.uncollectedFauna
                        } else {
                            it.uncollectedFlora
                        }
                        for (item in uncollectedDingoDex) {
                            try {
                                dingoDexStorageService.getDingoDexItem(item, isFauna)
                                val dingoDexItem = dingoDexStorageService.getDingoDexItem(item, isFauna)
                                if (dingoDexItem != null) {
                                    dingoDexItems.add(
                                        DingoDexCollectionItem(
                                            id = dingoDexItem.id,
                                            name = dingoDexItem.name,
                                            pictureURL = dingoDexItem.default_picture_name,
                                            isFauna = isFauna,
                                            numEncounters = 0
                                        )
                                    )
                                }
                            } catch (e: java.lang.Exception) {
                                // Do nothing
                            }
                        }

                    }
                    emit(dingoDexItems)
                }
            } catch (e: Exception) {
                emit(dingoDexItems)
            }
        }
    }

    private fun getDingoDexCollectedItems(
        isFauna: Boolean
    ): LiveData<MutableList<DingoDexCollectionItem>> {
        return liveData(Dispatchers.IO) {
            val dingoDexItems = mutableListOf<DingoDexCollectionItem>()
            try {
                val collectedDingoDex = if (isFauna) {
                    dingoDexEntryService.dingoDexFaunaEntries
                } else {
                    dingoDexEntryService.dingoDexFloraEntries
                }
                collectedDingoDex.collect {
                    for (item in it) {
                        dingoDexItems.add(
                            DingoDexCollectionItem(
                                id = item.dingoDexId,
                                name = item.name,
                                pictureURL = item.displayPicture,
                                isFauna = isFauna,
                                numEncounters = item.numEncounters
                            )
                        )
                    }
                    emit(dingoDexItems)
                }
            } catch (e: Exception) {
                emit(dingoDexItems)
            }
        }
    }

    // For testing only
    fun addDummyDingoDex(isFauna: Boolean) {
        viewModelScope.launch {
            dingoDexStorageService.addDummyDingoDex(isFauna)
        }
    }

    fun addNewUser() {
        viewModelScope.launch {
            // Todo: This implementation is only for demo, need to change for real one where the collectedDingoDexes
            //    would be empty
            val dingoDexes = listOf(dingoDexStorageService.getDingoDex(true), dingoDexStorageService.getDingoDex(false))

            var uncollectedDingoDex = listOf(mutableListOf<String>(), mutableListOf<String>())
            for (i in dingoDexes.indices) {
                for (j in 0 until dingoDexes[i].size) {
                    if (j % 2 == 0) {
                        val temp = DingoDex(
                            name = "Dummy Data",
                            isFauna = i == 0
                        )
                        //dingoDexEntryService.addNewEntry(temp)
                    } else {
                        uncollectedDingoDex[i].add(dingoDexes[i][j].id)
                    }
                }
//                userService.updateDingoDex("temp", uncollectedDingoDex[i], i == 0)
            }


        }
    }
}