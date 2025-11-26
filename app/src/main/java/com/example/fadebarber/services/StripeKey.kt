package com.example.fadebarber.services

import android.content.Context
import android.util.Log
import org.json.JSONObject

object SecretConfig {

    private const val TAG = "SecretConfig"
    private var secrets: JSONObject? = null

    fun init(context: Context) {
        Log.d(TAG, "========== INICIANDO SECRETCONFIG ==========")

        if (secrets != null) {
            Log.d(TAG, "Ya inicializado previamente")
            return
        }

        try {
            // Listar archivos en assets
            Log.d(TAG, "Listando archivos en assets:")
            val assetFiles = context.assets.list("") ?: emptyArray()
            if (assetFiles.isEmpty()) {
                Log.e(TAG, "❌ La carpeta assets está VACÍA")
            } else {
                assetFiles.forEach { file ->
                    Log.d(TAG, "  - Archivo encontrado: $file")
                }
            }

            // Intentar abrir el archivo
            Log.d(TAG, "Intentando abrir: secret_config.json")
            val inputStream = context.assets.open("secret_config.json")

            // Leer contenido
            val jsonText = inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Contenido leído (${jsonText.length} caracteres)")
            Log.d(TAG, "Contenido: $jsonText")

            // Parsear JSON
            secrets = JSONObject(jsonText)
            Log.d(TAG, "✅ JSON parseado exitosamente")

            // Listar keys
            val keys = secrets?.keys()
            if (keys != null && keys.hasNext()) {
                Log.d(TAG, "Keys encontradas en el JSON:")
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = secrets?.optString(key, "")
                    Log.d(TAG, "  - '$key' = '${value?.take(20)}...'")
                }
            } else {
                Log.e(TAG, "❌ No se encontraron keys en el JSON")
            }

        } catch (e: java.io.FileNotFoundException) {
            Log.e(TAG, "❌❌❌ ARCHIVO NO ENCONTRADO ❌❌❌")
            Log.e(TAG, "secret_config.json NO existe en app/src/main/assets/")
            Log.e(TAG, "Ubicación esperada: app/src/main/assets/secret_config.json")
        } catch (e: org.json.JSONException) {
            Log.e(TAG, "❌ ERROR parseando JSON", e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inesperado", e)
            e.printStackTrace()
        }

        Log.d(TAG, "========== FIN INIT (secrets ${if (secrets == null) "NULL" else "OK"}) ==========")
    }

    fun get(key: String): String {
        Log.d(TAG, "→ get('$key') llamado")

        if (secrets == null) {
            Log.e(TAG, "❌ secrets es NULL - init() falló o no se llamó")
            return ""
        }

        val value = secrets?.optString(key)

        if (value.isNullOrBlank()) {
            Log.e(TAG, "❌ Key '$key' no encontrada o vacía")
            Log.d(TAG, "Keys disponibles: ${secrets?.keys()?.asSequence()?.toList()}")
            return ""
        }

        Log.d(TAG, "✅ Key '$key' = '${value.take(20)}...'")
        return value
    }
}