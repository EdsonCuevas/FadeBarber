package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.repository.FirebaseRepository
import com.example.fadebarber.data.model.ServiceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _services = MutableStateFlow<List<ServiceData>>(emptyList())
    val services: StateFlow<List<ServiceData>> = _services

    init {
        viewModelScope.launch {
            _services.value = FirebaseRepository.getServices()
        }
    }
}
