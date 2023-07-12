package com.bmv.firebaseauth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        val loggedIn = !FirebaseAuth.getInstance().uid.isNullOrEmpty()
        if (loggedIn){
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
                    }
                }
            }
        }
        delay(4000) // Delay for 3 seconds
        if (loggedIn){
            navController.navigate(Screen.Home.route){
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }else{
            navController.navigate(Screen.Login.route){
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Blue),
        contentAlignment = Alignment.Center

    ) {
        // Background color
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "BMV Auth",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = Color.Red // Replace with your desired text color
                )
            )
        }
    }
}
