package com.example.dingo.authentication.signup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dingo.common.isValidEmail
import com.example.dingo.common.isValidPassword
import com.example.dingo.common.passwordMatches
import com.example.dingo.model.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel
@Inject
constructor(
    private val accountService: AccountService,
) : ViewModel() {
    var uiState = mutableStateOf(SignUpUIState())
        private set

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onRepeatPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(repeatPassword = newValue)
    }

    suspend fun onSignUpClick() {
        if (!email.isValidEmail()) {
            print("Not a valid email")
            return
        }

        if (!password.isValidPassword()) {
            print("Not a valid password")
            return
        }

        if (!password.passwordMatches(uiState.value.repeatPassword)) {
            print("passwords don't match")
            return
        }
        accountService.registerUser(email, password);
    }
}