package com.example.fadebarber.data.repository

import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.PromotionData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val database =
        FirebaseDatabase.getInstance("https://barbershop-dd871-default-rtdb.firebaseio.com/")

    private val serviceRef = database.getReference("Service")
    private val promotionRef = database.getReference("Promotion")

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

    suspend fun getPromotions(): List<PromotionData> {
        return try {
            val snapshot = promotionRef.get().await()
            val promotions = mutableListOf<PromotionData>()
            for (child in snapshot.children) {
                val promotion = child.getValue(PromotionData::class.java)
                promotion?.let { promotions.add(it) }
            }
            promotions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
