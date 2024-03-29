package com.example.dingo.model.service.impl

import com.example.dingo.model.Classroom
import com.example.dingo.model.Post
import com.example.dingo.model.UserType
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ClassroomServiceImpl
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) :
    ClassroomService {

    override suspend fun getClassroom(classroomId: String): Flow<Classroom?> {
        return callbackFlow {
            val entries = firestore.collection(CLASSROOM_COLLECTIONS).document(classroomId)
            val subscription = entries.addSnapshotListener { snapshot, _ ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot.exists()) {
                    trySend(snapshot.toObject(Classroom::class.java))
                }
            }
            awaitClose { subscription.remove() }
        }
    }

    override suspend fun addNewClassroom(newClassroom: Classroom): String {
        var classroomId = ""
        firestore.collection(CLASSROOM_COLLECTIONS)
            .add(newClassroom)
            .addOnSuccessListener {
                classroomId = it.id
            }
            .addOnFailureListener {e ->
                println("Error adding Classroom document: $e")
            }
            .await()

        if (classroomId.isEmpty()) {
            println("empty classroom id when creating; this should not happen")
        }

        return classroomId
    }

    override suspend fun addUser(classroomId: String, userId: String, userType: UserType) {
        var userArr = ""
        if (userType == UserType.STUDENT) {
            userArr = "students"
        } else if (userType == UserType.TEACHER) {
            userArr = "teachers"
        }
        firestore.collection(CLASSROOM_COLLECTIONS)
            .document(classroomId)
            .update(userArr, FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                println("Successfully added user id $userArr to classroom $classroomId")
            }
            .addOnFailureListener {e ->
                println("Error in adding user to classroom: $e")
            }

        firestore.collection(USER_COLLECTIONS)
            .document(userId)
            .update("classroomIds", FieldValue.arrayUnion(classroomId))
            .addOnSuccessListener {
                println("Successfully added classroom id $classroomId for user $userId")
            }
            .addOnFailureListener {e ->
                println("Error in adding classroom for user: $e")
            }

    }

    override suspend fun addPost(classroomId: String, postId: String) {
        firestore.collection(CLASSROOM_COLLECTIONS)
            .document(classroomId)
            .update("posts", FieldValue.arrayUnion(postId))
            .addOnSuccessListener {
                println("Successfully added post to classroom $classroomId")
            }
            .addOnFailureListener {e ->
                println("Error in adding user to classroom: $e")
            }
    }

    override suspend fun deletePost(classroomId: String, postId: String) {
        firestore.collection(CLASSROOM_COLLECTIONS)
            .document(classroomId)
            .update("posts", FieldValue.arrayRemove(postId))
            .addOnSuccessListener {
                println("Successfully removed post from classroom $classroomId")
            }
            .addOnFailureListener {e ->
                println("Error removing post from classroom: $e")
            }
    }

    override suspend fun getPostFeed(classroomId: String, limit: Int): Flow<MutableList<Post>?> {
        // TODO("get (limit) most recent posts for feed")

        return callbackFlow {
            val classroomCollection = firestore.collection(CLASSROOM_COLLECTIONS)
                .document(classroomId)
            val subscription = classroomCollection.addSnapshotListener { snapshot, e ->
                if (snapshot == null) {
                    trySend(null)
                } else if (snapshot!!.exists()) {
                    var classroom = snapshot.toObject(Classroom::class.java)
                    var limiter = 0
                    var ret: MutableList<Post> = mutableListOf<Post>()
                    if (classroom != null) {
                        for (postId in classroom.posts.reversed()) {
                            if (limiter > limit) {
                                break
                            }
                            limiter++

                            var post: Post? = null

                            println("got post with id $postId")

                            runBlocking {
                                post = firestore.collection(POST_COLLECTIONS)
                                    .document(postId)
                                    .get()
                                    .await()
                                    .toObject(Post::class.java)
                            }

                            if (post != null) {
                                ret.add(post!!)
                            } else {
                                println("post $postId not found!?")
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
        private const val CLASSROOM_COLLECTIONS = "classroomCollections"
        private const val POST_COLLECTIONS = "postCollections"
        private const val USER_COLLECTIONS = "userCollections"
        private val SAMPLE_CLASSROOM = Classroom(
            teachers = mutableListOf("vf6w8xMVABol0Ex383YG"),
            students = mutableListOf(),
            posts = listOf(),
        )
    }
}