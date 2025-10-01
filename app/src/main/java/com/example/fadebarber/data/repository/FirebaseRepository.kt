package com.example.fadebarber.data.repository

import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.AppointmentPromotion
import com.example.fadebarber.data.model.AppointmentService
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.model.PromotionData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

object FirebaseRepository {

    private val database =
        FirebaseDatabase.getInstance("https://barbershop-dd871-default-rtdb.firebaseio.com/")

    private val serviceRef = database.getReference("Service")
    private val promotionRef = database.getReference("Promotion")
    private val infoRef = database.getReference("Information")
    private val userRef = database.getReference("User")

    suspend fun getServices(): List<ServiceData> {
        return try {
            val snapshot = serviceRef.get().await()
            val services = mutableListOf<ServiceData>()
            for (child in snapshot.children) {
                val service = child.getValue(ServiceData::class.java)
                if (service?.statusService == 1) {
                    services.add(service)
                }
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
                if (promotion?.statusPromotion == 1) {
                    promotions.add(promotion)
                }
            }
            promotions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getBarberInfo(): BarberInfo? {
        return try {
            val snapshot = infoRef.get().await()
            snapshot.getValue(BarberInfo::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBarbers(): List<UserData> {
        return try {
            val snapshot = userRef.get().await()
            val users = mutableListOf<UserData>()
            for (child in snapshot.children) {
                val user = child.getValue(UserData::class.java)
                if (user?.categoryUser == 2 && user.statusUser == 1) {
                    users.add(user)
                }
            }
            users
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveAppointment(appointmentService: AppointmentClientData): Boolean {
        return try {
            val appointmentRef = database.getReference("Appointment").push()
            val appointmentId = appointmentRef.key ?: return false
            val appointmentWithId = appointmentService.copy(id = appointmentId)
            appointmentRef.setValue(appointmentWithId).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveAppointment(appointmentService: AppointmentService): Boolean {
        return try {
            val appointmentRef = database.getReference("Appointment").push()
            val appointmentId = appointmentRef.key ?: return false
            val appointmentWithId = appointmentService.copy(id = appointmentId)
            appointmentRef.setValue(appointmentWithId).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
