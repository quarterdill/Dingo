package com.example.dingo.social


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.CustomSwitch
import com.example.dingo.UIConstants
import com.example.dingo.common.SessionInfo
import com.example.dingo.dingodex.DingoDexScreen
import com.example.dingo.model.AccountType
import com.example.dingo.social.profile.ProfileScreen
import com.example.dingo.social.social_feed.SocialFeedScreen
import com.example.dingo.ui.theme.color_background


sealed class SocialNavigationItem(
    val name: String,
    val route: String,
) {
    object SocialFeed : SocialNavigationItem(
        name = "SocialFeed",
        route = "socialfeed",
    )
    object MyProfile : SocialNavigationItem(
        name = "MyProfile",
        route = "myprofile",
    )
    object MyDingoDex : SocialNavigationItem(
        name = "MyDingoDex",
        route = "mydingodex",
    )
}

@Composable
fun SocialScreen(
    navControllerSignOut: NavHostController
) {
//    val dummyUserId = "Q0vMYa9VSh7tyFdLTPgX" // eric shang
//    val dummyUsername = "Eric Shang"
//    val dummyUserId = "U47K9DYLoJLJlXHZrU7l"
//    val dummyUsername = "Dylan Xiao"
//    val dummyUserId = "XQIfyBwIwQKyAfiIDKggy"
//    val dummyUsername = "Simhon Chourasia"

//    var currUserId = dummyUserId
//    var currUsername = dummyUsername
//    var currUser = SessionInfo.currentUser
//    if (currUser != null) {
//        currUserId = currUser.id
//    }
    val navController = rememberNavController()
    Box {
        Column(
            modifier = Modifier.fillMaxSize().background(color = color_background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (SessionInfo.currentUser!!.accountType == AccountType.STANDARD) {
                Text(
                    text = "Socials",
                    fontSize = UIConstants.TITLE_TEXT,
                )
            }
            if (SessionInfo.currentUser!!.accountType == AccountType.STANDARD) {
                CustomSwitch(
                    "Feed", "Profile",
                    modifier = Modifier.padding(UIConstants.MEDIUM_PADDING),
                ) {
                    if (it) {
                        navController.navigate(SocialNavigationItem.MyProfile.route)
                    } else {
                        navController.navigate(SocialNavigationItem.SocialFeed.route)
                    }
                }
            } else {
                ProfileScreen(navControllerSignOut = navControllerSignOut, navController = navController)
            }
            NavHost(
                navController = navController,
                startDestination = SocialNavigationItem.SocialFeed.route
            ) {
                composable(SocialNavigationItem.SocialFeed.route) {
                    SocialFeedScreen()
                }
                composable(SocialNavigationItem.MyProfile.route) {
                    ProfileScreen(navControllerSignOut = navControllerSignOut, navController = navController)
                }
                composable(SocialNavigationItem.MyDingoDex.route) {
                    println("sdfgasdfasdf")
                    DingoDexScreen(userId = SessionInfo.currentUserID)
                }
            }
        }
    }
}
