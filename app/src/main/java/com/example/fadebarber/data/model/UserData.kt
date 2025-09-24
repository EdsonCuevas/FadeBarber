package com.example.fadebarber.data.model

data class UserData(
    val id: String = "",
    val nameUser: String = "",
    val correoUser: String = "",
    val categoryUser: Int = 0,
    val passwordUser: String = "",
    val phoneNumberUser: String = "",
    val statusUser: Int = 0,
    val activeUser: Boolean = true
)