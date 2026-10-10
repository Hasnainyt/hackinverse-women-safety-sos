package com.brokencoders.narisuraksha.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.ui.theme.BorderDivider
import com.brokencoders.narisuraksha.ui.theme.ButtonCornerRadius
import com.brokencoders.narisuraksha.ui.theme.ButtonShape
import com.brokencoders.narisuraksha.ui.theme.CardCornerRadius
import com.brokencoders.narisuraksha.ui.theme.CardShape
import com.brokencoders.narisuraksha.ui.theme.ChipShape
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedDark
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.NariSurakshaTheme
import com.brokencoders.narisuraksha.ui.theme.PillShape
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.PrimaryPinkPressed
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface
import com.brokencoders.narisuraksha.ui.theme.Spacing
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.WarningAmber

// =========================================================================
// 1. PrimaryButton
// Solid pink, white text, 64dp tall, full width, 20dp radius, icon + label.
// =========================================================================
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 64.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPink,
            contentColor = Color.White,
            disabledContainerColor = PrimaryPink.copy(alpha = 0.38f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
        }
    }
}

// Backward compatibility alias
@Composable
fun PrimaryAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = PrimaryButton(text, onClick, modifier, icon, enabled)

// =========================================================================
// 2. SecondaryButton
// White/surface fill, 2dp pink border, pink text, 64dp tall (or 56dp min).
// =========================================================================
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 64.dp
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        border = BorderStroke(2.dp, PrimaryPink),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryPink,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            disabledContentColor = PrimaryPink.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = PrimaryPink
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = PrimaryPink
            )
        }
    }
}

// Backward compatibility alias
@Composable
fun SecondaryAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = SecondaryButton(text, onClick, modifier, icon, enabled)

// =========================================================================
// 3. DangerButton
// Solid Emergency Red (#D32F2F), white text, 64dp tall, 20dp radius.
// =========================================================================
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 64.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = EmergencyRed,
            contentColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
        }
    }
}

// Backward compatibility alias
@Composable
fun DangerAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = DangerButton(text, onClick, modifier, icon, enabled)

// =========================================================================
// 4. StatusChip
// Pill: ALWAYS icon + text + color. Status is never indicated by color alone.
// =========================================================================
@Composable
fun StatusChip(
    label: String,
    icon: ImageVector,
    statusColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = SoftPinkSurface
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// =========================================================================
// 5. InfoCard
// 24dp card radius, subtle border, solid surface color, 20dp internal padding.
// =========================================================================
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(1.5.dp, borderColor),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(Spacing.screenPadding)) {
                content()
            }
        }
    } else {
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(1.5.dp, borderColor),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(Spacing.screenPadding)) {
                content()
            }
        }
    }
}

// Backward compatibility alias
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) = InfoCard(modifier, onClick, backgroundColor, borderColor, content)

// =========================================================================
// 6. CounterTile
// Large number, title label, and icon for diagnostics/telemetry.
// =========================================================================
@Composable
fun CounterTile(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDemoScale: Boolean = false
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(if (isDemoScale) 20.dp else 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isDemoScale) 16.sp else 13.sp
                    ),
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(if (isDemoScale) 36.dp else 28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(if (isDemoScale) 22.dp else 16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isDemoScale) 40.sp else 28.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = color
            )
        }
    }
}

// =========================================================================
// 7. SosButton
// Centered 220dp solid pink button, white shield icon, label "Hold for help",
// slow pulse ring (respects reduce-motion).
// =========================================================================
@Composable
fun SosButton(
    isBroadcasting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = if (isBroadcasting) "BROADCASTING" else "Hold for help"
) {
    val context = LocalContext.current
    val isReduceMotion = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isReduceMotion) 1f else 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isReduceMotion) 0.2f else 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    val heroColor = if (isBroadcasting) EmergencyRed else PrimaryPink
    val outerAuraColor = if (isBroadcasting) EmergencyRed.copy(alpha = 0.15f) else SoftPinkSurface

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer animated ring (disabled scaling if reduce-motion)
        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(if (isBroadcasting) pulseScale else if (isReduceMotion) 1f else 1.08f)
                .clip(CircleShape)
                .background(
                    if (isBroadcasting) EmergencyRed.copy(alpha = pulseAlpha)
                    else outerAuraColor
                )
        )

        // Middle solid halo
        Box(
            modifier = Modifier
                .size(235.dp)
                .clip(CircleShape)
                .background(
                    if (isBroadcasting) EmergencyRed.copy(alpha = 0.2f)
                    else SoftPinkSurface
                )
        )

        // Centered 220dp Hero Button (Solid color, subtle elevation shadow, no gradients)
        Box(
            modifier = Modifier
                .size(220.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = heroColor.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(heroColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 110.dp, color = Color.White),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isBroadcasting) Icons.Default.Warning else Icons.Default.Shield,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isBroadcasting) 18.sp else 20.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =========================================================================
