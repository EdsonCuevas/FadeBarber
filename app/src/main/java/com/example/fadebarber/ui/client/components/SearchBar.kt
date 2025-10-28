package com.example.fadebarber.ui.client.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    compact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 16.dp else 20.dp))
            .background(Color(0xFFF1F1F1))
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 6.dp else 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Buscar",
            tint = Color.Gray,
            modifier = Modifier.size(if (compact) 16.dp else 20.dp)
        )
        Spacer(Modifier.width(if (compact) 6.dp else 8.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder, fontSize = if (compact) 12.sp else 14.sp, color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
    }
}