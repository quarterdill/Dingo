package com.example.dingo.navigation

sealed class Screen(val route: String) {
    object LoginScreen: Screen("LOGIN_SCREEN")
    object ForgotPasswordScreen: Screen("FORGOT_PASSWORD_SCREEN")
    object SignUpScreen: Screen("SIGN_UP_SCREEN")
    object VerifyEmailScreen: Screen("VERIFY_EMAIL_SCREEN")
    object ProfileScreen: Screen("PROFILE_SCREEN")
    object MainScreen: Screen("MAIN_SCREEN")
}