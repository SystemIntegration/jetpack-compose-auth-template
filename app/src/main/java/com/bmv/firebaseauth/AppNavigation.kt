package com.bmv.firebaseauth

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

var userName = mutableStateOf("")
var userEmail = mutableStateOf("")
var userPassword = mutableStateOf("")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route){
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = { email, password ->
                        Log.e("FirebaseAuth","Handle Login Here...")
                        val auth = FirebaseAuth.getInstance()

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { loginTask ->
                                if (loginTask.isSuccessful) {
                                    Toast.makeText(context, "Login Successful.", Toast.LENGTH_SHORT).show()
                                    // Login successful, navigate to the home screen or perform other actions
                                    val auth = FirebaseAuth.getInstance()
                                    val firestore = FirebaseFirestore.getInstance()
                                    val user = auth.currentUser
                                    val userCollection = firestore.collection("users")
                                    userCollection.document(user!!.uid).get().addOnSuccessListener {
                                        if (it.exists()){
                                            var user = it.toObject(User::class.java)
                                            if (user != null) {
                                                userName.value = user.name
                                                userEmail.value = user.email
                                                userPassword.value = user.password
                                                navController.navigate(Screen.Home.route)
                                            }
                                        }
                                    }.addOnFailureListener(){
                                        Toast.makeText(context, "User Info not found", Toast.LENGTH_SHORT).show()
                                    }

                                } else {
                                    val exception = loginTask.exception
                                    if (exception is FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(context, "Please enter correct credentials..", Toast.LENGTH_SHORT).show()
                                    } else if (exception is FirebaseAuthException) {
                                        Toast.makeText(context, "Firebase Exception $exception", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Something went wrong..", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                    }, onForgotPasswordClick = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }, onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = { name, email, password ->
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { registrationTask ->
                            if (registrationTask.isSuccessful) {
                                val user = auth.currentUser
                                user?.let {
                                    val userData = hashMapOf(
                                        "name" to name,
                                        "email" to email,
                                        "password" to password
                                    )

                                    val userCollection = firestore.collection("users")
                                    userCollection.document(user.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            userName.value = name
                                            userEmail.value = email
                                            userPassword.value = password
                                            navController.navigate(Screen.Login.route)
                                            // Registration and user data saved successfully
                                            // You can navigate to the home screen or perform any other action
                                        }
                                        .addOnFailureListener { exception ->
                                            // Handle failure in saving user data to Firestore
                                        }
                                }
                            } else {
                                // Handle failure in user registration with Firebase Authentication
                            }
                        }

                    Log.e("FirebaseAuth","Handle Register here....")

                    // Handle registration logic and navigate to appropriate screen

//                    if (registrationSuccessful) {
//                        navController.navigate(Screen.Login.route) // Example navigation
//                    }
                }, onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onResetPasswordClick = { email ->
                    val auth = FirebaseAuth.getInstance()
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { resetPasswordTask ->
                            if (resetPasswordTask.isSuccessful) {
                                Toast.makeText(context, "reset password link is sent to your email.", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.Login.route)
                                // Password reset email sent successfully, handle accordingly
                                // For example, show a success message or navigate to a confirmation screen
                            } else {
                                val exception = resetPasswordTask.exception
                                if (exception is FirebaseAuthInvalidUserException) {
                                    Toast.makeText(context, "Exception ... $exception", Toast.LENGTH_SHORT).show()
                                    // User does not exist or email is invalid, show appropriate error message
                                } else {

                                    // General error, handle accordingly
                                }
                            }
                        }
                    Log.e("FirebaseAuth","Handle Forgot password here...")


                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(user = User(userName.value, userEmail.value, userPassword.value), onResetPasswordClick ={
               navController.navigate(Screen.ResetPassword.route)
            })
        }

        composable(Screen.ResetPassword.route){
            ResetPasswordScreen(
                onResetPasswordClick= { password ->
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    val credential = EmailAuthProvider.getCredential(user?.email!!, userPassword.value)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.updatePassword(password)
                                    ?.addOnCompleteListener { resetPasswordTask ->
                                        if (resetPasswordTask.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Password updated Successfully..",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate(Screen.Home.route)
                                            // Password reset successful, handle accordingly
                                            // For example, show a success message or navigate to another screen
                                        } else {
                                            val exception = resetPasswordTask.exception
                                            if (exception is FirebaseAuthInvalidCredentialsException) {
                                                Toast.makeText(
                                                    context,
                                                    "Something went wrong.. $exception",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Invalid password, show appropriate error message
                                            } else {
                                                Log.e(
                                                    "ResetPasswordError",
                                                    "Error: ${exception?.message}"
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "This is general error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // General error, handle accordingly
                                            }
                                        }
                                    }
                            }
                        }
                }
            )
        }
    }
}
