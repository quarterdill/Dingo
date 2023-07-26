package com.example.dingo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import com.example.dingo.authentication.signup.SignUpScreen
import com.example.dingo.authentication.signup.SignUpViewModel
import com.example.dingo.common.SessionInfo
import com.example.dingo.common.initializeStats
import com.example.dingo.navigation.NavGraph
import com.example.dingo.navigation.Screen
import com.example.dingo.ui.theme.Purple80


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private val viewModel: MainViewModel by viewModels()
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isLoading = viewModel.isLoading.observeAsState()
            viewModel.getUser()
            viewModel.setUpDingoDex(LocalContext.current)
            viewModel.setUpAchievements(LocalContext.current)
            println("ACHIEVEMENTS: : set up with user ${SessionInfo.currentUser}")
            initializeStats()
            navController = rememberNavController()
            DingoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Purple80),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoading.value!!) {
                            CircularProgressIndicator()
                        } else {
                            navigationConfiguration2(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        viewModel.updateUserStats()
    }

    @Composable
    private fun AuthState(navController: NavHostController) {
        val isUserSignedOut = viewModel.getAuthState().collectAsState().value
        Log.d("STATE", isUserSignedOut.toString())
        if (isUserSignedOut) {
            navController.navigate(Screen.LoginScreen.route)
        } else {
            navController.navigate(Screen.MainScreen.route)
        }
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
            composable(route = Screen.MainScreen.route) {
                MainScreen(navControllerSignOut = navController)
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
            LoginScreen(navController = navController)
        }
        composable(ModeSelectionButton.Education.route) {
            SignUpScreen(navController = navController)
        }
        composable(modeSelectionScreenRoute) {
            ModeSelectionScreen(navController)
        }
        composable("mainScreen") {
            MainScreen(navControllerSignOut = navController)
        }

    }
}