package com.example.dingo.model.service.impl

// from https://github.com/FirebaseExtended/make-it-so-android/blob/main/start/app/src/main/java/com/example/makeitso/model/service/impl/AccountServiceImpl.kt

import android.util.Log
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.Response.Failure
import com.example.dingo.model.Response.Success
import com.example.dingo.model.User
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.AuthStateResponse
import com.example.dingo.model.service.ReloadUserResponse
import com.example.dingo.model.service.SendEmailVerificationResponse
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.rpc.context.AttributeContext.Resource
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AccountService {

    override val currentUser get() = auth.currentUser
    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val hasUser: Boolean
        get() = auth.currentUser != null

//    override val currentUser: Flow<User>
//        get() = callbackFlow {
//            val listener =
//                FirebaseAuth.AuthStateListener { auth ->
//                    this.trySend(auth.currentUser?.let { User(it.uid, it.isAnonymous) } ?: User())
//                }
//            auth.addAuthStateListener(listener)
//            awaitClose { auth.removeAuthStateListener(listener) }
//        }

//    override suspend fun loginUser(
//        email: String, password: String
//    ) = try {
//        auth.signInWithEmailAndPassword(email, password).await()
//        Success(true)
//    } catch (e: Exception) {
//        Failure(e)
//    }

    override suspend fun loginUser(email: String, password: String): Boolean {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            // Authentication successful
            val user = authResult.user
            SessionInfo.currentUserAuthId = user?.uid
            // Handle the logged-in user as needed
            return true
        } catch (e: Exception) {
            // Authentication failed
            Log.e("STATE", "Login failed: ${e.message}")
            // Handle the authentication failure as needed
        }
        return false
    }

    override suspend fun sendEmailVerification() = try {
        auth.currentUser?.sendEmailVerification()?.await()
        Success(true)
    } catch (e: Exception) {
        Failure(e)
    }

//    override suspend fun registerUser(
//        email: String, password: String
//    ) = try {
//        Log.d("STATE", "in create user")
//        auth.createUserWithEmailAndPassword(email, password).await()
//        Success(true)
//    } catch (e: Exception) {
//        Failure(e)
//    }

    override suspend fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun sendRecoveryEmail(email: String) = try {
        auth.sendPasswordResetEmail(email).await()
        Success(true)
    } catch (e: Exception) {
        Failure(e)
    }

    override suspend fun linkAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential).await()
    }

    override suspend fun deleteAccount() = try {
        auth.currentUser?.delete()?.await()
        Success(true)
    } catch (e: Exception) {
        Failure(e)
    }

    override suspend fun signOut() {
//        if (auth.currentUser!!.isAnonymous) {
//            auth.currentUser!!.delete()
//        }
        try {
            auth.signOut()

            // Handle the logged-in user as needed
        } catch (e: Exception) {
            // Authentication failed
            Log.e("STATE", "Log out failed")
            // Handle the authentication failure as needed
        }
    }

    override suspend fun reloadFirebaseUser() = try {
        auth.currentUser?.reload()?.await()
        Success(true)
    } catch (e: Exception) {
        Failure(e)
    }

    override fun getAuthState(viewModelScope: CoroutineScope) = callbackFlow {
        val authStateListener = AuthStateListener { auth ->
            trySend(auth.currentUser == null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), auth.currentUser == null)

    companion object {
        private const val LINK_ACCOUNT_TRACE = "linkAccount"
    }
}