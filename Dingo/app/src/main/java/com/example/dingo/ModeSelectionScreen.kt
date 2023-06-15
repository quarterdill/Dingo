package com.example.dingo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class ModeSelectionButton(
    val name: String,
    val route: String,
) {
    object Standard : ModeSelectionButton(
        name = "Standard",
        route = "standard",
    )
    object Eduction : ModeSelectionButton(
        name = "Eduction",
        route = "eduction",
    )
}

@Composable
fun ModeSelectionScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Dingo Logo")
        Text(text = "Select Mode:")
        modeSelectionButtons(navController)
    }
}

@Composable
private fun modeSelectionButtons(navController: NavHostController) {
    val selectionButtons = listOf(ModeSelectionButton.Standard, ModeSelectionButton.Eduction)
    Row {
        selectionButtons.forEach {
            Button(
                onClick = { navController.navigate(it.route) }
            ) {
                Text(text = it.name)
            }
        }
    }
}

