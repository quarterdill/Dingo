package com.example.dingo.model.service.impl

import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.Post
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.UserService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) : UserService {

    override suspend fun createUser(username: String, email: String, accountType: AccountType) {
        var user: User = User()
        user.username = username
        user.email = email
        user.accountType = accountType

        firestore.collection(USER_COLLECTIONS).add(user)
    }

    override suspend fun getUser(userId: String): User? {
        var user = if (userId == "") "temp" else userId
        return firestore.collection(USER_COLLECTIONS)
            .document(user)
            .get()
            .await()
            .toObject(User::class.java)
    }

    override suspend fun getUserFlow(userId: String): Flow<User?> {
        if (userId == "") {
            // TODO: Uncomment once auth is implemented
//            return auth.currentUser.flatMapLatest { user ->
//                callbackFlow {
//                    val entries = firestore.collection(USER_COLLECTIONS).document(user.id)
//                    val subscription = entries.addSnapshotListener { snapshot, _ ->
//                        if (snapshot == null) {
//                            trySend(null)
//                        } else if (snapshot.exists()) {
//                            trySend(snapshot.toObject(User::class.java))
//                        }
//                    }
//                    awaitClose { subscription.remove() }
//                }
//            }
            return callbackFlow {
                    val entries = firestore.collection(USER_COLLECTIONS).document("temp")
                    val subscription = entries.addSnapshotListener { snapshot, _ ->
                        if (snapshot == null) {
                            trySend(null)
                        } else if (snapshot.exists()) {
                            trySend(snapshot.toObject(User::class.java))
                        }
                    }
                    awaitClose { subscription.remove() }
                }
        }
        return callbackFlow {
            val entries = firestore.collection(USER_COLLECTIONS).document(userId)
            val subscription = entries.addSnapshotListener { snapshot, _ ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot.exists()) {
                    trySend(snapshot.toObject(User::class.java))
                }
            }
            awaitClose { subscription.remove() }
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        var ret: User? = null
        val querySnapshot = firestore.collection(USER_COLLECTIONS)
            .whereEqualTo("email", email)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            val documentSnapshot = querySnapshot.documents[0]
            ret = documentSnapshot.toObject(User::class.java)
        }

        println("ambadeblou: $ret")
        return ret
    }

    override suspend fun sendFriendReq(senderId: String, receiverId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun acceptFriendReq(senderId: String, receiverId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun declineFriendReq(senderId: String, receiverId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateDingoDex(
        newEntryId: String,
        isFauna: Boolean
    ) {
        val field = if (isFauna) {
            UNCOLLECTED_FAUNA
        } else {
            UNCOLLECTED_FLORA
        }
        val user = firestore.collection(USER_COLLECTIONS)
            .document("temp").get().await().toObject(User::class.java)
        if (user != null) {
            val collection = if (isFauna) {
                user.uncollectedFauna.toMutableList()
            } else {
                user.uncollectedFlora.toMutableList()
            }
            collection.remove(newEntryId)
            // TODO: change temp once auth is done
            firestore.collection(USER_COLLECTIONS).document("temp").update(field, collection)
        }

    }


    companion object {
        private const val USER_COLLECTIONS = "userCollections"
        private const val UNCOLLECTED_FAUNA = "uncollectedFauna"
        private const val UNCOLLECTED_FLORA = "uncollectedFlora"
    }
}