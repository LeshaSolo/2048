package com.example.noblenumbers.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NoblePalette {
    val DeepWood = Color(0xFF15100B)
    val DarkWalnut = Color(0xFF24170E)
    val MediumWalnut = Color(0xFF3A2515)
    val WarmEdge = Color(0xFF6A4524)
    val WoodPanel = Color(0xFF2D1B10)
    val Parchment = Color(0xFFE6D2A8)
    val DarkParchment = Color(0xFFCBB17E)
    val Brass = Color(0xFFA8792A)
    val Gold = Color(0xFFD1A64B)
    val GoldLight = Color(0xFFF1D888)
    val PrimaryGreen = Color(0xFF566B24)
    val PrimaryGreenDark = Color(0xFF344016)
    val DangerRedBrown = Color(0xFF8A3926)
    val DisabledBrownGray = Color(0xFF4A4032)
    val Ink = Color(0xFF26180E)
    val Ivory = Color(0xFFF4E8C8)
    val Burgundy = Color(0xFF773133)
    val Olive = Color(0xFF6D7A37)
    val MutedBlue = Color(0xFF5E7387)
    val IceBlue = Color(0xFFBEEBFF)
    val FrostWhite = Color(0xFFF4FBFF)
    val DeepIceShadow = Color(0xFF416E7D)
    val FrozenTextShadow = Color(0xFF1B3842)
}

val WoodDark = NoblePalette.DarkWalnut
val Wood = NoblePalette.MediumWalnut
val WoodLight = NoblePalette.WarmEdge
val Parchment = NoblePalette.Parchment
val Gold = NoblePalette.Gold
val Burgundy = NoblePalette.Burgundy
val Olive = NoblePalette.Olive
val MutedBlue = NoblePalette.MutedBlue

private val NobleColorScheme = darkColorScheme(
    primary = NoblePalette.Gold,
    onPrimary = NoblePalette.Ink,
    secondary = NoblePalette.Parchment,
    onSecondary = NoblePalette.Ink,
    background = NoblePalette.DeepWood,
    onBackground = NoblePalette.Parchment,
    surface = NoblePalette.WoodPanel,
    onSurface = NoblePalette.Parchment,
    error = NoblePalette.DangerRedBrown,
    onError = NoblePalette.Parchment,
)

@Composable
fun NobleNumbersTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NobleColorScheme,
        typography = NobleTypography,
        content = content,
    )
}
