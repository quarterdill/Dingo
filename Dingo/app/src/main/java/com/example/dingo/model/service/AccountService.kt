package com.example.dingo.model.service

import com.example.dingo.model.Response
import com.example.dingo.model.User
import kotlinx.coroutines.flow.Flow
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

// Taken from https://github.com/FirebaseExtended/make-it-so-android/blob/main/start/app/src/main/java/com/example/makeitso/model/service/AccountService.kt

typealias SendEmailVerificationResponse = Response<Boolean>
typealias SignUpResponse = Response<Boolean>
typealias SignInResponse = Response<Boolean>
typealias ReloadUserResponse = Response<Boolean>
typealias SendRecoveryResponse = Response<Boolean>
typealias DeleteAccountResponse = Response<Boolean>
typealias AuthStateResponse = StateFlow<Boolean>
interface AccountService {
    val currentUserId: String
    val hasUser: Boolean
    val currentUser: FirebaseUser?

    suspend fun registerUser(email: String, password: String, callback: (Boolean, String) -> Unit)
    suspend fun loginUser(email: String, password: String): Boolean
    suspend fun sendEmailVerification(): SendEmailVerificationResponse
    suspend fun sendRecoveryEmail(email: String): SendRecoveryResponse
    suspend fun linkAccount(email: String, password: String)
    suspend fun deleteAccount(): DeleteAccountResponse
    suspend fun signOut(): Boolean
    suspend fun reloadFirebaseUser(): ReloadUserResponse
    fun getAuthState(viewModelScope: CoroutineScope): AuthStateResponse
}