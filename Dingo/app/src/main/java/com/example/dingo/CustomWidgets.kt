package com.example.dingo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import com.example.dingo.ui.theme.Purple40
import com.example.dingo.ui.theme.color_primary
import com.example.dingo.ui.theme.color_secondary

@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    content:  @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(size = 12.dp),
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .padding(UIConstants.MEDIUM_PADDING),
            ) {
                content()
            }

        }
    }
}
@Composable
fun CustomSwitch(
    option1: String,
    option2: String,
    modifier: Modifier = Modifier,
    onChanged: (Boolean) -> Unit,
) {
    var isChecked by remember { mutableStateOf(false) }
    val toggleButtonWidth = 160.dp
    val toggleButtonHeight= 42.dp
    val toggleThumbWidth= 80.dp
    val color = color_secondary
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