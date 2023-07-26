package com.example.dingo.dingodex

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dingo.R
import com.example.dingo.common.SessionInfo
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.DingoDexEntryContent
import com.example.dingo.UIConstants
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.DingoDexScientificToIndex
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileInputStream


sealed class DingoDexNavItem(
    val name: String,
    val route: String,
) {
    object Description : DingoDexNavItem(
        name = "Description",
        route = "description",
     )
    object DingoDex : DingoDexNavItem(
        name = "DingoDex",
        route = "dingodex"
    )
}

// TODO: Make heights and stuff into constants
@Composable
fun DingoDexScreen(
    viewModel: DingoDexViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val selected = remember { mutableStateOf("")}
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(
            navController = navController,
            startDestination = DingoDexNavItem.DingoDex.route
        ) {
            composable(DingoDexNavItem.DingoDex.route) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.padding(UIConstants.MEDIUM_PADDING),
                        fontSize = UIConstants.TITLE_TEXT,
                        text = "DingoDex",
                    )
                    var showFaunaDingoDex by remember { mutableStateOf(true) }
                    val collectedFaunaDingoDex = viewModel.collectedDingoDexFauna.observeAsState()
                    val uncollectedFaunaDingoDex =
                        viewModel.uncollectedDingoDexFauna.observeAsState()
                    val collectedFloraDingoDex = viewModel.collectedDingoDexFlora.observeAsState()
                    val uncollectedFloraDingoDex =
                        viewModel.uncollectedDingoDexFlora.observeAsState()

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
                                DingoDexItem(items[it], navController, selected)
                            }
                        }
                    } else {
                        Text("Loading")
                    }
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
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
            composable(DingoDexNavItem.Description.route) {
                val currentContext = LocalContext.current
                val assetManager: AssetManager = currentContext.assets
                var index = DingoDexScientificToIndex.dingoDexFaunaScientificToIndex[selected.value]

                if (index == null) {
                    index = DingoDexScientificToIndex.dingoDexFloraScientificToIndex[selected.value]
                }
                if (index == null) {
                    println("DingoDex entry image, ${selected.value} could not be found in json assets!")
                }
                val dingodexEntryContent = DingoDexEntryListings.dingoDexEntryList[index!!]
                var bitmap = BitmapFactory.decodeStream(assetManager.open(dingodexEntryContent.default_picture_name))

                val dingodexEntry: List<DingoDexEntry> = viewModel.getEntry(userId = SessionInfo.currentUserID, entryName = selected.value!!)
                if (dingodexEntry.size == 1 && dingodexEntry[0].displayPicture != "default") {
                    val storageRef = FirebaseStorage.getInstance().reference.child("temp/${selected.value}.jpg")
                    storageRef.getBytes(1000000).addOnSuccessListener {
                        bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    }.addOnFailureListener {
                        println("Error occurred when downloading user's DingoDex image from Firebase $it")
                    }
                }
                LazyColumn() {

                }
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            fontSize = 16.sp,
                            text = "${dingodexEntryContent.name} | ${dingodexEntryContent.scientific_name}"
                        )
                    }
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = if (dingodexEntryContent.is_fauna) "Fauna" else "Flora",
                            contentScale = ContentScale.Inside,
                            alignment = Alignment.CenterStart,
                        )
                    }
                    Row() {
                        Text(
                            textAlign = TextAlign.Left,
                            modifier = Modifier.width(300.dp),
                            fontSize = 16.sp,
                            text = dingodexEntryContent.description.trimIndent()
                        )
                    }
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                navController.navigate(DingoDexNavItem.DingoDex.route)
                            },
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "Back",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DingoDexItem(
    item: DingoDexCollectionItem,
    navController: NavHostController,
    selected: MutableState<String>,
    viewModel: DingoDexViewModel = hiltViewModel()
) {
    val currentContext = LocalContext.current
    val assetManager: AssetManager = currentContext.assets
    val index = if (item.isFauna) DingoDexScientificToIndex.dingoDexFaunaScientificToIndex[item.scientificName] else DingoDexScientificToIndex.dingoDexFloraScientificToIndex[item.scientificName]
    val bitmap = BitmapFactory.decodeStream(assetManager.open(DingoDexEntryListings.dingoDexEntryList[index!!].default_picture_name))
    Button(onClick = {
        selected.value = item.scientificName
        navController.navigate(DingoDexNavItem.Description.route)
    }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box() {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = if (item.isFauna) "Fauna" else "Flora",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)  // clip to the circle shape
                        .border(2.dp, Color.Gray, CircleShape)
                )
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(25.dp), onDraw = {
                        drawCircle(color = Color.LightGray)
                    })
                    Text(text = "${item.numEncounters}", color = Color.White)
                }
            }
            Text(
                modifier = Modifier.width(90.dp),
                text = item.name,
                textAlign = TextAlign.Center
            )
        }
    }
}
