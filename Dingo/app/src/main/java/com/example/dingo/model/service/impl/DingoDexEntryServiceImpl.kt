package com.example.dingo.model.service.impl

import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexEntryService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.dataObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DingoDexEntryServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) :
    DingoDexEntryService {

    override val dingoDexFaunaEntries: Flow<List<DingoDexEntry>>
        // todo: authentication finish
//        get() = auth.currentUser.flatMapLatest { user ->
//    firestore.collection(DINGO_DEX_ENTRIES)
//    .whereEqualTo(USER_ID_FIELD, "temp")
//    .whereEqualTo(IS_FAUNA_FIELD, true)
//    .dataObjects()           }
        get() = firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, "temp")
            .whereEqualTo(IS_FAUNA_FIELD, true)
            .dataObjects()

    override val dingoDexFloraEntries: Flow<List<DingoDexEntry>>
        // todo: authentication finish
//        get() = auth.currentUser.flatMapLatest { user ->
//    firestore.collection(DINGO_DEX_ENTRIES)
//    .whereEqualTo(USER_ID_FIELD, "temp")
//    .whereEqualTo(IS_FAUNA_FIELD, false)
//    .dataObjects()           }
        get() = firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, "temp")
            .whereEqualTo(IS_FAUNA_FIELD, false)
            .dataObjects()

    override suspend fun addNewEntry(newDingoDexEntry: DingoDexEntry) {
        firestore.collection(DINGO_DEX_ENTRIES).add(newDingoDexEntry)
    }

    override suspend fun updateEntry(entry: DingoDexEntry) {
        firestore.collection(DINGO_DEX_ENTRIES).document(entry.id).set(entry).await()
    }

    override suspend fun deleteEntry(entryId: String) {
        firestore.collection(DINGO_DEX_ENTRIES).document(entryId).delete().await()
    }

    companion object {
        private const val DINGO_DEX_ENTRIES = "dingoDexEntries"
        private const val USER_ID_FIELD = "userId"
        private const val IS_FAUNA_FIELD = "fauna"
    }
}
