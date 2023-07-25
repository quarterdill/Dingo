package com.example.dingo.model.service.impl

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.DingoDex
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexEntryService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.dataObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.lang.Exception
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

    override suspend fun addNewEntry(newDingoDexEntry: DingoDexEntryContent): Boolean {
        // todo: change temp auth yes
        val newEntry = DingoDexEntry(
            userId = "temp",
            dingoDexId = newDingoDexEntry.id,
            name = newDingoDexEntry.name,
            isFauna = newDingoDexEntry.is_fauna,
            numEncounters = 1,
            location = "",
            pictures = emptyList(),
            displayPicture = newDingoDexEntry.default_picture_name,
        )
        return try {
            firestore.collection(DINGO_DEX_ENTRIES).add(newEntry).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateEntry(entry: DingoDexEntry): Boolean {
        return try {
            firestore.collection(DINGO_DEX_ENTRIES).document(entry.id).set(entry).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteEntry(entryId: String) {
        firestore.collection(DINGO_DEX_ENTRIES).document(entryId).delete().await()
    }

    override suspend fun addPicture(entryName: String, image: Bitmap): String {
        // Create a storage reference from our app
        val storageRef = Firebase.storage.reference
        // TODO: Change temp to user when auth is done, make imagepath have no spaces
        val userId = "temp"
        val imagePath = "$userId/$entryName.jpg"
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child(imagePath)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        return try {
            mountainsRef.putBytes(data).await()
            imagePath
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val DINGO_DEX_ENTRIES = "dingoDexEntries"
        private const val USER_ID_FIELD = "userId"
        private const val IS_FAUNA_FIELD = "fauna"
        private const val ENTRY_NAME = "name"

    }
}
