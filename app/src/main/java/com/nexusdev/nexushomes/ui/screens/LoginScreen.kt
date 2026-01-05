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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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

    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    // Reaccionar a cambios de estado
    LaunchedEffect(authState) {
        authViewModel.authState.collect { authState ->
            Log.d("Login Screen", "Auth state: $authState")
            when (authState) {
                is AuthState.Authenticated -> {
                    Log.d("LoginScreen", "User authenticated, navigating to home")
                    // Navegar a home cuando el usuario esté autenticado
                    navController.navigate("addNew") {
                        popUpTo("login") { inclusive = true }
                    }
                }

                is AuthState.Error -> {
                    Log.e("LoginScreen", "Authentication error: ${authState.message}")
                    // Aquí podrías mostrar un Snackbar con el error
                }

                else -> {
                    // Otros estados
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.background_login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    Log.d("LoginScreen", "Google Sign-In button clicked")

                    if (onStartGoogleSignIn != null) {
                        // Usar el callback proporcionado por MainActivity
                        onStartGoogleSignIn { idToken ->
                            if (idToken != null) {
                                Log.d(
                                    "LoginScreen",
                                    "Received ID token, signing in with Firebase"
                                )
                                authViewModel.signInWithGoogle(idToken)
                            } else {
                                Log.e("LoginScreen", "Failed to get ID token")
                            }
                        }
                    } else {
                        // Fallback: método antiguo (puede que no funcione bien)
                        Log.w("LoginScreen", "Using fallback Google Sign-In method")
                        try {
                            val signInIntent =
                                authViewModel.getGoogleSignInClient(context).signInIntent
                            val activity = context as? androidx.activity.ComponentActivity
                            activity?.startActivityForResult(signInIntent, 9001)
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Fallback method failed: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(R.drawable.google_icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Continuar con Google")
            }

            if (authState is AuthState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Iniciando sesión…")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

