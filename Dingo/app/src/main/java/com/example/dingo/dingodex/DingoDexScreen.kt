package com.example.dingo.dingodex

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.R

// Todo: Make heights and stuff into consts
@Composable
fun DingoDexScreen(
    viewModel: DingoDexViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            fontSize = 36.sp,
            text = "DingoDex",
        )
        var showFaunaDingoDex by remember { mutableStateOf(true) }
        val collectedFaunaDingoDex = viewModel.collectedDingoDexFauna.observeAsState()
        val uncollectedFaunaDingoDex = viewModel.uncollectedDingoDexFauna.observeAsState()
        val collectedFloraDingoDex = viewModel.collectedDingoDexFlora.observeAsState()
        val uncollectedFloraDingoDex = viewModel.uncollectedDingoDexFlora.observeAsState()

        println("$showFaunaDingoDex")
        val isNull = if (showFaunaDingoDex) {
            collectedFaunaDingoDex.value == null || uncollectedFaunaDingoDex.value == null
        } else {
            collectedFloraDingoDex.value == null || uncollectedFloraDingoDex.value == null
        }
        if (!isNull) {
            val items: List<DingoDexCollectionItem> = if (showFaunaDingoDex) {
                collectedFaunaDingoDex.value!! + uncollectedFaunaDingoDex.value!!
            } else {
                collectedFloraDingoDex.value!! + uncollectedFloraDingoDex.value!!
            }
            LazyVerticalGrid(
                modifier = Modifier.weight(1.0f),
                columns = GridCells.Fixed(3),
            ) {
                items(items.size) {
                    DingoDexItem(items[it])
                }
            }
        } else {
            Text("Loading")
        }
        Row (
            modifier = Modifier.padding(16.dp),
            horizontalArrangement  = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Button(
                onClick = { showFaunaDingoDex = true },
            ) {
                Text(text = "Fauna")
            }
            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = Color.Gray,
            )
            Button(
                onClick = { showFaunaDingoDex = false }
            ) {
                Text(text = "Flora")
            }
        }
    }
}

@Composable
private fun DingoDexItem(
    item: DingoDexCollectionItem
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box() {
            if (item.pictureURL == "") {
                Image(
                    painter = if (item.isFauna) {
                        painterResource(R.drawable.fauna_placeholder)
                    } else {
                        painterResource(R.drawable.flore_placeholder)
                    },
                    contentDescription = if (item.isFauna) "Fauna" else "Flora",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)                       // clip to the circle shape
                        .border(2.dp, Color.Gray, CircleShape)
                )
            }
            // TODO: Actual images
            Box (
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(25.dp), onDraw = {
                    drawCircle(color = Color.LightGray)
                })
                Text(text = "${item.numEncounters}", color = Color.White)
            }
        }
        Text(
            modifier = Modifier.width(72.dp),
            text = "${item.name}",
            textAlign = TextAlign.Center
        )
    }
}