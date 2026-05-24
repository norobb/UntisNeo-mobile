package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer

val NothingBlack @Composable get() = MaterialTheme.colorScheme.background
val NothingDarkGray @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val NothingCardGray @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val NothingWhite @Composable get() = MaterialTheme.colorScheme.onSurface
val NothingRealWhite = Color(0xFFFFFFFF)
val NothingRed @Composable get() = MaterialTheme.colorScheme.primary
val NothingMutedGray @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun NothingHeader(
    text: String = "",
    title: String = text,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    showRedDot: Boolean = true
) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp, top = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (showRedDot) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            )
        }
    }
}

@Composable
fun NothingCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NothingCardGray,
    content: @Composable () -> Unit
) {
    val borderColor = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        Color(0xFF222222)
    } else {
        Color(0xFFE2E8F0)
    }
    Card(
        modifier = modifier.padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun NothingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    if (isPrimary) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = text.uppercase(),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 14.sp
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = text.uppercase(),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun NothingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)) },
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
