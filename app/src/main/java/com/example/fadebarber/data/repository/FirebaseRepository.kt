package com.example.fadebarber.data.repository

import com.example.fadebarber.data.model.ServiceData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object FirebaseRepository {
    private const val SERVICE_URL =
        "https://barbershop-dd871-default-rtdb.firebaseio.com/Service.json?auth=c7dq1YGCoYAUyAXJtLJStuY3pepWk7z2k9cW2p9p"

    suspend fun getServices(): List<ServiceData> = withContext(Dispatchers.IO) {
        val url = URL(SERVICE_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        val listType = object : TypeToken<List<ServiceData>>() {}.type
        Gson().fromJson(response, listType)
    }
}

