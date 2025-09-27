package com.example.fadebarber.data.repository

import com.example.fadebarber.data.model.AppointmentClientData
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

object Repository {
    private const val TAG = "AppoinmentRepository"

    private val database = FirebaseDatabase.getInstance("https://barbershop-dd871-default-rtdb.firebaseio.com/")
    private val appointmentRef = database.getReference("Appointment")
    private val serviceRef = database.getReference("Service")
    private val userRef = database.getReference("User")
    private val promotionRef = database.getReference("Promotion")

    fun getAppointments(): Flow<List<AppointmentClientData>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointments = snapshot.children.mapNotNull { it.getValue(AppointmentClientData::class.java) }
                trySend(appointments)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        appointmentRef.addValueEventListener(listener)
        awaitClose { appointmentRef.removeEventListener(listener) }
    }

    fun getServices(): Flow<List<ServiceData>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val services = snapshot.children.mapNotNull { it.getValue(ServiceData::class.java) }
                trySend(services)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        serviceRef.addValueEventListener(listener)
        awaitClose { serviceRef.removeEventListener(listener) }
    }

    fun getUsers(): Flow<List<UserData>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(UserData::class.java) }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userRef.addValueEventListener(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }

    fun getPromotions(): Flow<List<PromotionData>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val promotions = snapshot.children.mapNotNull { it.getValue(PromotionData::class.java) }
                trySend(promotions)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        promotionRef.addValueEventListener(listener)
        awaitClose { promotionRef.removeEventListener(listener) }
    }
}
