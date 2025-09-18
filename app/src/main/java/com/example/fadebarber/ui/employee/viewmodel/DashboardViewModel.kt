package com.example.fadebarber.ui.employee.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fadebarber.data.model.Appointment
import com.example.fadebarber.data.model.Date

class DashboardViewModel : ViewModel() {
    private val _date = MutableLiveData(
        Date(
            service = "Corte de pelo + Barba",
            client = "Alondra"
        )
    )
    val date: LiveData<Date> = _date


    private val _appointments = MutableLiveData(
        listOf(
            Appointment(id = 1,service = "Corte de pelo", client = "María Felix", hour = "7:00pm", date = "12 Sep 2025"),
            Appointment(id = 2,service = "Barba", client = "Juan Pérez", hour = "5:00pm", date = "12 Sep 2025"),
            Appointment(id = 3,service = "Tinte", client = "Antonio Guzman", hour = "3:00pm", date = "12 Sep 2025"),
            Appointment(id = 4,service = "Tinte", client = "Juan Gomez", hour = "3:00pm", date = "12 Sep 2025"),
            Appointment(id = 5,service = "Tinte", client = "Mei Feng", hour = "3:00pm", date = "12 Sep 2025"),
            Appointment(id = 6,service = "Tinte", client = "Ulises Flores", hour = "3:00pm", date = "12 Sep 2025"),
            Appointment(id = 7,service = "Tinte", client = "Joshua Gonzales", hour = "3:00pm", date = "12 Sep 2025"),
        )
    )
    val appointments: LiveData<List<Appointment>> = _appointments



}