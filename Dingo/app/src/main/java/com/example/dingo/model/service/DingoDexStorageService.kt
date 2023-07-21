package com.example.dingo.model.service

import com.example.dingo.model.DingoDex
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface DingoDexStorageService {
    suspend fun getDingoDexItem(dingoId: String, isFauna: Boolean): DingoDex?
    suspend fun findDingoDexItem(entryName: String): DingoDex?
    suspend fun getDingoDex(isFauna: Boolean): MutableList<DingoDex>
    // Dev only
    suspend fun addDummyDingoDex(isFauna: Boolean)
    suspend fun update(dingoDex: DingoDex)
    suspend fun delete(dingoId: String)
}