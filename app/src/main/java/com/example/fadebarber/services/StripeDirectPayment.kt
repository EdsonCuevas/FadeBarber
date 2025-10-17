package com.example.fadebarber.ui.client.components

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.fadebarber.services.EnvLoad
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetContract
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Hook de Stripe para manejar pagos.
 * Retorna una función que inicia el proceso de pago.
 */
@Composable
fun rememberStripePayment(
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: (String) -> Unit
): (Int) -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Inicializar Stripe con tu Publishable Key
    LaunchedEffect(Unit) {
        PaymentConfiguration.init(
            context,
            "pk_test_51RVceu2MionY2Ilj91otr62CGlji0IRxCnPiUr0rEd9wg7YCNZwoMqIJjR9ielwCvdEwz6LH1iCcq23fCBs4F6yW004jxb65MM"
        )
    }

    // Launcher de Stripe PaymentSheet
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = PaymentSheetContract(),
        onResult = { result: PaymentSheetResult ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    Log.d("Stripe", "Pago completado exitosamente")
                    onPaymentSuccess()
                }
                is PaymentSheetResult.Canceled -> {
                    Log.d("Stripe", "Pago cancelado por el usuario")
                    onPaymentFailed("Pago cancelado ❌")
                }
                is PaymentSheetResult.Failed -> {
                    Log.e("Stripe", "Error en el pago: ${result.error.localizedMessage}")
                    onPaymentFailed(result.error.localizedMessage ?: "Error desconocido ⚠️")
                }
            }
        }
    )

    // Retornar función que inicia el pago
    return { amount ->
        coroutineScope.launch {
            try {
                Log.d("Stripe", "Iniciando proceso de pago por: $$amount MXN")

                // Crear el PaymentIntent directamente con Stripe
                val clientSecret = crearPaymentIntentDirecto(amount)

                if (clientSecret.isNullOrBlank()) {
                    Log.e("Stripe", "ClientSecret es nulo o vacío")
                    onPaymentFailed("Error al conectar con el sistema de pagos")
                    return@launch
                }

                Log.d("Stripe", "Client secret recibido: ${clientSecret.take(20)}...")

                val configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "FadeBarber MX"
                )

                val args = PaymentSheetContract.Args.createPaymentIntentArgs(
                    clientSecret = clientSecret,
                    config = configuration
                )

                paymentLauncher.launch(args)
            } catch (e: Exception) {
                Log.e("Stripe", "Error al crear PaymentIntent", e)
                onPaymentFailed("Error: ${e.message ?: "Error inesperado"}")
            }
        }
    }
}

suspend fun crearPaymentIntentDirecto(amount: Int): String? {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("Stripe", "Creando PaymentIntent para: $amount MXN (${amount * 100} centavos)")
            val env = EnvLoad()
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Stripe API prefiere application/x-www-form-urlencoded
            val formBody = FormBody.Builder()
                .add("amount", (amount * 100).toString())
                .add("currency", "mxn")
                .add("automatic_payment_methods[enabled]", "true")
                .build()

            val request = Request.Builder()
                .url("https://api.stripe.com/v1/payment_intents")
                .post(formBody)
                .addHeader("Authorization", "Bearer ${env["SECRET_STRIPE_KEY"]}")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()

            Log.d("Stripe", "Enviando request a Stripe API...")
            val response = client.newCall(request).execute()
            val resBody = response.body?.string()

            Log.d("Stripe", "Código de respuesta: ${response.code}")
            Log.d("Stripe", "Respuesta completa: $resBody")

            if (!response.isSuccessful) {
                Log.e("Stripe", "Error ${response.code}: $resBody")
                return@withContext null
            }

            // Parsear la respuesta manualmente
            val clientSecret = resBody?.let { body ->
                // Buscar el client_secret en el JSON de forma simple
                val regex = """"client_secret"\s*:\s*"([^"]+)"""".toRegex()
                regex.find(body)?.groupValues?.get(1)
            }

            if (clientSecret != null) {
                Log.d("Stripe", "Client secret obtenido exitosamente")
            } else {
                Log.e("Stripe", "No se pudo extraer el client_secret de la respuesta")
            }

            clientSecret
        } catch (e: Exception) {
            Log.e("Stripe", "Excepción al crear PaymentIntent: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
}