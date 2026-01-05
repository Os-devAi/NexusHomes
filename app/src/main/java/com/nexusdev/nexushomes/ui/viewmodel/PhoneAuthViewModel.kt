package com.nexusdev.nexushomes.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class PhoneAuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val state: StateFlow<PhoneAuthState> = _state

    private var verificationId: String? = null

    fun sendCode(
        phone: String,
        activity: Activity
    ) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        _state.value = PhoneAuthState.CodeSent
    }

    fun verifyCode(code: String) {
        val credential =
            PhoneAuthProvider.getCredential(verificationId!!, code)

        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                _state.value = PhoneAuthState.Success
            } catch (e: Exception) {
                _state.value = PhoneAuthState.Error(e.message ?: "Error")
            }
        }
    }

    private val callbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(
                credential: PhoneAuthCredential
            ) {
                viewModelScope.launch {
                    auth.signInWithCredential(credential).await()
                    _state.value = PhoneAuthState.Success
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.value =
                    PhoneAuthState.Error(e.message ?: "Error")
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                _state.value = PhoneAuthState.CodeSent
            }
        }
}

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object CodeSent : PhoneAuthState()
    object Success : PhoneAuthState()
    data class Error(val message: String) : PhoneAuthState()
}
