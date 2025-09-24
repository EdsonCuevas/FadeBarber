package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    private val _promotions = MutableStateFlow<List<PromotionData>>(emptyList())
    private val _info = MutableStateFlow<BarberInfo?>(null)
    private val _currentUser = MutableStateFlow<UserData?>(null)

    val services: StateFlow<List<ServiceData>> = _services
    val promotions: StateFlow<List<PromotionData>> = _promotions
    val info: StateFlow<BarberInfo?> = _info
    val currentUser: StateFlow<UserData?> = _currentUser

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")

    init {
        viewModelScope.launch {
            _services.value = FirebaseRepository.getServices()
            _promotions.value = FirebaseRepository.getPromotions()
            _info.value = FirebaseRepository.getBarberInfo()
            loadCurrentUser()
        }
    }

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
}
