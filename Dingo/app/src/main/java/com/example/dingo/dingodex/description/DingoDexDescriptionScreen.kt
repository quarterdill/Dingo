package com.example.dingo.dingodex.description

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dingo.R

@Composable
fun DingoDexDescriptionScreen(onNavigateToMain: () -> Unit) {
    println("got to description screen!!!")
    Box() {
        println("in the box!!")
        Row() {
            Image(
                painter = painterResource(R.drawable.fauna_placeholder),
                contentDescription = "Fauna",
                contentScale = ContentScale.Inside,
            )
            Text(
                modifier = Modifier.width(72.dp),
                fontSize = 36.sp,
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
        Button(onClick = onNavigateToMain) {
            Text(
                modifier = Modifier.padding(4.dp),
                fontSize = 16.sp,
                text = "Back To DingoDex",
            )
        }
        println("what's wrong")
    }
}