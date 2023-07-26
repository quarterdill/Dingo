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
import com.example.dingo.UIConstants
import com.example.dingo.model.DingoDexEntryListings
import com.example.dingo.model.DingoDexScientificToIndex
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
    object Main : DingoDexNavItem(
        name = "Main",
        route = "main",
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
                                DingoDexItem(items[it], navController)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                                modifier = Modifier.padding(4.dp),
                                fontSize = 16.sp,
                                text = "Back",
                            )
                        }
                    }
                    Row() {
                        Image(
                            painter = painterResource(R.drawable.fauna_placeholder),
                            contentDescription = "Fauna",
                            contentScale = ContentScale.Inside,
                            alignment = Alignment.CenterStart,
                        )
                        Text(
                            textAlign = TextAlign.Left,
                            modifier = Modifier.width(200.dp),
                            fontSize = 16.sp,
                            text = """
                                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod 
                                    tempor incididunt ut labore et dolore magna aliqua. Ac auctor augue mauris 
                                    augue neque gravida in fermentum et. Id faucibus nisl tincidunt eget nullam 
                                    non nisi est sit. Aliquam faucibus purus in massa tempor nec feugiat. Mollis 
                                    nunc sed id semper risus in hendrerit gravida. Felis eget velit aliquet 
                                    sagittis id consectetur purus ut. 
                                   """.trimIndent()
                        )
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
) {
    val currentContext = LocalContext.current
    val assetManager: AssetManager = currentContext.assets
    // TODO: Implement for flora
    val index = DingoDexScientificToIndex.dingoDexFaunaScientificToIndex[item.name]
    val bitmap = BitmapFactory.decodeStream(currentContext.assets.open(DingoDexEntryListings.dingoDexEntryList[0].default_picture_name))
    //Bitmap bit = BitmapFactory.decodeFile( DingoDexEntryListings.getInstance(currentContext).dingoDexEntryList[0].default_picture_name)
    Button(onClick = {navController.navigate(DingoDexNavItem.Description.route)}) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box() {
                if (item.pictureURL == "") {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
//                        painter = if (item.isFauna) {
//                            painterResource(Drawable.createFromPath(DingoDexEntryListings.getInstance(currentContext).dingoDexEntryList[0].default_picture_name))
//                            //painterResource(R.drawable.fauna_placeholder)
//                        } else {
//                            painterResource(R.drawable.flore_placeholder)
//                        },
                        contentDescription = if (item.isFauna) "Fauna" else "Flora",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)  // clip to the circle shape
                            .border(2.dp, Color.Gray, CircleShape)
                    )
                }
                // TODO: Actual images
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
                modifier = Modifier.width(72.dp),
                text = item.name,
                textAlign = TextAlign.Center
            )
        }
    }
}
