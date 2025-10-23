package com.example.fadebarber.data.model

/**
 * Modelo para lecturas de presencia en tiempo real.
 * Ejemplo de nodo en DB (Readings):
 * {
 *   timestamp: "2025-10-17T20:13:40.332Z",
 *   tipo: "entrada" | "salida",
 *   uid: "A98329DF",
 *   userId: "IPcRnW4oI0VRTDFWrvgM1XdNJo53"
 * }
 */
data class ReadingData(
    val timestamp: String? = null,
    val tipo: String? = null,
    val uid: String? = null,
    val userId: String? = null
)