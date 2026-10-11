package com.brokencoders.narisuraksha.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface

/**
 * Redesigned Hero SOS Button:
 * - NO gradients: solid colors only.
 * - Idle: Solid Primary Pink (#C2185B) with Soft Pink Surface (#FCE4EC) outer ring.
 * - Active: Solid Emergency Red (#D32F2F) indicating genuine distress.
 * - Stress-proof: 180dp direct touch target for shaky hands.
 */
@Composable
fun SosPulseButton(
    isBroadcasting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    val heroColor = if (isBroadcasting) EmergencyRed else PrimaryPink
    val outerAuraColor = if (isBroadcasting) EmergencyRed.copy(alpha = 0.15f) else SoftPinkSurface

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer animated breathing ring
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(if (isBroadcasting) pulseScale else 1.12f)
                .clip(CircleShape)
                .background(
                    if (isBroadcasting) EmergencyRed.copy(alpha = pulseAlpha)
                    else outerAuraColor
                )
        )

        // Middle solid halo
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    if (isBroadcasting) EmergencyRed.copy(alpha = 0.2f)
                    else SoftPinkSurface
                )
        )

        // Inner 170dp Hero Button (Solid color, subtle elevation shadow, no gradients)
        Box(
            modifier = Modifier
                .size(170.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, spotColor = heroColor.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(heroColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 85.dp, color = Color.White),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isBroadcasting) Icons.Default.Warning else Icons.Default.Shield,
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBroadcasting) "ACTIVE SOS" else "Tap for SOS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isBroadcasting) 16.sp else 20.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
