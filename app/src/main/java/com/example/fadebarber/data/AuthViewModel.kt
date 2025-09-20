package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Estado del usuario actual
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    init {
        // Escucha cambios en la sesión (login/logout)
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
        }
    }

    /**
     * Iniciar sesión con email y password
     */
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _user.value = auth.currentUser
                        onResult(true, null)
                    } else {
                        _user.value = null
                        onResult(false, task.exception?.message)
                    }
                }
        }
    }

    /**
     * Registrar un nuevo usuario en Firebase
     */
    fun signUp(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _user.value = auth.currentUser
                        onResult(true, null)
                    } else {
                        _user.value = null
                        onResult(false, task.exception?.message)
                    }
                }
        }
    }

    /**
     * Restablecer contraseña
     */
    fun resetPassword(email: String, onResult: (success: Boolean, error: String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }

        /**
         * Cerrar sesión
         */
        fun logout() {
            auth.signOut()
            _user.value = null
        }
    }
}
