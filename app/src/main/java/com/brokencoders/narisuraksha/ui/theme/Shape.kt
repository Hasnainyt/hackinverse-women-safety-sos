package com.brokencoders.narisuraksha.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ==========================================
// Nari-Suraksha Shape & Spacing Tokens
// Unified radius scale and 8dp grid system
// ==========================================

val CardCornerRadius = 24.dp
val ButtonCornerRadius = 20.dp
val DialogCornerRadius = 24.dp
val ChipCornerRadius = 16.dp
val PillCornerRadius = 32.dp

val CardShape = RoundedCornerShape(CardCornerRadius)
val ButtonShape = RoundedCornerShape(ButtonCornerRadius)
val DialogShape = RoundedCornerShape(DialogCornerRadius)
val ChipShape = RoundedCornerShape(ChipCornerRadius)
val PillShape = RoundedCornerShape(PillCornerRadius)

val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(ChipCornerRadius),
    large = RoundedCornerShape(ButtonCornerRadius),
    extraLarge = RoundedCornerShape(CardCornerRadius)
)

// 8dp Spacing Scale
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val screenPadding = 20.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
}
