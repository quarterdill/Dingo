package com.example.dingo.model.service

import android.content.Context
import android.graphics.Bitmap
import com.example.dingo.model.DingoDex
import com.example.dingo.model.DingoDexEntryContent
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface DingoDexStorageService {
    suspend fun getDingoDexItem(dingoId: Int, isFauna: Boolean): DingoDexEntryContent
    suspend fun findDingoDexItem(entryName: String): DingoDexEntryContent?
    suspend fun getDingoDex(isFauna: Boolean): MutableList<DingoDex>
    // Dev only
    suspend fun addDummyDingoDex(isFauna: Boolean)
    suspend fun update(dingoDex: DingoDex)
    suspend fun delete(dingoId: String)
}