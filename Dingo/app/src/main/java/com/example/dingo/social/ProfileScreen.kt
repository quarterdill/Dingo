package com.example.dingo.social

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.model.DingoDexEntryListings

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val numFloraFound = viewModel.getNumFlora()
    val totalFlora = DingoDexEntryListings.getInstance(LocalContext.current).floraEntryList.size

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Profile")
//      Text(
        Text("Flora: $numFloraFound / $totalFlora")
    }
}