package com.example.dingo.authentication.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            singleLine = true,
            value = uiState.email,
            onValueChange = {viewModel.onEmailChange(uiState.email)},
            placeholder = { Text(text="Email") },
        )
        TextField(
            singleLine = true,
            value = uiState.password,
            onValueChange = {viewModel.onPasswordChange(uiState.password)},
            placeholder = { Text(text="Password") },
        )
        TextField(
            singleLine = true,
            value = uiState.repeatPassword,
            onValueChange = {viewModel.onRepeatPasswordChange(uiState.repeatPassword)},
            placeholder = { Text(text="Repeat Password") },
        )
    }
}