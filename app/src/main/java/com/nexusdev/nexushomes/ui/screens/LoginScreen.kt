@file:Suppress("DEPRECATION")
package com.nexusdev.nexushomes.ui.screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.nexusdev.nexushomes.R
import com.nexusdev.nexushomes.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel()) {
    val state by viewModel.state
    val context = LocalContext.current

    // Configuración básica de Google SignIn (requiere el web_client_id de tu google-services.json)
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel.onLoginSuccess(credential)
        } catch (e: ApiException) {
            viewModel.onLoginError("Fallo en Google: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.user != null) {
            Text("Bienvenido: ${state.user?.displayName}")
            Button(onClick = { viewModel.signOut() }) { Text("Cerrar Sesión") }
        } else {
            Button(onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
                Text("Iniciar sesión con Google")
            }
        }

        state.error?.let { Text("Error: $it", color = Color.Red) }
    }
}

