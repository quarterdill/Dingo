package com.example.dingo.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dingo.ui.theme.color_background
import com.example.dingo.ui.theme.color_on_secondary
import com.example.dingo.ui.theme.color_secondary

@Composable
fun NoPermission(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(color = color_background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "warning_icon"
        )
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Please grant permission to use the Camera",
            textAlign = TextAlign.Center
        )
        Button(onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)) {
            Text("Try Again ")
        }
    }
}
