package com.example.dingo.model.service.impl

import android.graphics.Bitmap
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexEntryService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.dataObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DingoDexEntryServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) :
    DingoDexEntryService {

    override val dingoDexFaunaEntries: Flow<List<DingoDexEntry>>
        get() = firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, SessionInfo.currentUserID)
            .whereEqualTo(IS_FAUNA_FIELD, true)
            .dataObjects()

    override val dingoDexFloraEntries: Flow<List<DingoDexEntry>>
        get() = firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, SessionInfo.currentUserID)
            .whereEqualTo(IS_FAUNA_FIELD, false)
            .dataObjects()

    override suspend fun getEntry(userId: String, entryName: String) : List<DingoDexEntry> {
        return firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, SessionInfo.currentUserID)
            .whereEqualTo(ENTRY_NAME, entryName).get().await().toObjects(DingoDexEntry::class.java)
    }

    override suspend fun addNewEntry(newDingoDexEntry: DingoDexEntryContent): Boolean {
        val newEntry = DingoDexEntry(
            userId = SessionInfo.currentUserID,
            dingoDexId = newDingoDexEntry.id,
            name = newDingoDexEntry.name,
            isFauna = newDingoDexEntry.is_fauna,
            numEncounters = 1,
            location = "",
            pictures = emptyList(),
            scientificName = newDingoDexEntry.scientific_name,
            displayPicture = "default",
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
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val imagePath = "${SessionInfo.currentUserID}/${entryName}_${dateFormat.format(Date())}.png"
        // Create a reference to "mountains.jpg"
        val imageRef = storageRef.child(imagePath)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        return try {
            imageRef.putBytes(data).await()
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
