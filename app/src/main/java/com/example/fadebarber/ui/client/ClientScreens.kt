package com.example.fadebarber.ui.client
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.example.fadebarber.ui.client.pages.HomePage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClientScreens(route: String) {
    when (route) {
        "home" -> HomePage()
        "account" -> Text("Pantalla Cuenta Cliente")
        "date" -> Text("Pantalla Citas Cliente")
    }
}