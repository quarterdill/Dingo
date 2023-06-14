package com.example.dingo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavBarItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
)

val navItems = listOf(
    NavBarItem(
        name = "Trip",
        route = "trip",
        icon = Icons.Rounded.AddCircle,
    ),
    NavBarItem(
        name = "Scanner",
        route = "scan",
        icon = Icons.Filled.Search,
    ),
    NavBarItem(
        name = "Social",
        route = "social",
        icon = Icons.Rounded.Person,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scannerScreen() {
    Scaffold(
        bottomBar = { navBar() }
    ) {
        it
        modeSelectionScreen()
    }
}

@Composable
fun navBar() {
    NavigationBar() {
        navItems.forEach{
            NavigationBarItem(
                icon = {
                    Icon(imageVector = it.icon, contentDescription = "temp")
                },
                onClick = { /*TODO*/ },
                selected = false
            )
        }
    }
}