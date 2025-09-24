package com.example.fadebarber.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object UserPreferences {
    private val Context.dataStore by preferencesDataStore("user_prefs")
    private val ROLE_KEY = intPreferencesKey("user_role")

    suspend fun saveUserRole(context: Context, role: Int) {
        context.dataStore.edit { prefs ->
            prefs[ROLE_KEY] = role
        }
    }

    fun getUserRole(context: Context): Flow<Int?> =
        context.dataStore.data.map { prefs -> prefs[ROLE_KEY] }
}