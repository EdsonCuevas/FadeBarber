package com.example.fadebarber.data.repository

import com.example.fadebarber.data.model.ServiceData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val database = FirebaseDatabase.getInstance("https://barbershop-dd871-default-rtdb.firebaseio.com/")
    private val serviceRef = database.getReference("Service")

    suspend fun getServices(): List<ServiceData> {
        return try {
            val snapshot = serviceRef.get().await()
            val services = mutableListOf<ServiceData>()
            for (child in snapshot.children) {
                val service = child.getValue(ServiceData::class.java)
                service?.let { services.add(it) }
            }
            services
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
