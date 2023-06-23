package com.example.dingo.dingodex

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.DingoDex
import com.example.dingo.model.DingoDexCollection
import com.example.dingo.model.service.DingoDexCollectionStorageService
import com.example.dingo.model.service.DingoDexStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DingoDexViewModel
@Inject
constructor(
    private val dingoDexCollectionStorageService: DingoDexCollectionStorageService,
    private val dingoDexStorageService: DingoDexStorageService,
) : ViewModel() {

    val fetchDingoDexFaunaCollection = getDingoDexItems(true)
    val fetchDingoDexFloraCollection = getDingoDexItems(false)

    private fun getDingoDexItems(
        isFauna: Boolean
    ): LiveData<MutableList<DingoDexCollectionItem>?> {
        return liveData(Dispatchers.IO) {
            try {
                dingoDexCollectionStorageService.getDingoDexCollection().collect {
                    if (it != null) {
                        val dingoDexItems = mutableListOf<DingoDexCollectionItem>()
                        val collectedDingoDex = if (isFauna) {
                            it.collectedFauna
                        } else {
                            it.collectedFlora
                        }
                        val uncollectedDingoDex = if (isFauna) {
                            it.uncollectedFauna
                        } else {
                            it.uncollectedFlora
                        }
                        println("$collectedDingoDex")
                        for (item in collectedDingoDex) {
                            try {
                                println("11111111111111111")
                                println("${item.keys.first()}")
                                val dingoDexItem = dingoDexStorageService.getDingoDexItem(item.keys.first(), isFauna)
                                    println("$dingoDexItem")
                                    if (dingoDexItem != null) {
                                        dingoDexItems.add(
                                            DingoDexCollectionItem(
                                                id = dingoDexItem.id,
                                                name = dingoDexItem.name,
                                                numEncounters = item[dingoDexItem.id]!!
                                            )
                                        )
                                    }
                                println("2222222222222222222")
                            } catch (e: java.lang.Exception) {
                                // Do nothing
                                println("$e")
                            }
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
                                            numEncounters = 0
                                        )
                                    )
                                }
                            } catch (e: java.lang.Exception) {
                                // Do nothing
                            }
                        }
                        emit(dingoDexItems)
                    } else {
                        emit(null)
                    }
                }
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    // For testing only
    fun addDummyDingoDex(isFauna: Boolean) {
        viewModelScope.launch {
            dingoDexStorageService.addDummyDingoDex(isFauna)
        }
    }

    fun addNewUser(userId: String) {
        viewModelScope.launch {
            // Todo: This implementation is only for demo, need to change for real one where the collectedDingoDexes
            //    would be empty
            val dingoDexes = listOf(dingoDexStorageService.getDingoDex(true), dingoDexStorageService.getDingoDex(false))

            var collectedDingoDex = listOf(mutableListOf<Map<String, Int>>(), mutableListOf<Map<String, Int>>())
            var uncollectedDingoDex = listOf(mutableListOf<String>(), mutableListOf<String>())
            for (i in dingoDexes.indices) {
                for (j in 0 until dingoDexes[i].size) {
                    if (j % 2 == 0) {
                        collectedDingoDex[i].add(mapOf(Pair(dingoDexes[i][j].id, (0..100).random())))
                    } else {
                        uncollectedDingoDex[i].add(dingoDexes[i][j].id)
                    }
                }
            }
            val newDingoDexCollection = DingoDexCollection(
                collectedFauna = collectedDingoDex[0],
                collectedFlora = collectedDingoDex[1],
                uncollectedFauna = uncollectedDingoDex[0],
                uncollectedFlora = uncollectedDingoDex[1]
            )
            dingoDexCollectionStorageService.addNewUser(newDingoDexCollection)
        }
    }
}