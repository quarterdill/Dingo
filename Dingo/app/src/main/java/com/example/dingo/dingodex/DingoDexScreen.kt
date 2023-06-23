package com.example.dingo.dingodex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DingoDexScreen(
    viewModel: DingoDexViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()//Do this to make sheet expandable
            .background(Color.Black.copy(0.2f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "DingoDex")
        var showFaunaDingoDex by remember { mutableStateOf(true) }
        val faunaDingoDex = viewModel.fetchDingoDexFaunaCollection.observeAsState()
        val floraDingoDex = viewModel.fetchDingoDexFloraCollection.observeAsState()

        println("$showFaunaDingoDex")
        val temp = if (showFaunaDingoDex && faunaDingoDex != null) {
            println("true")
            faunaDingoDex.value
        } else if (floraDingoDex != null) {
            println("false")
            floraDingoDex.value
        } else {
            null
        }
        println("wrwerawreawrawef $temp")
        if (temp != null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3)
            ) {
                items(temp.size) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${temp[it].numEncounters}")
                        Text(text = "${temp[it].name}",)
                    }
                }
            }

        }
        Row {
            Button(
                onClick = {
                    showFaunaDingoDex = true
                },
            ) {
                Text(text = "Fauna")
            }
            Button(
                onClick = {
                    showFaunaDingoDex = false
                }
            ) {
                Text(text = "Flora")
            }
        }
    }
}

@Composable
private fun DingoDexSelectionButtons(
    viewModel: DingoDexViewModel = hiltViewModel()
) {

}