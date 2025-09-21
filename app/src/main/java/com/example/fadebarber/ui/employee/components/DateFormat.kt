import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(fecha: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "MX"))
    val date: Date = inputFormat.parse(fecha)!!
    return outputFormat.format(date)
}
