package com.example.dingo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NoPermission(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
        Button(onClick = onRequestPermission) {
            Text("Try Again ")
        }
    }
}

@Composable
fun NoPermissionContent(
    onRequestPermission: () -> Unit
) {
    Text(text = "Please grant permission to use the Camera")
    Button(onClick = onRequestPermission) {

    }
}