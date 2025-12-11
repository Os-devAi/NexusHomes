@file:Suppress("DEPRECATION")

package com.nexusdev.nexushomes.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nexusdev.nexushomes.R
import com.nexusdev.nexushomes.ui.viewmodel.AuthState
import com.nexusdev.nexushomes.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    modifier: Modifier,
    navController: NavController,
    onStartGoogleSignIn: (((String?) -> Unit) -> Unit)? = null
) {

    //aux variables
    var sizeImage by remember { mutableStateOf(IntSize.Zero) }
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.White),
        startY = sizeImage.height.toFloat() / 3,  // 1/3
        endY = sizeImage.height.toFloat()
    )


    // init activity to show if logged in
    LaunchedEffect(authViewModel.authState) {
        authViewModel.authState.collect { authState ->
            when (authState) {
                is AuthState.Authenticated -> {
                    // Navegar a home cuando el usuario esté autenticado
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }

                is AuthState.Error -> {
                    Log.e("OnBoardingOne", "Authentication error: ${authState.message}")
                    // Aquí podrías mostrar un Snackbar con el error
                }

                else -> {
                    // Otros estados
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.background_login),
            contentDescription = "Just a background for login",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Google Sign-In Button
            Button(
                onClick = {
                    if (onStartGoogleSignIn != null) {
                        // Usar el callback proporcionado por MainActivity
                        onStartGoogleSignIn { idToken ->
                            if (idToken != null) {
                                Log.d(
                                    "OnBoardingOne",
                                    "Received ID token, signing in with Firebase"
                                )
                                authViewModel.signInWithGoogle(idToken)
                            } else {
                                Log.e("OnBoardingOne", "Failed to get ID token")
                            }
                        }
                    } else {
                        // Fallback: método antiguo (puede que no funcione bien)
                        Log.w("OnBoardingOne", "Using fallback Google Sign-In method")
                        try {
                            val signInIntent =
                                authViewModel.getGoogleSignInClient(context).signInIntent
                            val activity = context as? androidx.activity.ComponentActivity
                            activity?.startActivityForResult(signInIntent, 9001)
                        } catch (e: Exception) {
                            Log.e("OnBoardingOne", "Fallback method failed: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Gray
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                // Google icon (you might want to use a proper Google icon resource)
                Image(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = "Google icon",
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Continuar con google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
