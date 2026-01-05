package com.nexusdev.nexushomes.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexusdev.nexushomes.ui.viewmodel.PhoneAuthState
import com.nexusdev.nexushomes.ui.viewmodel.PhoneAuthViewModel

@Composable
fun PhoneLoginScreen(
    viewModel: PhoneAuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as Activity

    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        if (state == PhoneAuthState.Idle) {
            TextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Número de teléfono") }
            )

            Button(
                onClick = {
                    viewModel.sendCode(phone, activity)
                }
            ) {
                Text("Enviar código")
            }
        }

        if (state == PhoneAuthState.CodeSent) {
            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código SMS") }
            )

            Button(
                onClick = {
                    viewModel.verifyCode(code)
                }
            ) {
                Text("Verificar")
            }
        }

        if (state is PhoneAuthState.Error) {
            Text(
                (state as PhoneAuthState.Error).message,
                color = Color.Red
            )
        }

        if (state == PhoneAuthState.Success) {
            Text("Login exitoso ✅")
        }
    }
}
