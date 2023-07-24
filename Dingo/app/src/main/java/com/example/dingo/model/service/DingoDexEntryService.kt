package com.example.dingo.model.service

import android.graphics.Bitmap
import com.example.dingo.model.DingoDex
import com.example.dingo.model.DingoDexEntry
import kotlinx.coroutines.flow.Flow

interface DingoDexEntryService {
    val dingoDexFaunaEntries: Flow<List<DingoDexEntry>>
    val dingoDexFloraEntries: Flow<List<DingoDexEntry>>

    suspend fun getEntry(entryName: String) : List<DingoDexEntry>
    suspend fun addNewEntry(newDingoDexEntry: DingoDex) : Boolean
    suspend fun updateEntry(entry: DingoDexEntry) : Boolean
    suspend fun deleteEntry(entryId: String)
    suspend fun addPicture(entryName: String, image: Bitmap) : String
}