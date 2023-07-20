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
import androidx.navigation.NavHostController
import com.example.dingo.common.composable.BasicButton
import com.example.dingo.common.composable.DisplayPasswordField
import com.example.dingo.common.composable.EmailField
import com.example.dingo.common.composable.RepeatPasswordField

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmailField(uiState.email,  viewModel::onEmailChange)
        DisplayPasswordField(uiState.password, viewModel::onPasswordChange)
        RepeatPasswordField(uiState.repeatPassword, viewModel::onRepeatPasswordChange)
        BasicButton("Sign Up") {viewModel::onSignUpClick}
//        BasicButton("Sign Up") { navController.navigate("mainScreen") }
    }
}