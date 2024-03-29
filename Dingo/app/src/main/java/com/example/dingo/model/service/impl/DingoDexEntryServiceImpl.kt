package com.example.dingo.model.service.impl

import android.graphics.Bitmap
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.model.User
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

    override suspend fun getDingoDexFaunaEntries(userId: String) : Flow<List<DingoDexEntry>> {
        return firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, userId)
            .whereEqualTo(IS_FAUNA_FIELD, true)
            .dataObjects()
    }

    override suspend fun getDingoDexFloraEntries(userId: String) : Flow<List<DingoDexEntry>> {
        return firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, userId)
            .whereEqualTo(IS_FAUNA_FIELD, false)
            .dataObjects()
    }

    override suspend fun getEntry(userId: String, entryName: String) : List<DingoDexEntry> {
        return firestore.collection(DINGO_DEX_ENTRIES)
            .whereEqualTo(USER_ID_FIELD, SessionInfo.currentUserID)
            .whereEqualTo(ENTRY_NAME, entryName).get().await().toObjects(DingoDexEntry::class.java)
    }

    override suspend fun getEntryById(entryId: String): DingoDexEntry? {
        return firestore.collection(DINGO_DEX_ENTRIES)
            .document(entryId)
            .get()
            .await()
            .toObject(DingoDexEntry::class.java)
    }

    override suspend fun addNewEntry(newDingoDexEntry: DingoDexEntryContent): Boolean {
        val newEntry = DingoDexEntry(
            userId = SessionInfo.currentUserID,
            dingoDexId = newDingoDexEntry.id,
            name = newDingoDexEntry.name,
            fauna = newDingoDexEntry.is_fauna,
            numEncounters = 1,
            location = "",
            pictures = mutableListOf(),
            displayPicture = "default",
            scientificName = newDingoDexEntry.scientific_name
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
        val imagePath = "${SessionInfo.currentUserID}/${entryName}_${dateFormat.format(Date())}.jpg"
        // Create a reference to "mountains.jpg"
        val imageRef = storageRef.child(imagePath)
        println("add picture path is $imagePath")
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
        private const val ENTRY_NAME = "scientificName"
    }
}
