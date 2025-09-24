package com.example.fadebarber.data

import com.example.fadebarber.data.model.UserData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can´t be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Debes verificar tu correo para iniciar sesión")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(name: String, email: String, password: String, phone: String) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            _authState.value = AuthState.Error("All fields must be filled")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid

                    if (uid != null) {
                        val user = UserData(
                            id = uid,
                            nameUser = name,
                            correoUser = email,
                            phoneNumberUser = phone,
                            activeUser = true,
                            categoryUser = 0,
                            statusUser = 1
                        )

                        // Guardar en Realtime Database
                        database.child(uid).setValue(user)
                            .addOnSuccessListener {
                                // Enviar correo de verificación
                                firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener { emailTask ->
                                        if (emailTask.isSuccessful) {
                                            _authState.value = AuthState.EmailSent
                                        } else {
                                            _authState.value = AuthState.Error(
                                                emailTask.exception?.message
                                                    ?: "Error enviando correo"
                                            )
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                _authState.value =
                                    AuthState.Error(e.message ?: "Error saving user")
                            }
                    } else {
                        _authState.value = AuthState.Error("User ID not found")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun resetPassword(email: String, onResult: (success: Boolean, error: String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object EmailSent : AuthState()
    data class Error(val message: String) : AuthState()
}
