package com.example.dingo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dingo.authentication.login.LoginScreen

@Composable
@ExperimentalComposeUiApi
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(
            route = Screen.LoginScreen.route
        ) {
            LoginScreen(
                navController = navController
            )
        }
//        composable(
//            route = ForgotPasswordScreen.route
//        ) {
//            ForgotPasswordScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                }
//            )
//        }
//        composable(
//            route = SignUpScreen.route
//        ) {
//            SignUpScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                }
//            )
//        }
//        composable(
//            route = VerifyEmailScreen.route
//        ) {
//            VerifyEmailScreen(
//                navigateToProfileScreen = {
//                    navController.navigate(ProfileScreen.route) {
//                        popUpTo(navController.graph.id) {
//                            inclusive = true
//                        }
//                    }
//                }
//            )
//        }
//        composable(
//            route = ProfileScreen.route
//        ) {
//            ProfileScreen()
//        }
    }
}