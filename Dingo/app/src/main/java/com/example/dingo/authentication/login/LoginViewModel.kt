package com.example.dingo.authentication.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.StatName
import com.example.dingo.common.incrementStat
import com.example.dingo.common.initializeStats
import com.example.dingo.common.isValidEmail
import com.example.dingo.common.isValidPassword
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.UserService
import com.example.dingo.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val accountService: AccountService,
    private val userService: UserService,
) : ViewModel() {
    private val _uiState = mutableStateOf(LoginUIState())
    val uiState: State<LoginUIState> = _uiState

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    fun onEmailChange(newValue: String) {
        _uiState.value = _uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        _uiState.value = _uiState.value.copy(password = newValue)
    }

    suspend fun onSignInClick(navHostController: NavHostController) {
        Log.d("STATE", "In on click model")
        if (!email.isValidEmail()) {
            Log.d("STATE","Not a valid email")
            return
        }

//        if (!password.isValidPassword()) {
//            Log.d("STATE", password)
//            Log.d("STATE","Not a valid password")
//            return
//        }
        Log.d("STATE","trying to login")
        val successfulLogin = accountService.loginUser(email, password)
        if (successfulLogin) {
            val user = userService.getUserByEmail(email)
            SessionInfo.currentUser = user
            if (user != null) {
                SessionInfo.currentUsername = user.username
                SessionInfo.currentUserID = user.id
            }
            initializeStats()
            incrementStat(StatName.LOGINS)
            navHostController.navigate(route = Screen.MainScreen.route)

        }
    }


}