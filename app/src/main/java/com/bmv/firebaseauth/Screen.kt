package com.bmv.firebaseauth

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Splash : Screen("splash")
    object ResetPassword : Screen("reset_password")
}
