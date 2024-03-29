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
    val isLoading = MutableLiveData(false)

    var uncollectedDingoDexFauna = getDingoDexUncollectedItems(true, "")
    var uncollectedDingoDexFlora = getDingoDexUncollectedItems(false, "")
    var collectedDingoDexFauna = getDingoDexCollectedItems(true, "")
    var collectedDingoDexFlora = getDingoDexCollectedItems(false, "")


    fun getEntry(userId: String, entryName: String) : List<DingoDexEntry> {
        return runBlocking {  dingoDexEntryService.getEntry(userId, entryName) }
    }

    fun getEntries(userId: String) {
        uncollectedDingoDexFauna = getDingoDexUncollectedItems(true, userId)
        uncollectedDingoDexFlora = getDingoDexUncollectedItems(false, userId)
        collectedDingoDexFauna = getDingoDexCollectedItems(true, userId)
        collectedDingoDexFlora = getDingoDexCollectedItems(false, userId)
    }

    fun getImage(imagePath: String) {

    }
    fun getDingoDexUncollectedItems(
        isFauna: Boolean,
        userId: String = ""
    ): LiveData<MutableList<DingoDexCollectionItem>> {
        return liveData(Dispatchers.IO) {
            val dingoDexItems = mutableListOf<DingoDexCollectionItem>()
            try {
                userService.getUserFlow(userId).collect {
                    print(it)
                    if (it != null) {
                        val uncollectedDingoDex = if (isFauna) {
                            it.uncollectedFauna
                        } else {
                            it.uncollectedFlora
                        }
                        for (item in uncollectedDingoDex) {
                            try {
                                val dingoDexItem = dingoDexStorageService.getDingoDexItem(if (isFauna) item else item - 50, isFauna)
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
        userId: String = ""
    ): LiveData<MutableList<DingoDexCollectionItem>> {
        return liveData(Dispatchers.IO) {
            val dingoDexItems = mutableListOf<DingoDexCollectionItem>()
            try {
                val collectedDingoDex = if (isFauna) {
                    dingoDexEntryService.getDingoDexFaunaEntries(userId)
                } else {
                    dingoDexEntryService.getDingoDexFloraEntries(userId)
                }
                collectedDingoDex.collect {
                    for (item in it) {
                        dingoDexItems.add(DingoDexCollectionItem(
                            id = item.dingoDexId,
                            name = item.name,
                            scientificName = item.scientificName,
                            pictureURL = item.displayPicture,
                            isFauna = isFauna,
                            numEncounters = item.numEncounters,
                            pictures = item.pictures,
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

    fun getEntryById(entryId: String): DingoDexEntry? {
        var ret: DingoDexEntry?
        runBlocking {
            ret = dingoDexEntryService.getEntryById(entryId)
        }
        return ret
    }

    //important: this gives DingoDexEntry, not DingoDexEntryContent
    fun getDingoDexCollectedEntries(
        isFauna: Boolean,
        userId: String,
    ): LiveData<List<DingoDexEntry>> {
        return liveData(Dispatchers.IO) {
            val ret = mutableListOf<DingoDexEntry>()
//            try {
                    val collectedDingoDex = if (isFauna) {
                        dingoDexEntryService.getDingoDexFaunaEntries(userId)
                    } else {
                        dingoDexEntryService.getDingoDexFloraEntries(userId)
                    }

                    collectedDingoDex.collect {
                        print("GOT DINGO DEX COLLECTED ENTRIES for user id $userId: $it")
                        emit(it)
                    }
//            } catch (e: Exception) {
//                println("error in getting dingo dex collected entries for user: $e")
//                emit(ret)
//            }
        }
    }
}