package com.example.dingo.model.service.impl

import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.Post
import com.example.dingo.model.PostType
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.UserService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.min

class UserServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) : UserService {

    override suspend fun createUser(
        username: String, email: String, accountType: AccountType
    ): String {
        var user: User = User()
        user.username = username
        user.email = email
        user.accountType = accountType

        var userId = ""
        firestore.collection(USER_COLLECTIONS)
            .add(user)
            .addOnSuccessListener {
                userId = it.id
            }
            .addOnFailureListener {e ->
                println("Error adding User document: $e")
            }
            .await()

        if (userId.isEmpty()) {
            println("empty user id when creating user; this should not happen")
        }

        return userId
    }

    override suspend fun getUser(userId: String): User? {
        return firestore.collection(USER_COLLECTIONS)
            .document(userId)
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

        println("got user: $ret")
        return ret
    }

    override suspend fun sendFriendReq(senderId: String, receiverId: String): String? {
        var msg: String? = null

        firestore.collection(USER_COLLECTIONS)
            .document(senderId)
            .update("outgoingFriendRequests", FieldValue.arrayUnion(receiverId))
            .addOnSuccessListener {
                println("Successfully sent friend request from $senderId to $receiverId")
                msg = "Successfully send friend request"
            }
            .addOnFailureListener {e ->
                println("Error in sending friend request: $e")
                msg = "Error in sending friend request"
            }

        return msg;
    }

    override suspend fun acceptFriendReq(senderId: String, receiverId: String): String? {
        var sender = firestore.collection(USER_COLLECTIONS)
            .document(senderId)
            .get()
            .await()
            .toObject(User::class.java) ?: return "Could not find sender with id $senderId"

        if (sender.friends.contains(receiverId)) {
            return "Users are already friends!"
        } else if (!sender.outgoingFriendRequests.contains(receiverId)) {
            return "No active friend request between users"
        }

        var receiver = firestore.collection(USER_COLLECTIONS)
            .document(receiverId)
            .get()
            .await()
            .toObject(User::class.java) ?: return "Could not find receiver with id $receiverId"

        if (receiver.friends.contains(senderId)) {
            return "Error: friendship is not mutual. This should not happen"
        } else if (!sender.incomingFriendRequests.contains(receiverId)) {
            return "No active friend request between users"
        }

        firestore.collection(USER_COLLECTIONS)
            .document(senderId)
            .update("outgoingFriendRequests", FieldValue.arrayRemove(receiverId))
            .addOnFailureListener {e ->
                println("Error in processing friend request acceptance: $e")
            }
            .await()

        firestore.collection(USER_COLLECTIONS)
            .document(receiverId)
            .update("incomingFriendRequests", FieldValue.arrayRemove(senderId))
            .addOnFailureListener {e ->
                println("Error in processing friend request acceptance: $e")
            }
            .await()

        firestore.collection(USER_COLLECTIONS)
            .document(senderId)
            .update("friends", FieldValue.arrayUnion(receiverId))
            .addOnFailureListener {e ->
                println("Error in adding friend: $e")
            }
            .await()

        firestore.collection(USER_COLLECTIONS)
            .document(receiverId)
            .update("friends", FieldValue.arrayUnion(senderId))
            .addOnFailureListener {e ->
                println("Error in adding friend: $e")
            }
            .await()

        return null
    }

    override suspend fun declineFriendReq(senderId: String, receiverId: String): String? {
        var msg: String? = null

        firestore.collection(USER_COLLECTIONS)
            .document(senderId)
            .update("outgoingFriendRequests", FieldValue.arrayRemove(receiverId))
            .addOnFailureListener {e ->
                msg = "Error in processing friend request decline: $e"
            }
            .await()

        firestore.collection(USER_COLLECTIONS)
            .document(receiverId)
            .update("incomingFriendRequests", FieldValue.arrayRemove(senderId))
            .addOnFailureListener {e ->
                msg = "Error in processing friend request decline: $e"
            }
            .await()

        return msg
    }

    override suspend fun updateDingoDex(
        userId: String,
        uncollected: List<String>,
        isFauna: Boolean
    ) {
        val field = if (isFauna) {
            UNCOLLECTED_FAUNA
        } else {
            UNCOLLECTED_FLORA
        }

        firestore.collection(USER_COLLECTIONS).document(userId).update(field, uncollected)
    }

    // This is for social posts
    override suspend fun setPostHeadForUser(userId: String, postId: String, postType: PostType) {
        firestore.collection(USER_COLLECTIONS)
            .document(userId)
            .update("postHead", postId)
            .await()
    }

    // This is for classroom posts
    override suspend fun addClassroomPost(userId: String, postId: String) {
        firestore.collection(USER_COLLECTIONS)
            .document(userId)
            .update("classroomPosts", FieldValue.arrayUnion(postId))
            .await()
    }

    override suspend fun getFriends(userId: String, limit: Int): Flow<MutableList<User>?> {
        return callbackFlow {
            val userCollection = firestore.collection(USER_COLLECTIONS).document(userId)

            val subscription = userCollection.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot!!.exists()) {
                    var currUser = snapshot.toObject(User::class.java)
                    var ret: MutableList<User> = mutableListOf<User>()

                    if (currUser != null) {
                        var numFriends = min(currUser.friends.size, limit)
                        if (limit == -1) {
                            numFriends = currUser.friends.size
                        }

                        // TODO: maybe change the schema to make this more efficient
                        for (i in 0 until numFriends) {
                            runBlocking {
                                val friend = firestore.collection(USER_COLLECTIONS)
                                    .document(currUser.friends[i])
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                                if (friend != null) {
                                    ret.add(friend)
                                }
                            }
                        }
                    }
                    trySend(ret)
                }
            }
            awaitClose { subscription.remove() }
        }
    }


    companion object {
        private const val USER_COLLECTIONS = "userCollections"
        private const val UNCOLLECTED_FAUNA = "uncollectedFauna"
        private const val UNCOLLECTED_FLORA = "uncollectedFlora"
    }
}