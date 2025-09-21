package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.repository.FirebaseRepository
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.PromotionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    private val _promotions = MutableStateFlow<List<PromotionData>>(emptyList())
    private val _info = MutableStateFlow<BarberInfo?>(null)

    val services: StateFlow<List<ServiceData>> = _services
    val promotions: StateFlow<List<PromotionData>> = _promotions
    val info: StateFlow<BarberInfo?> = _info


    init {
        viewModelScope.launch {
            _services.value = FirebaseRepository.getServices()
            _promotions.value = FirebaseRepository.getPromotions()
            _info.value = FirebaseRepository.getBarberInfo()
        }
    }
}
