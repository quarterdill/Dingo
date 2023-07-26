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

    fun getEntry(userId: String, entryName: String) : List<DingoDexEntry> {
        return runBlocking {  dingoDexEntryService.getEntry(userId, entryName) }
    }

    fun getDingoDexUncollectedItems(
        isFauna: Boolean,
        userId: String
    ): LiveData<MutableList<DingoDexCollectionItem>> {
        return liveData(Dispatchers.IO) {
            println(userId)
            val dingoDexItems = mutableListOf<DingoDexCollectionItem>()
            try {
                userService.getUserFlow(userId).collect {
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
                                            scientificName = dingoDexItem.scientific_name,
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

    fun getDingoDexCollectedItems(
        isFauna: Boolean,
        userId: String
    ): LiveData<MutableList<DingoDexCollectionItem>> {
        println(userId)
        var dingoDexIdToCollectedItems = mutableMapOf<Int, DingoDexCollectionItem>()
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
                        dingoDexIdToCollectedItems[item.dingoDexId] = DingoDexCollectionItem(
                            id = item.dingoDexId,
                            name = item.name,
                            scientificName = item.scientificName,
                            pictureURL = item.displayPicture,
                            isFauna = isFauna,
                            numEncounters = item.numEncounters
                        )
                    }
                }
                userService.getUserFlow(userId).collect {
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
                                if (dingoDexItem != null && dingoDexIdToCollectedItems.containsKey(dingoDexItem.id)) {
                                    dingoDexIdToCollectedItems.remove(dingoDexItem.id)
                                }
                            } catch (e: java.lang.Exception) {
                                // Do nothing
                            }
                        }
                    }
                }
                emit(dingoDexIdToCollectedItems.values.toMutableList())
            } catch (e: Exception) {
                emit(dingoDexItems)
            }
        }
    }
}