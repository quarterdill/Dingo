package com.example.dingo

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class NavBarItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
) {
    object Trips : NavBarItem(
        name = "Trip",
        route = "trip",
        icon = Icons.Rounded.AddCircle,
    )
    object Scanner : NavBarItem(
        name = "Scanner",
        route = "scan",
        icon = Icons.Filled.Search,
    )
    object Social : NavBarItem(
        name = "Social",
        route = "social",
        icon = Icons.Rounded.Person,
    )
}



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Suppresses error for not using it: Padding Values
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { navBar(navController) }
    ) {
        navigationConfiguration(navController)
    }
}
@Composable
private fun navigationConfiguration(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavBarItem.Scanner.route) {
        composable(NavBarItem.Trips.route) {
            TripsScreen()
        }
        composable(NavBarItem.Scanner.route) {
            ScannerScreen()
        }
        composable(NavBarItem.Social.route) {
            SocialScreen()
        }
    }
}
@Composable
private fun navBar(navController: NavHostController) {
    val navItems = listOf(NavBarItem.Trips, NavBarItem.Scanner, NavBarItem.Social)
    NavigationBar() {
        val currentRoute = getCurrentRoute(navController = navController)
        navItems.forEach{
            val isSelected =  it.route == currentRoute
            NavigationBarItem(
                icon = {
                    Icon(imageVector = it.icon, contentDescription = "temp")
                },
                onClick = {
                    if (!isSelected)
                        navController.navigate(it.route)
                },
                selected = isSelected
            )
        }
    }
}

@Composable
private fun getCurrentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}