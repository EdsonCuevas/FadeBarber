package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")
    private val _currentUser = MutableStateFlow<UserData?>(null)

    val currentUser: StateFlow<UserData?> = _currentUser


    private fun loadCurrentUser() {
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
    val appointments: StateFlow<List<AppointmentClientData>> =
        Repository.getAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceData>> =
        Repository.getServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserData>> =
        Repository.getUsers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promotions: StateFlow<List<PromotionData>> =
        Repository.getPromotions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            loadCurrentUser()
        }
    }
}
