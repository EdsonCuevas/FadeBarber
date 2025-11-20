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
    private val _cartItems = MutableStateFlow<List<Any>>(emptyList())
    private val _appointments = MutableStateFlow<List<com.example.fadebarber.data.model.AppointmentClientData>>(emptyList())
    private val _cartNotice = MutableStateFlow<String?>(null)

    val services: StateFlow<List<ServiceData>> = _services
    val promotions: StateFlow<List<PromotionData>> = _promotions
    val barbers: StateFlow<List<UserData>> = _barbers
    val info: StateFlow<BarberInfo?> = _info
    val currentUser: StateFlow<UserData?> = _currentUser
    val cartItems: StateFlow<List<Any>> = _cartItems
    val appointments: StateFlow<List<com.example.fadebarber.data.model.AppointmentClientData>> = _appointments
    val cartNotice: StateFlow<String?> = _cartNotice

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")

    // Referencias para listeners
    private var barberInfoListener: ValueEventListener? = null
    private var barberInfoReference: com.google.firebase.database.DatabaseReference? = null
    private var servicesListener: ValueEventListener? = null
    private var promotionsListener: ValueEventListener? = null
    private var barbersListener: ValueEventListener? = null
    private var appointmentsListener: ValueEventListener? = null

    init {
        viewModelScope.launch {
            // Carga inicial (opcional, los listeners harán la carga también)
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

    // ========== LISTENERS EN TIEMPO REAL ==========

    /**
     * Iniciar todos los listeners para HomePage
     */
    fun startRealtimeListeners() {
        startServicesListener()
        startPromotionsListener()
        startBarbersListener()
        startAppointmentsListener()
        Log.d("HomeViewModel", "Todos los listeners de HomePage iniciados")
    }

    /**
     * Detener todos los listeners
     */
    fun stopRealtimeListeners() {
        stopServicesListener()
        stopPromotionsListener()
        stopBarbersListener()
        stopAppointmentsListener()
        Log.d("HomeViewModel", "Todos los listeners de HomePage detenidos")
    }

    /**
     * Listener para servicios
     */
    private fun startServicesListener() {
        servicesListener = FirebaseRepository.listenToServices { services ->
            _services.value = services
        }
    }

    private fun stopServicesListener() {
        servicesListener?.let {
            FirebaseRepository.stopListeningToServices(it)
            servicesListener = null
        }
    }

    /**
     * Listener para promociones
     */
    private fun startPromotionsListener() {
        promotionsListener = FirebaseRepository.listenToPromotions { promotions ->
            _promotions.value = promotions
        }
    }

    private fun stopPromotionsListener() {
        promotionsListener?.let {
            FirebaseRepository.stopListeningToPromotions(it)
            promotionsListener = null
        }
    }

    /**
     * Listener para barberos
     */
    private fun startBarbersListener() {
        barbersListener = FirebaseRepository.listenToBarbers { barbers ->
            _barbers.value = barbers
        }
    }

    private fun stopBarbersListener() {
        barbersListener?.let {
            FirebaseRepository.stopListeningToBarbers(it)
            barbersListener = null
        }
    }

    /**
     * Listener para citas en tiempo real
     */
    private fun startAppointmentsListener() {
        appointmentsListener = FirebaseRepository.listenToAppointments { appts ->
            _appointments.value = appts
        }
    }

    private fun stopAppointmentsListener() {
        appointmentsListener?.let {
            FirebaseRepository.stopListeningToAppointments(it)
            appointmentsListener = null
        }
    }

    /**
     * Listener para información del barbero (horarios)
     */
    fun listenToBarberInfo() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("HomeViewModel", "Usuario no autenticado")
            return
        }

        val userId = firebaseUser.uid
        barberInfoReference = FirebaseDatabase.getInstance().getReference("Barber/$userId")

        barberInfoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val barberData = snapshot.getValue(BarberInfo::class.java)
                    barberData?.let {
                        _info.value = it
                        Log.d("HomeViewModel", "Información de barbero actualizada")
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error al actualizar información de barbero", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeViewModel", "Error en listener de barbero: ${error.message}")
            }
        }

        barberInfoListener?.let {
            barberInfoReference?.addValueEventListener(it)
            Log.d("HomeViewModel", "Listener de horarios iniciado")
        }
    }

    fun stopListeningToBarberInfo() {
        barberInfoListener?.let { listener ->
            barberInfoReference?.removeEventListener(listener)
            Log.d("HomeViewModel", "Listener de horarios detenido")
        }
        barberInfoListener = null
        barberInfoReference = null
    }

    // ========== CARRITO ==========

    fun addToCart(item: Any) {
        if (item is ServiceData) {
            val countSameService = _cartItems.value.count { it is ServiceData && it.id == item.id }
            if (countSameService >= 2) {
                _cartNotice.value = "Máximo 2 veces el mismo servicio"
                return
            }
        }
        if (_cartItems.value.size >= 4) {
            _cartNotice.value = "Máximo 4 elementos por cita. Deselecciona uno para agregar otro."
            return
        }
        _cartItems.value = _cartItems.value + item
    }

    fun removeFromCart(item: Any) {
        _cartItems.value = _cartItems.value - item
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartItemsCount(): Int = _cartItems.value.size

    fun clearCartNotice() {
        _cartNotice.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeListeners()
        stopListeningToBarberInfo()
        Log.d("HomeViewModel", "ViewModel limpiado, listeners detenidos")
    }
}
