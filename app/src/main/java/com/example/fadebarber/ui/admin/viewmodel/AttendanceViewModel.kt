package com.example.fadebarber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class AttendanceViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlows
    private val _barbers = MutableStateFlow<List<UserData>>(emptyList())
    val barbers: StateFlow<List<UserData>> = _barbers.asStateFlow()

    private val _readings = MutableStateFlow<List<ReadingData>>(emptyList())
    val readings: StateFlow<List<ReadingData>> = _readings.asStateFlow()

    private val _currentUser = MutableStateFlow<UserData>(UserData())
    val currentUser: StateFlow<UserData> = _currentUser.asStateFlow()

    // Listeners
    private var barbersListener: ValueEventListener? = null
    private var readingsListener: ValueEventListener? = null
    private var currentUserListener: ValueEventListener? = null

    // Referencias
    private val usersRef = database.getReference("User")
    private val readingsRef = database.getReference("Readings")

    /**
     * Inicia todos los listeners
     */
    fun startListeners() {
        listenToBarbers()
        listenToReadings()
        listenToCurrentUser()
    }

    /**
     * Detiene todos los listeners
     */
    fun stopListeners() {
        barbersListener?.let { usersRef.removeEventListener(it) }
        readingsListener?.let { readingsRef.removeEventListener(it) }
    }

    /**
     * Escucha los datos del usuario actual
     */
    private fun listenToCurrentUser() {
        val currentUserId = auth.currentUser?.uid ?: return

        currentUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val user = snapshot.getValue(UserData::class.java)
                    if (user != null) {
                        _currentUser.value = user
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        }
        usersRef.child(currentUserId).addValueEventListener(currentUserListener!!)
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
     * Verifica si un timestamp es de hoy
     */
    fun isToday(timestamp: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("America/Mexico_City")

            val date = format.parse(timestamp)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
            val today = calendar.time

            val calDate = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
            calDate.time = date ?: return false

            calDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    calDate.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
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
     * Obtiene las lecturas de hoy
     */
    fun getTodayReadings(): List<ReadingData> {
        return _readings.value.filter { isToday(it.timestamp) }
    }

    /**
     * Obtiene las lecturas de un día específico
     */
    fun getReadingsForDay(calendar: Calendar): List<ReadingData> {
        return _readings.value.filter { isSameDay(it.timestamp, calendar) }
    }

    /**
     * Obtiene las lecturas de un barbero específico hoy
     */
    fun getBarberReadingsToday(barberId: String): List<ReadingData> {
        return getTodayReadings().filter { it.userId == barberId }
    }

    /**
     * Obtiene las lecturas de un barbero en un día específico
     */
    fun getBarberReadingsForDay(barberId: String, calendar: Calendar): List<ReadingData> {
        return getReadingsForDay(calendar).filter { it.userId == barberId }
    }

    /**
     * Verifica si un barbero está dentro (última lectura es "entrada")
     */
    fun isBarberInside(barberId: String): Boolean {
        val todayReadings = getBarberReadingsToday(barberId)
        val lastReading = todayReadings.maxByOrNull { it.timestamp }
        return lastReading?.tipo == "entrada"
    }

    /**
     * Verifica si un barbero estaba dentro en un día específico
     */
    fun isBarberInsideOnDay(barberId: String, calendar: Calendar): Boolean {
        val dayReadings = getBarberReadingsForDay(barberId, calendar)
        val lastReading = dayReadings.maxByOrNull { it.timestamp }
        return lastReading?.tipo == "entrada"
    }

    /**
     * Cuenta cuántos barberos están dentro
     */
    fun getActiveBarbersCount(): Int {
        return _barbers.value.count { isBarberInside(it.id) }
    }

    /**
     * Cuenta cuántos barberos estaban dentro en un día específico
     */
    fun getActiveBarbersCountForDay(calendar: Calendar): Int {
        return _barbers.value.count { isBarberInsideOnDay(it.id, calendar) }
    }

    /**
     * Calcula las horas trabajadas de un barbero específico hoy
     */
    fun calculateBarberWorkHours(barberId: String): Double {
        val readings = getBarberReadingsToday(barberId).sortedBy { it.timestamp }
        return calculateWorkHoursFromReadings(readings)
    }

    /**
     * Calcula las horas trabajadas de un barbero en un día específico
     */
    fun calculateBarberWorkHoursForDay(barberId: String, calendar: Calendar): Double {
        val readings = getBarberReadingsForDay(barberId, calendar).sortedBy { it.timestamp }
        return calculateWorkHoursFromReadings(readings)
    }

    /**
     * Función auxiliar para calcular horas trabajadas desde una lista de lecturas
     */
    private fun calculateWorkHoursFromReadings(readings: List<ReadingData>): Double {
        var totalHours = 0.0
        var lastEntryTime: Date? = null
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("America/Mexico_City")

        readings.forEach { reading ->
            try {
                val currentTime = dateFormat.parse(reading.timestamp)

                when (reading.tipo) {
                    "entrada" -> {
                        lastEntryTime = currentTime
                    }
                    "salida" -> {
                        if (lastEntryTime != null && currentTime != null) {
                            val diffInMillis = currentTime.time - lastEntryTime!!.time
                            totalHours += diffInMillis / (1000.0 * 60 * 60)
                            lastEntryTime = null
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar error de parsing
            }
        }

        return totalHours
    }

    /**
     * Calcula las horas trabajadas totales de todos los barberos hoy
     */
    fun getTotalWorkHours(): Double {
        return _barbers.value.sumOf { calculateBarberWorkHours(it.id) }
    }

    /**
     * Calcula las horas trabajadas totales de todos los barberos en un día específico
     */
    fun getTotalWorkHoursForDay(calendar: Calendar): Double {
        return _barbers.value.sumOf { calculateBarberWorkHoursForDay(it.id, calendar) }
    }

    /**
     * Obtiene la última lectura de un barbero
     */
    fun getLastReading(barberId: String): ReadingData? {
        return getBarberReadingsToday(barberId).maxByOrNull { it.timestamp }
    }

    /**
     * Obtiene la última lectura de un barbero en un día específico
     */
    fun getLastReadingForDay(barberId: String, calendar: Calendar): ReadingData? {
        return getBarberReadingsForDay(barberId, calendar).maxByOrNull { it.timestamp }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeners()
    }
}