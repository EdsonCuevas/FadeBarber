package com.example.fadebarber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ReadingData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.FirebaseRepository
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class AdminEmployeeListViewModel : ViewModel() {

    private val _barbers = MutableStateFlow<List<UserData>>(emptyList())
    val barbers: StateFlow<List<UserData>> = _barbers

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    private val _appointments = MutableStateFlow<List<AppointmentClientData>>(emptyList())
    val appointments: StateFlow<List<AppointmentClientData>> = _appointments

    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    val services: StateFlow<List<ServiceData>> = _services

    private val _promotions = MutableStateFlow<List<PromotionData>>(emptyList())
    val promotions: StateFlow<List<PromotionData>> = _promotions

    private val _readings = MutableStateFlow<List<ReadingData>>(emptyList())
    val readings: StateFlow<List<ReadingData>> = _readings

    // Listeners
    private var barbersListener: ValueEventListener? = null
    private var usersListener: ValueEventListener? = null
    private var appointmentsListener: ValueEventListener? = null
    private var servicesListener: ValueEventListener? = null
    private var promotionsListener: ValueEventListener? = null
    private var readingsListener: ValueEventListener? = null

    fun startListeners() {
        stopListeners() // asegurar estado limpio
        barbersListener = FirebaseRepository.listenToBarbers { _barbers.value = it }
        usersListener = FirebaseRepository.listenToUsers { _users.value = it }
        appointmentsListener = FirebaseRepository.listenToAppointments { _appointments.value = it }
        servicesListener = FirebaseRepository.listenToServices { _services.value = it }
        promotionsListener = FirebaseRepository.listenToPromotions { _promotions.value = it }
        readingsListener = FirebaseRepository.listenToReadings { _readings.value = it }
    }

    fun stopListeners() {
        barbersListener?.let { FirebaseRepository.stopListeningToBarbers(it) }
        usersListener?.let { FirebaseRepository.stopListeningToUsers(it) }
        appointmentsListener?.let { FirebaseRepository.stopListeningToAppointments(it) }
        servicesListener?.let { FirebaseRepository.stopListeningToServices(it) }
        promotionsListener?.let { FirebaseRepository.stopListeningToPromotions(it) }
        readingsListener?.let { FirebaseRepository.stopListeningToReadings(it) }
        barbersListener = null
        usersListener = null
        appointmentsListener = null
        servicesListener = null
        promotionsListener = null
        readingsListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListeners()
    }

    private fun parseInstant(s: String?): Long {
        return try {
            Instant.parse(s).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    fun isUserOnline(userId: String?): Boolean {
        if (userId.isNullOrBlank()) return false
        val latest = _readings.value
            .filter { it.userId == userId }
            .maxByOrNull { parseInstant(it.timestamp) }
        return latest?.tipo == "entrada"
    }

    fun todayAppointments(): List<AppointmentClientData> {
        val today = LocalDate.now().toString()
        return _appointments.value.filter {
            val dateStr = it.dateAppointment?.take(10)
            dateStr == today
        }
    }

    fun appointmentsForEmployeeToday(userId: String?): List<AppointmentClientData> {
        if (userId.isNullOrBlank()) return emptyList()
        val today = LocalDate.now().toString()
        return _appointments.value.filter {
            val dateStr = it.dateAppointment?.take(10)
            dateStr == today && it.idEmployee == userId
        }
    }

    fun todayTotalsForEmployee(userId: String?): Pair<Int, Int> {
        val list = appointmentsForEmployeeToday(userId)
        val total = list.size
        val completedOrCanceled = list.count { (it.statusAppointment ?: 0) in listOf(3, 4) }
        return Pair(completedOrCanceled, total)
    }
}