package com.example.dingo.model.service.impl

import com.example.dingo.model.DingoDex
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

    override suspend fun getEntry(entryName: String) : List<DingoDexEntry> {
        // TODO: Change temp to user when auth is done
        return firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, "temp")
            .whereEqualTo(ENTRY_NAME, entryName).get().await().toObjects(DingoDexEntry::class.java)
    }

    override suspend fun addNewEntry(newDingoDexEntry: DingoDex) {
        // todo: change temp auth yes
        val newEntry = DingoDexEntry(
            userId = "temp",
            dingoDexId = newDingoDexEntry.id,
            name = newDingoDexEntry.name,
            isFauna = newDingoDexEntry.isFauna,
            numEncounters = 1,
            location = "",
            pictures = emptyList(),
            displayPicture = newDingoDexEntry.defaultPicture,
        )
        firestore.collection(DINGO_DEX_ENTRIES).add(newEntry)
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
        private const val ENTRY_NAME = "name"

    }
}
