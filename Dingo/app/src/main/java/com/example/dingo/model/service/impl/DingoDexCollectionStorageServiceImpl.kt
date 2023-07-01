//package com.example.dingo.model.service.impl
//
//import com.example.dingo.model.DingoDexCollection
//import com.example.dingo.model.service.AccountService
//import com.example.dingo.model.service.DingoDexCollectionStorageService
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//
//class DingoDexCollectionStorageServiceImpl
//@Inject
//constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) :
//DingoDexCollectionStorageService {
//
//    override suspend fun getDingoDexCollection(): Flow<DingoDexCollection?> {
//        // TODO: uncomment code below once authentication is working
////        return auth.currentUser.flatMapLatest { user ->
////        callbackFlow {
////            val entries = firestore.collection(DINGO_DEX_COLLECTIONS).document(user.io)
////            val subscription = entries.addSnapshotListener { snapshot, e ->
////                println("teststs: $snapshot $e")
////                if (snapshot == null) {
////                    trySend(null)
////                } else if (snapshot!!.exists()) {
////                    println("wfwefwefwfwefwefwefwe: $snapshot $e")
////                    trySend(snapshot.toObject(DingoDexCollection::class.java))
////                }
////            }
////            awaitClose { subscription.remove() }
////        }
////        }
//        return callbackFlow {
//                val entries = firestore.collection(DINGO_DEX_COLLECTIONS).document("temp")
//                val subscription = entries.addSnapshotListener { snapshot, e ->
//                    println("teststs: $snapshot $e")
//                    if (snapshot == null) {
//                        trySend(null)
//                    } else if (snapshot!!.exists()) {
//                        println("wfwefwefwfwefwefwefwe: $snapshot $e")
//                        trySend(snapshot.toObject(DingoDexCollection::class.java))
//                    }
//                }
//                awaitClose { subscription.remove() }
//            }
//    }
//
//    override suspend fun addNewUser(newDingoDexCollection: DingoDexCollection) {
//        // TODO: Add actual functionality once we have all dingo dex entries
//        firestore.collection(DINGO_DEX_COLLECTIONS).add(newDingoDexCollection)
//    }
//
//    override suspend fun update(collection: DingoDexCollection) {
//        firestore.collection(DINGO_DEX_COLLECTIONS).document(collection.id).set(collection).await()
//    }
//
//    override suspend fun delete(userId: String) {
//        firestore.collection(DINGO_DEX_COLLECTIONS).document(userId).delete().await()
//    }
//
//    companion object {
//        private const val DINGO_DEX_COLLECTIONS = "dingoDexCollections"
//        private val NEW_COLLECTION = DingoDexCollection(
//            id = "hehehe",
//            collectedFauna = listOf(mapOf(Pair("gfdsgsdg", 4)), mapOf(Pair("gfdsgsdg", 4)), mapOf(Pair("gfdsgsdg", 4)), mapOf(Pair("gfdsgsdg", 4))),
//            uncollectedFauna =  listOf("fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf",),
//            collectedFlora = listOf(mapOf(Pair("gfdsgsdg", 4)), mapOf(Pair("gfdsgsdg", 4)), mapOf(Pair("gfdsgsdg", 4)), mapOf(Pair("gfdsgsdg", 4))),
//            uncollectedFlora =  listOf("fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf","fsdf",)
//        )
//    }
//}