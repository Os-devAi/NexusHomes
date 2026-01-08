package com.nexusdev.nexushomes.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.google.firebase.auth.AuthCredential


class LoginViewModel(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onLoginSuccess(credential: AuthCredential) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = auth.currentUser
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = task.exception?.message ?: "Error desconocido"
                    )
                }
            }
    }

    fun onLoginError(message: String) {
        _state.value = _state.value.copy(error = message)
    }

    fun signOut() {
        auth.signOut()
        _state.value = LoginState()
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null
)