package com.example.fadebarber.data.model

data class UserData(
    val id: String = "",
    val nameUser: String = "",
    val correoUser: String = "",
    val categoryUser: Int = 0,
    val passwordUser: String = "",
    val phoneNumberUser: String = "",
    val photoURL: String = "",
    val schedule: Map<String, ScheduleDay> = emptyMap(),
    val statusUser: Int = 0,
    val activeUser: Boolean = true
)

data class ScheduleDay(
    val available: Boolean = false,
    val start: String? = null,
    val end: String? = null
)