package com.example.dingo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun modeSelectionScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Dingo Logo")
        Text(text = "Select Mode:")
        modeSelectionButton()
    }
}

@Composable
fun modeSelectionButton() {
    Row() {
        Button(
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Standard")
        }
        Button(
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Eduction")
        }
    }
}
