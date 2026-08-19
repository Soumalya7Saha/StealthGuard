package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorButton(
    symbol: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
    fontSize: Int = 30,
    testTag: String = "calc_btn_$symbol",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        label = "btn_scale"
    )

    val shape = if (isWide) RoundedCornerShape(percent = 50) else CircleShape

    Box(
        modifier = modifier
            .padding(4.dp)
            .scale(scale)
            .then(if (isWide) Modifier.aspectRatio(2.15f, matchHeightConstraintsFirst = false) else Modifier.aspectRatio(1f))
            .clip(shape)
            .background(if (isPressed) backgroundColor.copy(alpha = 0.75f) else backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = if (isWide) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = symbol,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium,
            modifier = if (isWide) Modifier.padding(start = 28.dp) else Modifier
        )
    }
}
