package com.example.fadebarber.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _errorEvent = MutableSharedFlow<Event<String>>()
    val errorEvent = _errorEvent.asSharedFlow()

    // Listener para monitorear cambios en statusUser
    private var statusUserListener: ValueEventListener? = null
    private var currentUserId: String? = null

    //Variables de signup
    private val _registerName = MutableStateFlow("")
    private val _registerPhone = MutableStateFlow("")
    private val _registerEmail = MutableStateFlow("")
    private val _registerPassword = MutableStateFlow("")
    private val _registerConfirmPassword = MutableStateFlow("")
    private val _termsAccepted = MutableStateFlow(false)

    val registerName = _registerName.asStateFlow()
    val registerPhone = _registerPhone.asStateFlow()
    val registerEmail = _registerEmail.asStateFlow()
    val registerPassword = _registerPassword.asStateFlow()
    val registerConfirmPassword = _registerConfirmPassword.asStateFlow()
    val termsAccepted = _termsAccepted.asStateFlow()
    //Terminan las variables

    init {
        checkAuthStatus()
    }

    fun updateRegisterName(value: String) { _registerName.value = value }
    fun updateRegisterPhone(value: String) { _registerPhone.value = value }
    fun updateRegisterEmail(value: String) { _registerEmail.value = value }
    fun updateRegisterPassword(value: String) { _registerPassword.value = value }
    fun updateRegisterConfirmPassword(value: String) { _registerConfirmPassword.value = value }
    fun updateTermsAccepted(value: Boolean) { _termsAccepted.value = value }


    fun clearRegisterForm() {
        _registerName.value = ""
        _registerPhone.value = ""
        _registerEmail.value = ""
        _registerPassword.value = ""
        _registerConfirmPassword.value = ""
        _termsAccepted.value = false
    }

    // Configurar listener para monitorear statusUser en tiempo real
    private fun setupStatusUserListener(userId: String) {
        // Remover listener previo si existe
        removeStatusUserListener()

        currentUserId = userId
        statusUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val statusUser = snapshot.child("statusUser").getValue(Int::class.java) ?: 0

                if (statusUser != 1) {
                    // Usuario desactivado, cerrar sesión inmediatamente
                    Log.d("AuthViewModel", "Usuario desactivado detectado, cerrando sesión")
                    viewModelScope.launch {
                        _errorEvent.emit(Event("Tu cuenta ha sido desactivada. Contacta al administrador."))
                    }
                    logout()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AuthViewModel", "Error al monitorear statusUser: ${error.message}")
            }
        }

        // Agregar listener a Firebase
        database.child(userId).addValueEventListener(statusUserListener!!)
    }

    // Remover listener
    private fun removeStatusUserListener() {
        currentUserId?.let { userId ->
            statusUserListener?.let { listener ->
                database.child(userId).removeEventListener(listener)
            }
        }
        statusUserListener = null
        currentUserId = null
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
            removeStatusUserListener()
        } else {
            viewModelScope.launch {
                try {
                    // Obtener datos del usuario desde Firebase
                    val snapshot = database.child(currentUser.uid).get().await()

                    val statusUser = snapshot.child("statusUser").getValue(Int::class.java) ?: 0
                    val role = snapshot.child("categoryUser").getValue(Int::class.java) ?: 0

                    // Verificar si el usuario está activo (statusUser = 1)
                    if (statusUser != 1) {
                        // Usuario inactivo o bloqueado, cerrar sesión
                        logout()
                        _authState.postValue(AuthState.Error("Tu cuenta ha sido desactivada. Contacta al administrador."))
                        return@launch
                    }

                    // Usuario activo, configurar listener para monitorear cambios
                    setupStatusUserListener(currentUser.uid)

                    // Usuario activo, verificar rol guardado
                    val savedRole = UserPreferences.getUserRole(appContext).firstOrNull()

                    if (savedRole != null && savedRole == role) {
                        _authState.postValue(AuthState.Authenticated(role))
                    } else {
                        // Actualizar rol en DataStore
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
            _authState.value = AuthState.Error("Los campos correo electrónico y contraseña no pueden estar vacíos")
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
                                val statusUser = snapshot.child("statusUser").getValue(Int::class.java) ?: 0

                                // Verificar si el usuario está activo
                                if (statusUser != 1) {
                                    auth.signOut()
                                    _authState.value = AuthState.Error("Tu cuenta ha sido desactivada. Contacta al administrador.")
                                    return@addOnSuccessListener
                                }

                                val role = snapshot.child("categoryUser").getValue(Int::class.java) ?: 0
                                val emailChangePending = snapshot.child("emailChangePending").getValue(Boolean::class.java) ?: false
                                val pendingEmail = snapshot.child("pendingEmail").getValue(String::class.java)

                                // Configurar listener para monitorear cambios en statusUser
                                setupStatusUserListener(uid)

                                if (emailChangePending) {
                                    val isNewEmailVerified = user.isEmailVerified && pendingEmail != null && pendingEmail == user.email
                                    if (isNewEmailVerified) {
                                        val newCorreo = user.email ?: pendingEmail ?: ""
                                        database.child(uid)
                                            .updateChildren(
                                                mapOf(
                                                    "correoUser" to newCorreo,
                                                    "emailChangePending" to false,
                                                    "pendingEmail" to ""
                                                )
                                            )
                                            .addOnCompleteListener {
                                                viewModelScope.launch {
                                                    UserPreferences.saveUserRole(appContext, role)
                                                    NotificationHelper.saveUserToken()
                                                }
                                                _authState.value = AuthState.Authenticated(role)
                                            }
                                    } else {
                                        auth.signOut()
                                        removeStatusUserListener()
                                        _authState.value = AuthState.Error("Debes confirmar tu nuevo correo para iniciar sesión")
                                    }
                                } else {
                                    viewModelScope.launch {
                                        UserPreferences.saveUserRole(appContext, role)
                                        NotificationHelper.saveUserToken()
                                    }
                                    _authState.value = AuthState.Authenticated(role)
                                }
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error("Error al obtener rol: ${e.message}")
                            }
                    } else {
                        _authState.value = AuthState.Error("Debes verificar tu correo para iniciar sesión")
                    }
                } else {
                    // Filtrar el mensaje original de Firebase
                    val errorMessage = when (task.exception?.message) {
                        "The supplied auth credential is incorrect, malformed or has expired." ->
                            "Correo o contraseña incorrectos"
                        "There is no user record corresponding to this identifier. The user may have been deleted." ->
                            "No existe una cuenta con este correo"
                        else -> "Credenciales incorrectas"
                    }

                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }

    fun signup(name: String, email: String, password: String, phone: String, profileImageUrl: String = "") {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            _authState.value = AuthState.Error("Todos los campos deben de ser llenados.")
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
                            statusUser = 1,
                            photoURL = profileImageUrl // URL de imagen (puede estar vacía)
                        )

                        database.child(uid).setValue(user)
                            .addOnSuccessListener {
                                firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener { emailTask ->
                                        if (emailTask.isSuccessful) {
                                            _authState.value = AuthState.EmailSent
                                        } else {
                                            _authState.value = AuthState.Error("No se pudo enviar el correo de verificación")
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error("Error al registrar usuario: ${e.message}")
                            }
                    } else {
                        _authState.value = AuthState.Error("No se pudo obtener el usuario")
                    }
                } else {
                    _authState.value = AuthState.Error("Error en el registro: ${task.exception?.message}")
                }
            }
    }

    fun resetPassword(email: String, onResult: (success: Boolean, error: String?) -> Unit) {
        if (email.isEmpty()) {
            onResult(false, "Correo electrónico requerido")
            return
        }

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
        removeStatusUserListener()
        NotificationHelper.removeUserToken()
        auth.signOut()
        viewModelScope.launch {
            UserPreferences.saveUserRole(appContext, 0)
        }
        _authState.value = AuthState.Unauthenticated
    }

    fun resetAuthState() {
        _authState.value = AuthState.Unauthenticated
    }

    fun prepareForSignUp() {
        _authState.value = AuthState.Unauthenticated
    }

    // Invitado
    fun loginAsGuest() {
        _authState.value = AuthState.Guest
    }

    override fun onCleared() {
        super.onCleared()
        removeStatusUserListener()
    }
}

sealed class AuthState {
    data class Authenticated(val role: Int) : AuthState() // 0 cliente, 1 admin, 2 empleado
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object EmailSent : AuthState()
    data class Error(val message: String) : AuthState()
    object Guest : AuthState()
}

class Event<T> {
    private var content: T? = null
    private var hasBeenHandled = false

    constructor(content: T) {
        this.content = content
    }

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T? = content

    fun consumeContent() {
        hasBeenHandled = true
        content = null
    }
}