package com.example.dingo.authentication.signup

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.dingo.CustomSwitch
import com.example.dingo.UIConstants
import com.example.dingo.common.composable.BasicButton
import com.example.dingo.common.composable.DisplayPasswordField
import com.example.dingo.common.composable.EmailField
import com.example.dingo.common.composable.RepeatPasswordField
import com.example.dingo.common.composable.UsernameField
import com.example.dingo.navigation.Screen
import com.example.dingo.ui.theme.Purple80
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Purple80),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

        ) {
        CustomSwitch(
            "Standard",
            "Education",
            Modifier.padding(vertical = UIConstants.MEDIUM_PADDING),
            onChanged = viewModel::onButtonToggle
        )
        Log.d("STATE", uiState.accountType.toString())
        if (uiState.accountType) {
            CustomSwitch(
                "Student",
                "Instructor",
                Modifier.padding(vertical = UIConstants.MEDIUM_PADDING),
                onChanged = viewModel::onButtonToggleEducation
            )
        }
        SignUpFields(navController = navController)
    }
}
@Composable
private fun SignUpFields(
    viewModel: SignUpViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState
    val viewModelJob = Job()
    val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UIConstants.MEDIUM_PADDING)
    ) {
        EmailField(uiState.email,  viewModel::onEmailChange)
        UsernameField(uiState.username,  viewModel::onUsernameChange)
        DisplayPasswordField(uiState.password, viewModel::onPasswordChange)
        RepeatPasswordField(uiState.repeatPassword, viewModel::onRepeatPasswordChange)
        Button(
            onClick = { coroutineScope.launch {
                Log.d("STATE", "in on click")
                viewModel.onSignUpClick(navController)
            }},
            colors =
            ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Sign Up", fontSize = 16.sp)
        }
        Row {
            Text(
                modifier = Modifier.clickable {
                    navController.navigate(route = Screen.LoginScreen.route)
                },
                text = "Already have an account? Login",
                fontSize = 15.sp
            )
        }
    }

}