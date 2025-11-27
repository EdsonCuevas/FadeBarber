package com.example.fadebarber.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")
    private val _currentUser = MutableStateFlow<UserData?>(null)
    private var currentUserRef: DatabaseReference? = null
    private var userListener: ValueEventListener? = null

    val currentUser: StateFlow<UserData?> = _currentUser

    fun loadCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            database.child(user.uid)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val userData = dataSnapshot.getValue(UserData::class.java)
                        userData?.let {
                            _currentUser.value = it
                            android.util.Log.d("DashboardViewModel", "User data loaded: ${it.nameUser}")
                        }
                    } else {
                        android.util.Log.w("DashboardViewModel", "No user data found")
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("DashboardViewModel", "Error loading user data", exception)
                }
        }
    }

    fun startListeningCurrentUser() {
        val current = FirebaseAuth.getInstance().currentUser ?: return
        currentUserRef = database.child(current.uid)
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                _currentUser.value = userData
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        currentUserRef?.addValueEventListener(userListener!!)
    }

    fun stopListeningCurrentUser() {
        userListener?.let { listener ->
            currentUserRef?.removeEventListener(listener)
        }
        userListener = null
        currentUserRef = null
    }

    val appointments: StateFlow<List<AppointmentClientData>> =
        Repository.getAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceData>> =
        Repository.getServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserData>> =
        Repository.getUsers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promotions: StateFlow<List<PromotionData>> =
        Repository.getPromotions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            loadCurrentUser()
            startListeningCurrentUser()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningCurrentUser()
    }
}
