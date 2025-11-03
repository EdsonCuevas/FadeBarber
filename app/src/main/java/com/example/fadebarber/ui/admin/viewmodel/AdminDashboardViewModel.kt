package com.example.fadebarber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.admin.pages.ReadingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlows
    private val _barbers = MutableStateFlow<List<UserData>>(emptyList())
    val barbers: StateFlow<List<UserData>> = _barbers.asStateFlow()

    private val _appointments = MutableStateFlow<List<AppointmentClientData>>(emptyList())
    val appointments: StateFlow<List<AppointmentClientData>> = _appointments.asStateFlow()

    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    val services: StateFlow<List<ServiceData>> = _services.asStateFlow()

    private val _promotions = MutableStateFlow<List<PromotionData>>(emptyList())
    val promotions: StateFlow<List<PromotionData>> = _promotions.asStateFlow()

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users.asStateFlow()

    private val _onlineStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val onlineStatus: StateFlow<Map<String, Boolean>> = _onlineStatus.asStateFlow()

    private val _readings = MutableStateFlow<List<ReadingData>>(emptyList())
    val readings: StateFlow<List<ReadingData>> = _readings.asStateFlow()

    // Listeners
    private var barbersListener: ValueEventListener? = null
    private var appointmentsListener: ValueEventListener? = null
    private var servicesListener: ValueEventListener? = null
    private var promotionsListener: ValueEventListener? = null
    private var usersListener: ValueEventListener? = null
    private var onlineStatusListener: ValueEventListener? = null
    private var readingsListener: ValueEventListener? = null

    // Referencias
    private val usersRef = database.getReference("User")
    private val appointmentsRef = database.getReference("Appointment")
    private val servicesRef = database.getReference("Service")
    private val promotionsRef = database.getReference("Promotion")
    private val onlineStatusRef = database.getReference("onlineStatus")
    private val readingsRef = database.getReference("Readings")

    /**
     * Mapeo de estados de citas (según tu DB)
     * statusAppointment:
     * 1 = Pendiente
     * 2 = Confirmada
     * 3 = En progreso
     * 4 = Completada
     * 5 = Cancelada
     */
    private fun getAppointmentStatus(statusCode: Int?): String {
        return when (statusCode) {
            1 -> "Pendiente"
            2 -> "Confirmada"
            3 -> "En progreso"
            4 -> "Completada"
            5 -> "Cancelada"
            else -> "Desconocido"
        }
    }

    /**
     * Inicia todos los listeners
     */
    fun startListeners() {
        listenToBarbers()
        listenToAppointments()
        listenToServices()
        listenToPromotions()
        listenToUsers()
        listenToOnlineStatus()
        listenToReadings()
    }

    /**
     * Detiene todos los listeners
     */
    fun stopListeners() {
        barbersListener?.let { usersRef.removeEventListener(it) }
        appointmentsListener?.let { appointmentsRef.removeEventListener(it) }
        servicesListener?.let { servicesRef.removeEventListener(it) }
        promotionsListener?.let { promotionsRef.removeEventListener(it) }
        usersListener?.let { usersRef.removeEventListener(it) }
        onlineStatusListener?.let { onlineStatusRef.removeEventListener(it) }
        readingsListener?.let { readingsRef.removeEventListener(it) }
    }

    /**
     * Escucha los barberos (usuarios con categoryUser == 2)
     */
    private fun listenToBarbers() {
        barbersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val list = mutableListOf<UserData>()
                    snapshot.children.forEach { child ->
                        child.getValue(UserData::class.java)?.let { user ->
                            // categoryUser: 1=Admin, 2=Barbero, 3=Cliente
                            if (user.categoryUser == 2) {
                                list.add(user)
                            }
                        }
                    }
                    _barbers.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        usersRef.addValueEventListener(barbersListener!!)
    }

    /**
     * Escucha todas las citas
     */
    private fun listenToAppointments() {
        appointmentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val list = mutableListOf<AppointmentClientData>()
                    snapshot.children.forEach { child ->
                        child.getValue(AppointmentClientData::class.java)?.let { appointment ->
                            list.add(appointment)
                        }
                    }
                    _appointments.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        appointmentsRef.addValueEventListener(appointmentsListener!!)
    }

    /**
     * Escucha los servicios
     */
    private fun listenToServices() {
        servicesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val list = mutableListOf<ServiceData>()
                    snapshot.children.forEach { child ->
                        child.getValue(ServiceData::class.java)?.let { service ->
                            list.add(service)
                        }
                    }
                    _services.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        servicesRef.addValueEventListener(servicesListener!!)
    }

    /**
     * Escucha las promociones
     */
    private fun listenToPromotions() {
        promotionsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val list = mutableListOf<PromotionData>()
                    snapshot.children.forEach { child ->
                        child.getValue(PromotionData::class.java)?.let { promotion ->
                            list.add(promotion)
                        }
                    }
                    _promotions.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        promotionsRef.addValueEventListener(promotionsListener!!)
    }

    /**
     * Escucha todos los usuarios
     */
    private fun listenToUsers() {
        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val list = mutableListOf<UserData>()
                    snapshot.children.forEach { child ->
                        child.getValue(UserData::class.java)?.let { user ->
                            list.add(user)
                        }
                    }
                    _users.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        usersRef.addValueEventListener(usersListener!!)
    }

    /**
     * Escucha el estado online de los usuarios
     */
    private fun listenToOnlineStatus() {
        onlineStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val map = mutableMapOf<String, Boolean>()
                    snapshot.children.forEach { child ->
                        val userId = child.key ?: return@forEach
                        val isOnline = child.getValue(Boolean::class.java) ?: false
                        map[userId] = isOnline
                    }
                    _onlineStatus.value = map
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        onlineStatusRef.addValueEventListener(onlineStatusListener!!)
    }

    /**
     * Escucha todas las lecturas de asistencia
     */
    private fun listenToReadings() {
        readingsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val list = mutableListOf<ReadingData>()
                    snapshot.children.forEach { child ->
                        child.getValue(ReadingData::class.java)?.let { reading ->
                            list.add(reading)
                        }
                    }
                    _readings.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        readingsRef.addValueEventListener(readingsListener!!)
    }

    /**
     * Verifica si un usuario está online
     */
    fun isUserOnline(userId: String): Boolean {
        return _onlineStatus.value[userId] ?: false
    }

    /**
     * Verifica si una fecha es hoy
     * Formato esperado: "yyyy-MM-dd" (ej: "2025-10-28")
     */
    fun isToday(dateString: String?): Boolean {
        if (dateString.isNullOrEmpty()) return false

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val appointmentDate = dateFormat.parse(dateString)
            val today = Calendar.getInstance()
            val appointmentCal = Calendar.getInstance()
            appointmentCal.time = appointmentDate ?: return false

            today.get(Calendar.YEAR) == appointmentCal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == appointmentCal.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si un timestamp pertenece al mismo día que el Calendar proporcionado
     */
    fun isSameDay(timestamp: String, calendar: Calendar): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("America/Mexico_City")

            val date = format.parse(timestamp) ?: return false

            val calDate = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
            calDate.time = date

            calDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    calDate.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene las citas de hoy
     */
    fun getTodayAppointments(): List<AppointmentClientData> {
        return _appointments.value.filter { isToday(it.dateAppointment) }
    }

    /**
     * Obtiene citas completadas hoy (statusAppointment == 4)
     */
    fun getCompletedTodayCount(): Int {
        return getTodayAppointments().count { it.statusAppointment == 4 }
    }

    /**
     * Obtiene citas en progreso hoy (statusAppointment == 3)
     */
    fun getInProgressTodayCount(): Int {
        return getTodayAppointments().count { it.statusAppointment == 3 }
    }

    /**
     * Obtiene citas pendientes hoy (statusAppointment == 1)
     */
    fun getPendingTodayCount(): Int {
        return getTodayAppointments().count { it.statusAppointment == 1 }
    }

    /**
     * Obtiene barberos activos (con citas hoy)
     */
    fun getActiveBarbersWithCount(): List<Pair<UserData, Int>> {
        val todayAppointments = getTodayAppointments()
        return _barbers.value.mapNotNull { barber ->
            val count = todayAppointments.count { it.idEmployee == barber.id }
            if (count > 0) barber to count else null
        }.sortedByDescending { it.second }
    }

    /**
     * Obtiene las próximas citas (no completadas ni canceladas, ordenadas por hora)
     */
    fun getUpcomingAppointments(limit: Int = 5): List<AppointmentClientData> {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        return getTodayAppointments()
            .filter {
                // Excluir completadas (4) y canceladas (5)
                it.statusAppointment != 4 &&
                        it.statusAppointment != 5 &&
                        !it.timeAppointment.isNullOrEmpty()
            }
            .sortedBy { appointment ->
                // Convertir hora a minutos para comparación
                val timeParts = appointment.timeAppointment?.split(":") ?: return@sortedBy Int.MAX_VALUE
                if (timeParts.size != 2) return@sortedBy Int.MAX_VALUE

                try {
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()
                    hour * 60 + minute
                } catch (e: Exception) {
                    Int.MAX_VALUE
                }
            }
            .take(limit)
    }

    /**
     * Obtiene el número de barberos online
     */
    fun getOnlineBarbersCount(): Int {
        return _barbers.value.count { isUserOnline(it.id) }
    }

    /**
     * Obtiene citas de un barbero específico hoy
     */
    fun getBarberAppointmentsToday(barberId: String): List<AppointmentClientData> {
        return getTodayAppointments().filter { it.idEmployee == barberId }
    }

    /**
     * Obtiene el estado de una cita como texto
     */
    fun getAppointmentStatusText(statusCode: Int?): String {
        return getAppointmentStatus(statusCode)
    }

    /**
     * Verifica si un barbero está activo (última lectura del día es "entrada")
     */
    fun isBarberActive(barberId: String, calendar: Calendar): Boolean {
        val barberReadings = _readings.value
            .filter { it.userId == barberId }
            .filter { isSameDay(it.timestamp, calendar) }
            .sortedByDescending { it.timestamp }

        return barberReadings.firstOrNull()?.tipo == "entrada"
    }

    override fun onCleared() {
        super.onCleared()
        stopListeners()
    }
}