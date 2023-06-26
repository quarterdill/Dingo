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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.authentication.login.LoginScreen
import com.example.dingo.authentication.login.LoginViewModel
import com.example.dingo.dingodex.DingoDexScreen
import com.example.dingo.ui.theme.DingoTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            DingoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navigationConfiguration(navController)
                }
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
    NavHost(navController = navController, startDestination = modeSelectionScreenRoute) {
        composable(ModeSelectionButton.Standard.route) {
            LoginScreen(loginViewModel,navController)
        }

        composable(ModeSelectionButton.Education.route) {
            MainScreen()
        }
        composable(modeSelectionScreenRoute) {
            ModeSelectionScreen(navController)
        }
        composable("mainScreen") {
            MainScreen()
        }

    }
}