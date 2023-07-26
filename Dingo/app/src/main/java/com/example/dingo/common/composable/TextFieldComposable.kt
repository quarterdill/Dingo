package com.example.dingo.common.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.dingo.R
import com.example.dingo.ui.theme.PurpleGrey80

@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.background(color = PurpleGrey80),
        singleLine = true,
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(text="Email")  },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = Color.Gray,
            )
        }
    )
//    TextField(
//        singleLine = true,
//        value = uiState.email,
//        onValueChange = {viewModel.onEmailChange(it)},
//        placeholder = { Text(text="Email") },
//    )
}

@Composable
fun UsernameField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.background(color = PurpleGrey80),
        singleLine = true,
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(text="Username")  },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Username",
                tint = Color.Gray,
            )
        }
    )
//    TextField(
//        singleLine = true,
//        value = uiState.email,
//        onValueChange = {viewModel.onEmailChange(it)},
//        placeholder = { Text(text="Email") },
//    )
}

@Composable
fun DisplayPasswordField(value: String, onNewValue: (String) -> Unit) {
    PasswordField(value, "Password", onNewValue)
}

@Composable
fun RepeatPasswordField(
    value: String,
    onNewValue: (String) -> Unit,
) {
    PasswordField(value, "Repeat Password", onNewValue)
}

@Composable
private fun PasswordField(
    value: String,
    placeholder: String,
    onNewValue: (String) -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }

//    val icon = if (isVisible) {
//        Icons.Filled.
//    } else {
//        Icons.Filled.VisibilityOff
//    }

    val icon = ImageVector.vectorResource(id = R.drawable.baseline_remove_red_eye_24)
    val imageVector = rememberVectorPainter(icon)
    val visualTransformation =
        if (isVisible) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        modifier = Modifier.background(color = PurpleGrey80),
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = Color.Gray,
            )
        },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    modifier = Modifier.height(25.dp).width(25.dp),
                    painter = imageVector,
                    contentDescription = "Visibility",
                    tint = Color.Gray,
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation
    )
}