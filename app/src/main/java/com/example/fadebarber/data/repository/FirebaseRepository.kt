package com.example.fadebarber.data.repository

import android.util.Log
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.AppointmentService
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.model.ReadingData
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
    private val readingsRef = database.getReference("Readings")

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

    /**
     * Escuchar cambios en citas en tiempo real
     */
    fun listenToAppointments(onUpdate: (List<AppointmentClientData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val list = mutableListOf<AppointmentClientData>()
                    for (child in snapshot.children) {
                        val appt = child.getValue(AppointmentClientData::class.java)
                        if (appt != null) list.add(appt)
                    }
                    onUpdate(list)
                    Log.d("FirebaseRepository", "Citas actualizadas: ${list.size}")
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error al actualizar citas", e)
                    onUpdate(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error en listener de citas: ${error.message}")
                onUpdate(emptyList())
            }
        }
        appointmentRef.addValueEventListener(listener)
        return listener
    }

    fun stopListeningToAppointments(listener: ValueEventListener) {
        appointmentRef.removeEventListener(listener)
        Log.d("FirebaseRepository", "Listener de citas detenido")
    }

    /**
     * Escuchar cambios en lecturas de presencia (Readings) en tiempo real
     */
    fun listenToReadings(onUpdate: (List<ReadingData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val list = mutableListOf<ReadingData>()
                    for (child in snapshot.children) {
                        val reading = child.getValue(ReadingData::class.java)
                        if (reading != null) list.add(reading)
                    }
                    onUpdate(list)
                    Log.d("FirebaseRepository", "Lecturas actualizadas: ${list.size}")
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error al actualizar lecturas", e)
                    onUpdate(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error en listener de lecturas: ${error.message}")
                onUpdate(emptyList())
            }
        }
        readingsRef.addValueEventListener(listener)
        return listener
    }

    fun stopListeningToReadings(listener: ValueEventListener) {
        readingsRef.removeEventListener(listener)
        Log.d("FirebaseRepository", "Listener de lecturas detenido")
    }

    /**
     * Escuchar lista completa de usuarios
     */
    fun listenToUsers(onUpdate: (List<UserData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val users = mutableListOf<UserData>()
                    for (child in snapshot.children) {
                        val user = child.getValue(UserData::class.java)
                        if (user != null) users.add(user)
                    }
                    onUpdate(users)
                    Log.d("FirebaseRepository", "Usuarios actualizados: ${users.size}")
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error al actualizar usuarios", e)
                    onUpdate(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error en listener de usuarios: ${error.message}")
                onUpdate(emptyList())
            }
        }
        userRef.addValueEventListener(listener)
        return listener
    }

    fun stopListeningToUsers(listener: ValueEventListener) {
        userRef.removeEventListener(listener)
        Log.d("FirebaseRepository", "Listener de usuarios detenido")
    }
}