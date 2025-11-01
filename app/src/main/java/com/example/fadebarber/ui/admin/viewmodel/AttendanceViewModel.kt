package com.example.fadebarber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.admin.pages.ReadingData
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    // StateFlows
    private val _barbers = MutableStateFlow<List<UserData>>(emptyList())
    val barbers: StateFlow<List<UserData>> = _barbers.asStateFlow()

    private val _readings = MutableStateFlow<List<ReadingData>>(emptyList())
    val readings: StateFlow<List<ReadingData>> = _readings.asStateFlow()

    // Listeners
    private var barbersListener: ValueEventListener? = null
    private var readingsListener: ValueEventListener? = null

    // Referencias
    private val usersRef = database.getReference("User")
    private val readingsRef = database.getReference("Readings")

    /**
     * Inicia todos los listeners
     */
    fun startListeners() {
        listenToBarbers()
        listenToReadings()
    }

    /**
     * Detiene todos los listeners
     */
    fun stopListeners() {
        barbersListener?.let { usersRef.removeEventListener(it) }
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
     * Obtiene las lecturas de hoy
     */
    fun getTodayReadings(): List<ReadingData> {
        return _readings.value.filter { isToday(it.timestamp) }
    }

    /**
     * Obtiene las lecturas de un barbero específico hoy
     */
    fun getBarberReadingsToday(barberId: String): List<ReadingData> {
        return getTodayReadings().filter { it.userId == barberId }
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
     * Cuenta cuántos barberos están dentro
     */
    fun getActiveBarbersCount(): Int {
        return _barbers.value.count { isBarberInside(it.id) }
    }

    /**
     * Calcula las horas trabajadas de un barbero específico hoy
     */
    fun calculateBarberWorkHours(barberId: String): Double {
        val readings = getBarberReadingsToday(barberId).sortedBy { it.timestamp }
        var totalHours = 0.0
        var lastEntryTime: Date? = null
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

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
     * Obtiene la última lectura de un barbero
     */
    fun getLastReading(barberId: String): ReadingData? {
        return getBarberReadingsToday(barberId).maxByOrNull { it.timestamp }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeners()
    }
}