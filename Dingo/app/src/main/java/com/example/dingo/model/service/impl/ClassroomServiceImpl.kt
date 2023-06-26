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

    override suspend fun addNewClassroom(newClassroom: Classroom) {
        firestore.collection(CLASSROOM_COLLECTIONS).add(newClassroom)
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
    }

    override suspend fun addPost(classroomId: String, post: Post) {
        var postId = ""
        firestore.collection(POST_COLLECTIONS)
            .add(post)
            .addOnSuccessListener {postRef ->
                println("Post DocumentSnapshot written with ID ${postRef.id}")
            }
            .addOnFailureListener {e ->
                println("Error adding Post document: $e")
            }

        if (postId.isEmpty()) {
            return
        }
        firestore.collection(CLASSROOM_COLLECTIONS)
            .document(classroomId)
            .update(classroomId, FieldValue.arrayUnion(post))
            .addOnSuccessListener {
                println("Successfully added post to classroom $classroomId")
            }
            .addOnFailureListener {e ->
                println("Error in adding user to classroom: $e")
            }
    }

    override suspend fun deletePost(postId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getPostFeed(classroomId: String): List<Post> {
        return listOf()
    }


    companion object {
        private const val CLASSROOM_COLLECTIONS = "classroomCollections"
        private const val POST_COLLECTIONS = "classroomPostCollections"
        private val SAMPLE_CLASSROOM = Classroom(
            teachers = listOf("vf6w8xMVABol0Ex383YG"),
            students = listOf(),
            posts = listOf(),
        )
    }
}