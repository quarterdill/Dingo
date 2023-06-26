package com.example.dingo.authentication.signup

data class SignUpUIState (
    var email: String = "",
    var password: String = "",
    var repeatPassword: String = "",
)