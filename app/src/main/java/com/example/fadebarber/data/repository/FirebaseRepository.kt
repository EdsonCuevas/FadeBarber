package com.example.fadebarber.data.repository

import android.util.Log
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.AppointmentService
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val database =
        FirebaseDatabase.getInstance("https://barbershop-dd871-default-rtdb.firebaseio.com/")

    private val appointmentRef = database.getReference("Appointment")
    private val serviceRef = database.getReference("Service")
    private val promotionRef = database.getReference("Promotion")
    private val infoRef = database.getReference("Information")
    private val userRef = database.getReference("User")

    // ========== FUNCIONES SUSPEND (llamadas únicas) ==========

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

    suspend fun getAppointmentsByBarberAndDate(barberId: String, date: String): List<AppointmentClientData> {
        return try {
            val snapshot = appointmentRef.get().await()
            val list = mutableListOf<AppointmentClientData>()
            for (child in snapshot.children) {
                val appt = child.getValue(AppointmentClientData::class.java)
                if (appt != null && appt.idEmployee == barberId && appt.dateAppointment == date) {
                    list.add(appt)
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveAppointment(appointment: AppointmentClientData): String? {
        return try {
            val ref = appointmentRef.push()
            val id = ref.key ?: return null
            val withId = appointment.copy(id = id)
            ref.setValue(withId).await()
            id
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

    // ========== FUNCIONES DE LISTENERS EN TIEMPO REAL ==========

    /**
     * Escuchar cambios en servicios en tiempo real
     */
    fun listenToServices(onServicesUpdate: (List<ServiceData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val services = mutableListOf<ServiceData>()
                    for (child in snapshot.children) {
                        val service = child.getValue(ServiceData::class.java)
                        if (service?.statusService == 1) {
                            services.add(service)
                        }
                    }
                    onServicesUpdate(services)
                    Log.d("FirebaseRepository", "Servicios actualizados: ${services.size}")
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error al actualizar servicios", e)
                    onServicesUpdate(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error en listener de servicios: ${error.message}")
                onServicesUpdate(emptyList())
            }
        }
        serviceRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Escuchar cambios en promociones en tiempo real
     */
    fun listenToPromotions(onPromotionsUpdate: (List<PromotionData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val promotions = mutableListOf<PromotionData>()
                    for (child in snapshot.children) {
                        val promotion = child.getValue(PromotionData::class.java)
                        if (promotion?.statusPromotion == 1) {
                            promotions.add(promotion)
                        }
                    }
                    onPromotionsUpdate(promotions)
                    Log.d("FirebaseRepository", "Promociones actualizadas: ${promotions.size}")
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error al actualizar promociones", e)
                    onPromotionsUpdate(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error en listener de promociones: ${error.message}")
                onPromotionsUpdate(emptyList())
            }
        }
        promotionRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Escuchar cambios en barberos en tiempo real
     */
    fun listenToBarbers(onBarbersUpdate: (List<UserData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val users = mutableListOf<UserData>()
                    for (child in snapshot.children) {
                        val user = child.getValue(UserData::class.java)
                        if (user?.categoryUser == 2 && user.statusUser == 1) {
                            users.add(user)
                        }
                    }
                    onBarbersUpdate(users)
                    Log.d("FirebaseRepository", "Barberos actualizados: ${users.size}")
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error al actualizar barberos", e)
                    onBarbersUpdate(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error en listener de barberos: ${error.message}")
                onBarbersUpdate(emptyList())
            }
        }
        userRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Detener listener de servicios
     */
    fun stopListeningToServices(listener: ValueEventListener) {
        serviceRef.removeEventListener(listener)
        Log.d("FirebaseRepository", "Listener de servicios detenido")
    }

    /**
     * Detener listener de promociones
     */
    fun stopListeningToPromotions(listener: ValueEventListener) {
        promotionRef.removeEventListener(listener)
        Log.d("FirebaseRepository", "Listener de promociones detenido")
    }

    /**
     * Detener listener de barberos
     */
    fun stopListeningToBarbers(listener: ValueEventListener) {
        userRef.removeEventListener(listener)
        Log.d("FirebaseRepository", "Listener de barberos detenido")
    }
}