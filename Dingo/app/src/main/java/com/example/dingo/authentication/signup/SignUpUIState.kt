package com.example.dingo.authentication.signup

data class SignUpUIState (
    var email: String = "",
    var password: String = "",
    var repeatPassword: String = "",
    var username: String = "",
    var accountType: Boolean = false,
    var educationType: Boolean = false
)