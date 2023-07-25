package com.example.dingo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.dingo.ui.theme.Purple40

@Composable
fun CustomSwitch(
    modifier: Modifier,
    option1: String,
    option2: String,
    onChanged: (Boolean) -> Unit,
) {
    var isChecked by remember { mutableStateOf(false) }
    val toggleButtonWidth = 160.dp
    val toggleButtonHeight= 42.dp
    val toggleThumbWidth= 80.dp
    val color = Purple40
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .size(toggleButtonWidth, toggleButtonHeight)
                .toggleable(
                    value = isChecked,
                    onValueChange = {
                        isChecked = it
                        onChanged(isChecked)
                    }
                )
                .background(shape = RoundedCornerShape(12.dp), color = color)
        ) {
            Box(
                modifier = Modifier
                    .background(shape = RoundedCornerShape(12.dp), color = color)
                    .height(toggleButtonHeight)
                    .width(toggleThumbWidth),
                contentAlignment = Alignment.Center,
            ) {

                Text(
                    text = option1,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )

            }
            Box(
                modifier = Modifier
                    .background(shape = RoundedCornerShape(12.dp), color = color)
                    .height(toggleButtonHeight)
                    .width(toggleThumbWidth)
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center,
            ) {

                Text(
                    text = option2,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )

            }

            // Thumb
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX =
                            if (isChecked) (toggleButtonWidth - toggleThumbWidth).toPx() else 0f
                    }
            ) {
                Box(
                    modifier = Modifier
                        .background(shape = RoundedCornerShape(12.dp), color = Color.White,)
                        .height(toggleButtonHeight)
                        .width(toggleThumbWidth),
                    contentAlignment = Alignment.Center,
                ) {

                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = if (!isChecked) option1 else option2,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}