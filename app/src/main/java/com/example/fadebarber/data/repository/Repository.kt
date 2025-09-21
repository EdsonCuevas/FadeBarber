package com.example.fadebarber.data.repository

import com.example.fadebarber.data.model.AppointmentData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object Repository {
    private const val TAG = "AppoinmentRepository"

    private val database = FirebaseDatabase.getInstance("https://barbershop-dd871-default-rtdb.firebaseio.com/")
    private val appointmentRef = database.getReference("Appointment")
    private val serviceRef = database.getReference("Service")
    private val userRef = database.getReference("User")
    private  val promotionRef = database.getReference("Promotion")

    suspend fun getAppointments(): List<AppointmentData> {
        return try {
            val snapshot = appointmentRef.get().await()
            val appointments = mutableListOf<AppointmentData>()
            for (child in snapshot.children) {
                val appointment = child.getValue(AppointmentData::class.java)
                appointment?.let { appointments.add(it) }
            }
            appointments
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()

        }
    }

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

    suspend fun getUser(): List<UserData> {
        return try {
            val snapshot = userRef.get().await()
            val users = mutableListOf<UserData>()
            for (child in snapshot.children) {
                val user = child.getValue(UserData::class.java)
                user?.let { users.add(it) }
            }
            users
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    fun getPromotion(): Flow<List<PromotionData>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val promotions = mutableListOf<PromotionData>()
                for (child in snapshot.children) {
                    val promotion = child.getValue(PromotionData::class.java)
                    promotion?.let { promotions.add(it) }
                }
                trySend(promotions) // emite la lista al Flow
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        promotionRef.addValueEventListener(listener)

        awaitClose { promotionRef.removeEventListener(listener) }
    }

}
