package com.example.dingo

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dingo.dingodex.DingoDexScreen
import com.example.dingo.social.ClassroomScreen
import com.example.dingo.social.SocialScreen
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted


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
    object Classroom : NavBarItem(
        name = "Classroom",
        route = "classroom",
        icon = Icons.Filled.Face,
    )
}



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Suppresses error for not using it: Padding Values
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState =  SheetState(
            true,
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()
    val corner_radius = 16.dp
    var offsetY by remember{ mutableStateOf(0f) }
    BottomSheetScaffold(
        sheetContent = {
            DingoDexScreen()
        },
        sheetPeekHeight = 0.dp,
        scaffoldState = scaffoldState,
        sheetShape = RoundedCornerShape(corner_radius),
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val y = dragAmount.y
                when {
                    y > 0 -> {
                        scope.launch {
                            if (scaffoldState.bottomSheetState.hasExpandedState) {
                                scaffoldState.bottomSheetState.hide()
                            }
                        }
                    }
                    y < 0 -> {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                            if (!scaffoldState.bottomSheetState.hasExpandedState) {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                }
                offsetY += dragAmount.y
            }
        }
    ) {
        Scaffold(
            bottomBar = { navBar(navController) }
        ) {
            navigationConfiguration(navController,
            )
        }
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
        composable(NavBarItem.Classroom.route) {
            ClassroomScreen()
        }
        composable(NavBarItem.Social.route) {
            SocialScreen()
        }
    }
}
@Composable
private fun navBar(navController: NavHostController) {
    val navItems = listOf(NavBarItem.Trips, NavBarItem.Scanner, NavBarItem.Social, NavBarItem.Classroom)
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