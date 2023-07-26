package com.example.dingo.authentication.signup

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.dingo.common.isValidEmail
import com.example.dingo.common.isValidPassword
import com.example.dingo.common.passwordMatches
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.UserService
import com.example.dingo.navigation.Screen
import com.example.dingo.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel
@Inject
constructor(
    private val accountService: AccountService,
    private val userService: UserService
) : ViewModel() {
    var uiState = mutableStateOf(SignUpUIState())
        private set

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    fun onButtonToggle(type: Boolean) {
        uiState.value = uiState.value.copy(accountType = type)
        Log.d("STATE", type.toString())
    }

    fun onButtonToggleEducation(type: Boolean) {
        uiState.value = uiState.value.copy(educationType = type)
        Log.d("STATE", type.toString())
    }
    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onRepeatPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(repeatPassword = newValue)
    }

    suspend fun onSignUpClick(navController: NavController) {
        if (!email.isValidEmail()) {
            print("Not a valid email")
            return
        }

//        if (!password.isValidPassword()) {
//            print("Not a valid password")
//            return
//        }

        if (!password.passwordMatches(uiState.value.repeatPassword)) {
            Log.d("STATE", "passwords don't match")
            return
        }
        val successfulSignup = accountService.registerUser(email, password)

        if (successfulSignup) {
            navController.navigate(route = Screen.LoginScreen.route)
            var accountType: AccountType = AccountType.STANDARD
            if (uiState.value.accountType) {
                accountType = if (uiState.value.educationType) {
                    AccountType.INSTRUCTOR
                } else {
                    AccountType.STUDENT
                }
            }
            userService.createUser(uiState.value.email,uiState.value.email,accountType)
        }
    }
}