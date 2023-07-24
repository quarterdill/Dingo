package com.example.dingo.model.service.impl

import com.example.dingo.model.DingoDex
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexStorageService
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DingoDexStorageServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) :
    DingoDexStorageService {
    // TODO: Make this live data somehow
    override suspend fun getDingoDexItem(dingoId: String, isFauna: Boolean): DingoDex? {
        val collection = if (isFauna) {
            firestore.collection(DINGO_DEX_FAUNA)
        } else {
            firestore.collection(DINGO_DEX_FLORA)
        }
        return collection.document(dingoId).get().await().toObject(DingoDex::class.java)

    }

    override suspend fun findDingoDexItem(entryName: String): DingoDex? {
        var collection = firestore.collection(DINGO_DEX_FAUNA)
            .whereEqualTo(ENTRY_NAME, entryName).get().await().toObjects(DingoDex::class.java)
        if (collection.isNotEmpty()) {
            // Should only have 1 entry per animal
            return collection[0]
        }
        collection = firestore.collection(DINGO_DEX_FLORA)
            .whereEqualTo(ENTRY_NAME, entryName).get().await().toObjects(DingoDex::class.java)
        if (collection.isNotEmpty()) {
            // Should only have 1 entry per animal
            return collection[0]
        }
        return null
    }


//    override suspend fun getDingoDexItem(dingoId: String, isFauna: Boolean): Flow<DingoDex?> {
//        return return callbackFlow {
//            val collection = if (isFauna) {
//                firestore.collection(DINGO_DEX_FAUNA)
//            } else {
//                firestore.collection(DINGO_DEX_FLORA)
//            }
//            val entries = collection.document(dingoId)
//            val subscription = entries.addSnapshotListener { snapshot, e ->
//                if (snapshot == null) {
//                    trySend(null)
//                } else if (snapshot!!.exists()) {
//                    trySend(snapshot.toObject(DingoDex::class.java))
//                }
//            }
//            awaitClose { subscription.remove() }
//        }
//    }

    // For testing only
    override suspend fun addDummyDingoDex(isFauna: Boolean) {
        if (isFauna)
            firestore.collection(DINGO_DEX_FAUNA).add(dummyFaunaDingoDex)
        else
            firestore.collection(DINGO_DEX_FLORA).add(dummyFloraDingoDex)
    }

    override suspend fun update(dingoDex: DingoDex) {
        if (dingoDex.isFauna) {
            firestore.collection(DINGO_DEX_FAUNA).document(dingoDex.id).set(dingoDex).await()
        } else {
            firestore.collection(DINGO_DEX_FLORA).document(dingoDex.id).set(dingoDex).await()
        }
    }

    override suspend fun delete(dingoId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getDingoDex(isFauna: Boolean): MutableList<DingoDex> {
        val collection = if (isFauna) {
            firestore.collection(DINGO_DEX_FAUNA)
        } else {
            firestore.collection(DINGO_DEX_FLORA)
        }
        var dingoDex = mutableListOf<DingoDex>()
        for (document in collection.get().await().documents) {
            val dingoDexItem = document.toObject(DingoDex::class.java)
            if (dingoDexItem != null)
                dingoDex.add(dingoDexItem)
        }
        return dingoDex
    }


    companion object {
        private const val USER_ID_FIELD = "userId"
        private const val DINGO_DEX_FAUNA = "dingoDexFauna"
        private const val DINGO_DEX_FLORA = "dingoDexFlora"
        private const val ENTRY_NAME = "name"
        private val dummyFaunaDingoDex = DingoDex(
            name = "Dummy Fauna",
            isFauna = true,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                    "tempor incididunt ut labore et dolore magna aliqua. Ac auctor augue mauris augue " +
                    "neque gravida in fermentum et. Id faucibus nisl tincidunt eget nullam non nisi est sit. " +
                    "Aliquam faucibus purus in massa tempor nec feugiat. Mollis nunc sed id semper risus in" +
                    " hendrerit gravida. Felis eget velit aliquet sagittis id consectetur purus ut. ",
            notes = "This is a dummy fauna entry"
        )
        private val dummyFloraDingoDex = DingoDex(
            name = "Dummy Flora",
            isFauna = false,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                    "tempor incididunt ut labore et dolore magna aliqua. Ac auctor augue mauris augue " +
                    "neque gravida in fermentum et. Id faucibus nisl tincidunt eget nullam non nisi est sit. " +
                    "Aliquam faucibus purus in massa tempor nec feugiat. Mollis nunc sed id semper risus in" +
                    " hendrerit gravida. Felis eget velit aliquet sagittis id consectetur purus ut. ",
            notes = "This is a dummy flora entry"
        )
    }
}
