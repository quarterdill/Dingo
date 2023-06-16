package com.example.dingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.ui.theme.DingoTheme

class MainActivity : ComponentActivity() {


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
    NavHost(navController = navController, startDestination = modeSelectionScreenRoute) {
        composable(ModeSelectionButton.Standard.route) {
            MainScreen()
        }
        composable(ModeSelectionButton.Eduction.route) {
            MainScreen()
        }
        composable(modeSelectionScreenRoute) {
            ModeSelectionScreen(navController)
        }
    }
}