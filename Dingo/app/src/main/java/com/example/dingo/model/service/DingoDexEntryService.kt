package com.example.dingo.model.service

import com.example.dingo.model.DingoDexEntry
import kotlinx.coroutines.flow.Flow

interface DingoDexEntryService {
    val dingoDexFaunaEntries: Flow<List<DingoDexEntry>>
    val dingoDexFloraEntries: Flow<List<DingoDexEntry>>
    suspend fun addNewEntry(newDingoDexEntry: DingoDexEntry)
    suspend fun updateEntry(entry: DingoDexEntry)
    suspend fun deleteEntry(entryId: String)
}