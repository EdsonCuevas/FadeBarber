package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.repository.Repository
import com.example.fadebarber.data.model.AppointmentData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _appointments = MutableStateFlow<List<AppointmentData>>(emptyList())
    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    private  val _promotion = MutableStateFlow<List<PromotionData>>(emptyList())
    val appointments: StateFlow<List<AppointmentData>> = _appointments
    val services: StateFlow<List<ServiceData>> = _services
    val users: StateFlow<List<UserData>> = _users
    val promotion: StateFlow<List<PromotionData>> = _promotion

    init {
        viewModelScope.launch {
            try {
                val appointments = Repository.getAppointments()
                val serivices = Repository.getServices()
                val users = Repository.getUser()
                val promotions = Repository.getPromotion()
                _appointments.value = appointments
                _services.value = serivices
                _users.value = users
                Repository.getPromotion().collect { promociones ->
                    _promotion.value = promociones
                }

            } catch (e: Exception) {
            }
        }
    }

}
