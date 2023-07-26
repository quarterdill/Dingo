package com.example.dingo.model.service.impl

import androidx.core.util.rangeTo
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.AccountType
import com.example.dingo.model.Classroom
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.Post
import com.example.dingo.model.PostType
import com.example.dingo.model.User
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.UserService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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
        username: String,
        email: String,
        authId: String,
        accountType: AccountType
    ): String {
        var user: User = User()
        user.username = username
        user.email = email
        user.authId = authId
        user.accountType = accountType

        var uncollectedFauna = mutableListOf<Int>()
        for (i in DingoDexEntryListings.faunaEntryList) {
            uncollectedFauna.add(i.id)
        }
        user.uncollectedFauna = uncollectedFauna

        var uncollectedFlora = mutableListOf<Int>()
        for (i in DingoDexEntryListings.floraEntryList) {
            uncollectedFlora.add(i.id)
        }
        user.uncollectedFlora = uncollectedFlora

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
        var user = if (userId == "") "temp" else userId
        return firestore.collection(USER_COLLECTIONS)
            .document(user)
            .get()
            .await()
            .toObject(User::class.java)
    }

    override suspend fun getUserFlow(userId: String): Flow<User?> {
        if (userId == "") {
            return callbackFlow {
                    val entries = firestore.collection(USER_COLLECTIONS).document(SessionInfo.currentUserID)
                    val subscription = entries.addSnapshotListener { snapshot, _ ->
                        if (snapshot == null) {
                            trySend(null)
                        } else if (snapshot.exists()) {
                            println("SNAPSHOT: $snapshot")
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

    override suspend fun getCurrentUser() {
        if (auth.currentUserId.isNotEmpty()) {
            val querySnapshot = firestore.collection(USER_COLLECTIONS)
                .whereEqualTo("authId", auth.currentUserId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentSnapshot = querySnapshot.documents[0]
                val currUser = documentSnapshot.toObject(User::class.java)
                SessionInfo.currentUser = currUser
                if (currUser != null) {
                    SessionInfo.currentUserID = currUser.id
                    SessionInfo.currentUsername = currUser.username
                }

            }
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

    override suspend fun getUserByUsername(username: String): User? {
        var ret: User? = null
        val querySnapshot = firestore.collection(USER_COLLECTIONS)
            .whereEqualTo("username", username)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            val documentSnapshot = querySnapshot.documents[0]
            ret = documentSnapshot.toObject(User::class.java)
        }

        println("got user: $ret")
        return ret
    }

    override suspend fun sendFriendReq(senderId: String, receiverId: String): Boolean {
        var ok = true

        firestore.collection(USER_COLLECTIONS)
            .document(senderId)
            .update("outgoingFriendRequests", FieldValue.arrayUnion(receiverId))
            .addOnSuccessListener {
                println("Successfully sent friend request from $senderId to $receiverId")
            }
            .addOnFailureListener {e ->
                println("Error in sending friend request: $e")
                ok = false
            }.await()

        if (!ok) {
            return ok
        }

        firestore.collection(USER_COLLECTIONS)
            .document(receiverId)
            .update("incomingFriendRequests", FieldValue.arrayUnion(senderId))
            .addOnSuccessListener {
                println("Successfully got friend request for $receiverId from $senderId")
            }
            .addOnFailureListener {e ->
                println("Error in sending friend request: $e")
                ok = false
            }.await()

        return ok;
    }

    override suspend fun acceptFriendReq(senderId: String, receiverId: String): String {
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
        } else if (!receiver.incomingFriendRequests.contains(senderId)) {
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

        return "Accepted!"
    }

    override suspend fun declineFriendReq(senderId: String, receiverId: String): String {
        var msg: String = "Declined..."

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
        newEntryId: Int,
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

    override suspend fun getUsersPosts(userId: String): Flow<MutableList<Post>?> {
        var user = firestore.collection(USER_COLLECTIONS)
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)

        return callbackFlow {
            val userCollection = firestore.collection(USER_COLLECTIONS)
                .document(userId)
            val subscription = userCollection.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot!!.exists()) {
                    var user = snapshot.toObject(User::class.java)
                    var ret = mutableListOf<Post>()
                    if (user != null) {
                        var currPostId = user.postHead
                        while (currPostId != "") {
                            var post: Post? = null
                            runBlocking {
                                post = firestore.collection(POST_COLLECTIONS)
                                    .document(currPostId)
                                    .get()
                                    .await()
                                    .toObject(Post::class.java)
                            }

                            if (post != null) {
                                ret.add(post!!)
                                currPostId = post!!.prevPost
                            } else {
                                println("post $currPostId not found when getting posts for $userId")
                                break
                            }

                        }
                    }
                    trySend(ret)
                }
            }
            awaitClose { subscription.remove() }
        }
    }

    override suspend fun getPendingFriendReqs(userId: String): Flow<MutableList<User>?> {
        return callbackFlow {
            val userCollection = firestore.collection(USER_COLLECTIONS).document(userId)

            val subscription = userCollection.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot!!.exists()) {
                    var currUser = snapshot.toObject(User::class.java)
                    var ret: MutableList<User> = mutableListOf<User>()

                    if (currUser != null) {
                        // TODO: maybe change the schema to make this more efficient
                        for (i in 0 until currUser.incomingFriendRequests.size) {
                            runBlocking {
                                val incoming = firestore.collection(USER_COLLECTIONS)
                                    .document(currUser.incomingFriendRequests[i])
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                                if (incoming != null) {
                                    ret.add(incoming)
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

    override suspend fun addAchievementForUser(user: User, achievementId: Int) {
        firestore.collection(USER_COLLECTIONS)
            .document(user.id)
            .update("achievements", FieldValue.arrayUnion(achievementId))
            .await()
    }

    override suspend fun getClassrooms(userId: String, limit: Int): Flow<MutableList<Classroom>?>{
        return callbackFlow {
            val userCollection = firestore.collection(USER_COLLECTIONS).document(userId)

            val subscription = userCollection.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    println("failed to get user snapshot when fetching classrooms...")
                    trySend(null)
                } else if (snapshot!!.exists()) {
                    var currUser = snapshot.toObject(User::class.java)
                    var ret: MutableList<Classroom> = mutableListOf()
                    println("getting classrooms for user: $currUser")
                    if (currUser != null) {
                        for (i in 0 until currUser.classroomIds.size) {
                            runBlocking {
                                val incoming = firestore.collection(CLASSROOM_COLLECTIONS)
                                    .document(currUser.classroomIds[i])
                                    .get()
                                    .await()
                                    .toObject(Classroom::class.java)
                                if (incoming != null) {
                                    ret.add(incoming)
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

    override suspend fun updateStats() {
        val currUser = SessionInfo.currentUser
        if (currUser != null) {
            firestore.collection(USER_COLLECTIONS)
                .document(currUser.id)
                .update("stats", currUser.stats)
                .await()
        }
    }

    override suspend fun addTripForUser(userId : String, tripId: String) {
        firestore.collection(USER_COLLECTIONS)
            .document(userId)
            .update("trips", FieldValue.arrayUnion(tripId))
            .await()
    }

    companion object {
        private const val USER_COLLECTIONS = "userCollections"
        private const val POST_COLLECTIONS = "postCollections"
        private const val CLASSROOM_COLLECTIONS = "classroomCollections"
        private const val UNCOLLECTED_FAUNA = "uncollectedFauna"
        private const val UNCOLLECTED_FLORA = "uncollectedFlora"
    }
}