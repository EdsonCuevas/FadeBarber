package com.example.fadebarber.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    private val _promotions = MutableStateFlow<List<PromotionData>>(emptyList())
    private val _barbers = MutableStateFlow<List<UserData>>(emptyList())
    private val _info = MutableStateFlow<BarberInfo?>(null)
    private val _currentUser = MutableStateFlow<UserData?>(null)

    // NUEVO: Estado del carrito
    private val _cartItems = MutableStateFlow<List<Any>>(emptyList())

    val services: StateFlow<List<ServiceData>> = _services
    val promotions: StateFlow<List<PromotionData>> = _promotions
    val info: StateFlow<BarberInfo?> = _info
    val currentUser: StateFlow<UserData?> = _currentUser
    val cartItems: StateFlow<List<Any>> = _cartItems

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")

    // Referencia para el listener de información del barbero
    private var barberInfoListener: ValueEventListener? = null
    private var barberInfoReference: com.google.firebase.database.DatabaseReference? = null

    init {
        viewModelScope.launch {
            _services.value = FirebaseRepository.getServices()
            _promotions.value = FirebaseRepository.getPromotions()
            _barbers.value = FirebaseRepository.getBarbers()
            _info.value = FirebaseRepository.getBarberInfo()
            loadCurrentUser()
        }
    }

    fun loadCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        database.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserData::class.java)
                _currentUser.value = user
            }
            .addOnFailureListener {
                _currentUser.value = null
            }
    }

    // NUEVO: Iniciar listener en tiempo real para información del barbero
    fun listenToBarberInfo() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("HomeViewModel", "Usuario no autenticado")
            return
        }

        val userId = firebaseUser.uid
        barberInfoReference = FirebaseDatabase.getInstance().getReference("Barber/$userId")

        // Crear el listener
        barberInfoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val barberData = snapshot.getValue(BarberInfo::class.java)
                    barberData?.let {
                        _info.value = it
                        Log.d("HomeViewModel", "Información de barbero actualizada en tiempo real")
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error al actualizar información de barbero", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeViewModel", "Error en listener de barbero: ${error.message}")
            }
        }

        // Adjuntar el listener
        barberInfoListener?.let {
            barberInfoReference?.addValueEventListener(it)
            Log.d("HomeViewModel", "Listener de horarios iniciado")
        }
    }

    // NUEVO: Detener listener cuando ya no sea necesario
    fun stopListeningToBarberInfo() {
        barberInfoListener?.let { listener ->
            barberInfoReference?.removeEventListener(listener)
            Log.d("HomeViewModel", "Listener de horarios detenido")
        }
        barberInfoListener = null
        barberInfoReference = null
    }

    // Funciones para manejar el carrito
    fun addToCart(item: Any) {
        _cartItems.value = _cartItems.value + item
    }

    fun removeFromCart(item: Any) {
        _cartItems.value = _cartItems.value - item
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartItemsCount(): Int = _cartItems.value.size

    // Limpiar recursos cuando el ViewModel se destruya
    override fun onCleared() {
        super.onCleared()
        stopListeningToBarberInfo()
        Log.d("HomeViewModel", "ViewModel limpiado")
    }
}