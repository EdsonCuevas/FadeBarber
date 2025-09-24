package com.example.fadebarber.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")
    private val firestore = FirebaseFirestore.getInstance()
    private val appContext = getApplication<Application>().applicationContext

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            viewModelScope.launch {
                try {
                    // 1. Revisar si ya hay un rol guardado en DataStore
                    val savedRole = UserPreferences.getUserRole(appContext).firstOrNull()

                    if (savedRole != null) {
                        _authState.postValue(AuthState.Authenticated(savedRole))
                    } else {
                        // 2. Si no existe en DataStore, obtener de Firebase
                        val role = database.child(currentUser.uid)
                            .get()
                            .await()
                            .child("categoryUser")
                            .getValue(Int::class.java) ?: 0

                        // Guardar en DataStore
                        UserPreferences.saveUserRole(appContext, role)

                        _authState.postValue(AuthState.Authenticated(role))
                    }
                } catch (e: Exception) {
                    _authState.postValue(AuthState.Error(e.message ?: "Error al verificar sesión"))
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can’t be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        val uid = user.uid
                        database.child(uid).get()
                            .addOnSuccessListener { snapshot ->
                                val role = snapshot.child("categoryUser").getValue(Int::class.java) ?: 0

                                viewModelScope.launch {
                                    UserPreferences.saveUserRole(appContext, role)
                                }

                                _authState.value = AuthState.Authenticated(role)
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error("Error al obtener rol: ${e.message}")
                            }
                    } else {
                        _authState.value = AuthState.Error("Debes verificar tu correo para iniciar sesión")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
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
                            categoryUser = 1, // Cliente por defecto
                            statusUser = 1
                        )

                        database.child(uid).setValue(user)
                            .addOnSuccessListener {
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
                                _authState.value = AuthState.Error(e.message ?: "Error saving user")
                            }
                    } else {
                        _authState.value = AuthState.Error("User ID not found")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
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

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            UserPreferences.saveUserRole(appContext, -1) // reset
            _authState.postValue(AuthState.Unauthenticated)
        }
    }
}

sealed class AuthState {
    data class Authenticated(val role: Int) : AuthState() // 0 cliente, 1 admin, 2 empleado
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object EmailSent : AuthState()
    data class Error(val message: String) : AuthState()
}
