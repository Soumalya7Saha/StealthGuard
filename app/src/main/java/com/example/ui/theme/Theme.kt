package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeometricBalanceColorScheme = darkColorScheme(
    primary = CalcOpKeyBg, // #D0BCFF
    onPrimary = CalcOpKeyText, // #381E72
    primaryContainer = CalcEqualsBg, // #EADDFF
    onPrimaryContainer = CalcEqualsText, // #21005D
    secondary = StealthShieldBlue,
    onSecondary = Color(0xFF21005D),
    tertiary = StealthAlertRed,
    background = CalcBlack, // #1C1B1F
    onBackground = TextPrimary, // #E6E1E5
    surface = CalcSurfaceDark, // #1C1B1F
    onSurface = TextPrimary, // #E6E1E5
    surfaceVariant = CalcSurfaceVariant, // #49454F
    onSurfaceVariant = TextSecondary, // #938F99
    outline = DividerDark // #49454F
)

@Composable
fun StealthGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GeometricBalanceColorScheme,
        typography = Typography,
        content = content
    )
}