// 8. SectionHeader
// Clean section title with high-contrast typography and consistent spacing.
// =========================================================================
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null) {
            action()
        }
    }
}

// =========================================================================
// 9. EmptyState
// Friendly flat illustration + headline + body.
// =========================================================================
@Composable
fun EmptyState(
    icon: ImageVector,
    headline: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SoftPinkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPink,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(20.dp))
            action()
        }
    }
}

// =========================================================================
// 10. ConfirmDialog
// Accessible modal confirmation dialog.
// =========================================================================
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Cancel",
    isDanger: Boolean = false,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDanger) EmergencyRed else PrimaryPink,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else null,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDanger) EmergencyRed else PrimaryPink,
                    contentColor = Color.White
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextSecondary)
            }
        },
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// =========================================================================
// 11. MeshStatusBadge
// Mesh health indicator with icon + text + color.
// =========================================================================
@Composable
fun MeshStatusBadge(
    isMeshActive: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isMeshActive) SafeGreen else WarningAmber
    val containerColor = if (isMeshActive) SafeGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
    val statusText = if (isMeshActive) "BLE Mesh Active" else "Mesh Offline"
    val icon = if (isMeshActive) Icons.Default.CheckCircle else Icons.Default.Warning

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = statusColor
        )
    }
}

// =========================================================================
// 12. AppBottomBar
// Bottom navigation bar: 72dp tall, max 4 items, 24dp icons with 14sp+ labels.
// =========================================================================
@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "SOS",
                icon = Icons.Default.Shield,
                isSelected = currentRoute == "home",
                onClick = { onNavigate("home") }
            )
            BottomBarItem(
                label = "Safe Zones",
                icon = Icons.Default.LocalPolice,
                isSelected = currentRoute == "safe_zones",
                onClick = { onNavigate("safe_zones") }
            )
            BottomBarItem(
                label = "Contacts",
                icon = Icons.Default.Person,
                isSelected = currentRoute == "emergency_contacts",
                onClick = { onNavigate("emergency_contacts") }
            )
            BottomBarItem(
                label = "Radar",
                icon = Icons.Default.Map,
                isSelected = currentRoute == "map",
                onClick = { onNavigate("map") }
            )
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = PrimaryPink
    val inactiveColor = TextSecondary

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) SoftPinkSurface else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) activeColor else inactiveColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

// =========================================================================
// Component Previews (Light & Dark Mode)
// =========================================================================

@Preview(name = "Components Light Mode", showBackground = true)
@Composable
fun ComponentsPreviewLight() {
    NariSurakshaTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrimaryButton(text = "Primary Button (64dp)", onClick = {})
                SecondaryButton(text = "Secondary Button (64dp)", onClick = {})
                DangerButton(text = "Danger Action (64dp)", onClick = {})
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(label = "Bluetooth On", icon = Icons.Default.Bluetooth, statusColor = SafeGreen)
                    MeshStatusBadge(isMeshActive = true)
                }
                CounterTile(title = "Packets Sent", count = 42, icon = Icons.Default.Radio, color = GuardianBlue)
            }
        }
    }
}

@Preview(name = "Components Dark Mode", showBackground = true)
@Composable
fun ComponentsPreviewDark() {
    NariSurakshaTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrimaryButton(text = "Primary Button Dark", onClick = {})
                SecondaryButton(text = "Secondary Button Dark", onClick = {})
                DangerButton(text = "Danger Action Dark", onClick = {})
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(label = "Bluetooth On", icon = Icons.Default.Bluetooth, statusColor = SafeGreen)
                    MeshStatusBadge(isMeshActive = true)
                }
                CounterTile(title = "Packets Sent", count = 42, icon = Icons.Default.Radio, color = GuardianBlue)
            }
        }
    }
}
