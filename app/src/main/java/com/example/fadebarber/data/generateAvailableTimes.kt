package com.example.fadebarber.data

import java.time.format.DateTimeFormatter
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.UserData
import java.time.LocalDate
import java.time.LocalTime

import kotlin.collections.mapNotNull

fun generateAvailableTimes(
    barber: UserData,
    selectedDate: LocalDate,
    appointments: List<AppointmentClientData>
): List<LocalTime> {
    val dayOfWeek = selectedDate.dayOfWeek.name.lowercase() // monday, tuesday...
    val schedule = barber.schedule[dayOfWeek] ?: return emptyList()
    if (!schedule.available) return emptyList()

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val start = LocalTime.parse(schedule.start, formatter)
    val end = LocalTime.parse(schedule.end, formatter)

    // citas ya ocupadas en ese día
    val occupied = appointments.mapNotNull { it.timeAppointment }
        .map { LocalTime.parse(it, formatter) }

    val times = mutableListOf<LocalTime>()
    var current = start
    while (current <= end) {
        if (current !in occupied) { // no está ocupado
            times.add(current)
        }
        current = current.plusMinutes(30) // saltos de 30min, lo puedes cambiar
    }
    return times
}
