package com.example.dingo.social

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.model.DingoDexEntryListings

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val totalFlora = DingoDexEntryListings.floraEntryList.size
    val totalFauna = DingoDexEntryListings.faunaEntryList.size
    val numFloraFound = totalFlora - viewModel.getNumUncollectedFlora()
    val numFaunaFound = totalFauna - viewModel.getNumUncollectedFauna()
    val achievements = viewModel.getAchievements(LocalContext.current)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Profile")
//      Text(
        Text("Flora: $numFloraFound / $totalFlora")
        Text("Flora: $numFaunaFound / $totalFauna")

        Text("Achievements: ")
        LazyColumn(
            modifier = Modifier.weight(1.0f, true)
        ) {
            items(achievements.size) {
                Text(
                    modifier = Modifier.height(20.dp),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    text ="${achievements[it].name})",
                )
                Text(
                    modifier = Modifier.padding(all = 12.dp),
                    text = "${achievements[it].description}"
                )
                Divider(
                    thickness = 1.dp,
                    color = Color.Gray,
                )
            }
        }
    }
}