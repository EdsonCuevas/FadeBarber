package com.example.fadebarber.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import java.io.FileOutputStream

object CloudinaryHelper {

    private var isInitialized = false

    // Inicializar Cloudinary - llama esto en tu Application class o al inicio de la app
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                val config = mapOf(
                    "cloud_name" to "drwlz40o6", // Reemplaza con tu cloud name
                    "api_key" to "252161548229833",        // Reemplaza con tu API key
                    "api_secret" to "H_NKQp1YNlx0fL4rDrxZ-VSoGiU"   // Reemplaza con tu API secret
                )
                MediaManager.init(context, config)
                isInitialized = true
                Log.d("CloudinaryHelper", "Cloudinary inicializado correctamente")
            } catch (e: Exception) {
                Log.e("CloudinaryHelper", "Error inicializando Cloudinary", e)
            }
        }
    }

    /**
     * Sube una imagen a Cloudinary
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @param userId ID del usuario para organizar las imágenes
     * @param onSuccess Callback con la URL de la imagen subida
     * @param onError Callback con el mensaje de error
     * @param onProgress Callback opcional con el progreso de subida (0-100)
     */
    fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        userId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onProgress: ((Int) -> Unit)? = null
    ) {
        try {
            if (!isInitialized) {
                initialize(context)
            }

            // Convertir URI a File temporal
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = File(context.cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")

            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Subir imagen con opciones de transformación
            MediaManager.get()
                .upload(tempFile.absolutePath)
                .option("folder", "profile_images")
                .option("public_id", "user_$userId")
                .option("overwrite", true)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CloudinaryHelper", "Iniciando subida: $requestId")
                        onProgress?.invoke(0)
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = ((bytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
                        Log.d("CloudinaryHelper", "Progreso: $progress%")
                        onProgress?.invoke(progress)
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        Log.d("CloudinaryHelper", "Subida exitosa: $resultData")

                        // Obtener URL segura de la imagen
                        val secureUrl = resultData["secure_url"] as? String
                        val url = resultData["url"] as? String
                        val imageUrl = secureUrl ?: url

                        // Limpiar archivo temporal
                        tempFile.delete()

                        if (imageUrl != null) {
                            onSuccess(imageUrl)
                        } else {
                            onError("No se pudo obtener la URL de la imagen")
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("CloudinaryHelper", "Error en subida: ${error.description}")

                        // Limpiar archivo temporal
                        tempFile.delete()

                        onError("Error al subir imagen: ${error.description}")
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w("CloudinaryHelper", "Reintentando subida: ${error.description}")
                    }
                })
                .dispatch()

        } catch (e: Exception) {
            Log.e("CloudinaryHelper", "Error preparando imagen", e)
            onError("Error preparando imagen: ${e.message}")
        }
    }

    /**
     * Elimina una imagen de perfil anterior (opcional)
     * @param publicId El public_id de la imagen en Cloudinary (ej: "user_123456")
     */
    fun deleteProfileImage(
        publicId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Nota: La eliminación requiere hacer una llamada al API REST de Cloudinary
            // con tu API Key y Secret. Por seguridad, es mejor hacer esto desde tu backend.
            // Si quieres implementarlo, necesitarás crear un endpoint en tu servidor.
            Log.w("CloudinaryHelper", "La eliminación debe implementarse en el backend por seguridad")
            onSuccess()
        } catch (e: Exception) {
            Log.e("CloudinaryHelper", "Error eliminando imagen", e)
            onError("Error eliminando imagen: ${e.message}")
        }
    }

    /**
     * Genera una URL de transformación para mostrar miniaturas
     * @param imageUrl URL original de Cloudinary
     * @param width Ancho deseado
     * @param height Alto deseado
     * @return URL transformada
     */
    fun getThumbnailUrl(imageUrl: String, width: Int = 200, height: Int = 200): String {
        return if (imageUrl.contains("cloudinary.com")) {
            // Insertar transformación en la URL
            imageUrl.replace("/upload/", "/upload/w_$width,h_$height,c_fill/")
        } else {
            imageUrl
        }
    }
}