package com.example.dingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.authentication.login.LoginScreen
import com.example.dingo.authentication.login.LoginViewModel
import com.example.dingo.ui.theme.DingoTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.dingo.authentication.signup.SignUpScreen
import com.example.dingo.authentication.signup.SignUpViewModel
import com.example.dingo.navigation.NavGraph
import com.example.dingo.navigation.Screen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private val viewModel: MainViewModel by viewModels()
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberNavController()
            DingoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navigationConfiguration2(navController)
                }
            }
        }
    }

    @Composable
    private fun AuthState(navController: NavHostController) {
        val isUserSignedOut = viewModel.getAuthState().collectAsState().value
        if (isUserSignedOut) {
            navController.navigate(Screen.LoginScreen.route)
        }
        navController.navigate(Screen.LoginScreen.route)
//        else {
//            if (viewModel.isEmailVerified) {
//                NavigateToMainScreen()
//            } else {
//                NavigateToVerifyEmailScreen()
//            }
//        }
    }
    @Composable
    private fun NavigateToLoginScreen()  {
        navController.navigate(Screen.LoginScreen.route)
//        popUpTo(navController.graph.id) {
//            inclusive = true
//        }
    }
    @Composable
    private fun NavigateToMainScreen() = navController.navigate(Screen.MainScreen.route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }
//
//    @Composable
//    private fun NavigateToVerifyEmailScreen() = navController.navigate(VerifyEmailScreen.route) {
//        popUpTo(navController.graph.id) {
//            inclusive = true
//        }
//    }

    @Composable
    private fun navigationConfiguration2 (navController: NavHostController) {
        NavHost(navController = navController, startDestination = "auth_checker") {
            composable(route = "auth_checker") {
                AuthState(navController = navController)
            }
            composable(route = Screen.LoginScreen.route) {
                LoginScreen(navController = navController)
            }
            composable(route = Screen.SignUpScreen.route) {
                SignUpScreen(navController = navController)
            }
            composable("mainScreen") {
                MainScreen()
            }

        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    DingoTheme {
        Greeting("Android")
    }
}

@Composable
private fun navigationConfiguration(navController: NavHostController) {
    val modeSelectionScreenRoute = "mode_selection_screen"
    val loginViewModel: LoginViewModel = viewModel()
    val signUpViewModel: SignUpViewModel = viewModel()
    NavHost(navController = navController, startDestination = modeSelectionScreenRoute) {
        composable(ModeSelectionButton.Standard.route) {
            LoginScreen(loginViewModel,navController)
        }
        composable(ModeSelectionButton.Education.route) {
            SignUpScreen(signUpViewModel,navController)
        }
        composable(modeSelectionScreenRoute) {
            ModeSelectionScreen(navController)
        }
        composable("mainScreen") {
            MainScreen()
        }

    }
}