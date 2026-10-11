package com.brokencoders.narisuraksha.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.R
import com.brokencoders.narisuraksha.ble.ReceivedSos
import com.brokencoders.narisuraksha.ui.components.AppBottomBar
import com.brokencoders.narisuraksha.ui.components.CountdownOverlay
import com.brokencoders.narisuraksha.ui.components.HeroBannerCard
import com.brokencoders.narisuraksha.ui.components.LatestUpdatesCard
import com.brokencoders.narisuraksha.ui.components.QuickActionCard
import com.brokencoders.narisuraksha.ui.components.SafetyStatusCard
import com.brokencoders.narisuraksha.ui.components.SectionHeader
import com.brokencoders.narisuraksha.ui.components.StaySafeBanner
import com.brokencoders.narisuraksha.ui.components.WomanShieldLogo
import com.brokencoders.narisuraksha.ui.theme.BorderDivider
import com.brokencoders.narisuraksha.ui.theme.CardBackground
import com.brokencoders.narisuraksha.ui.theme.CardShape
import com.brokencoders.narisuraksha.ui.theme.DarkHeading
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.SafeGreenSurface
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSafeZones: () -> Unit = {},
    onNavigateToDecoy: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToBleDebug: () -> Unit = {},
    onNavigateToAlert: (senderId: Short, lat: Float, lon: Float, rssi: Int) -> Unit = { _, _, _, _ -> },
    onNavigateToSafetyAlerts: () -> Unit = onNavigateToHistory,
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToResources: () -> Unit = onNavigateToSafeZones,
    onNavigateToQuickRecord: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val isSosActive by viewModel.isSosActive.collectAsState()
    val currentCoords by viewModel.currentCoordinates.collectAsState()
    val contacts by viewModel.emergencyContacts.collectAsState()
    val primaryContact = contacts.find { it.isPrimary } ?: contacts.firstOrNull()
    val responderAckCount by viewModel.responderAckCount.collectAsState()
    val latestAlert by viewModel.latestReceivedAlert.collectAsState()
    val isScanEnabled by viewModel.isScanEnabled.collectAsState()

    val lastUpdatedText = remember(currentCoords) {
        currentCoords?.timestamp?.let {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
        } ?: "Today, 10:24 AM"
    }

    // Launch SOS Countdown
    val triggerSos = {
        if (!isSosActive && !isCountingDown) {
            viewModel.triggerSos()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "home" -> { /* Already on Home */ }
                        "safety_alerts", "safety" -> onNavigateToSafetyAlerts()
                        "safe_zones" -> onNavigateToSafeZones()
                        "map", "location" -> onNavigateToMap()
                        "community" -> onNavigateToCommunity()
                        "profile", "settings" -> onNavigateToProfile()
                        "emergency_contacts" -> onNavigateToContacts()
                    }
                },
                onSosClick = triggerSos
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // ==========================================
                // 1. TOP HEADER (Matching Image 1 & 2)
                // Logo, Nari-Suraksha, "Hello, Sister ♥", Notifications & Profile
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        WomanShieldLogo(size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nari-Suraksha",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 19.sp
                                    ),
                                    color = DarkHeading
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "♥",
                                    color = PrimaryPink,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Hello, Sister • Stay safe, stay strong.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextSecondary
                            )
                        }
                    }

                    // Notification & Profile Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Notification bell with unread badge (3)
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(
                                onClick = onNavigateToSafetyAlerts,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = DarkHeading,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPink)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "3",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Profile Avatar
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SoftPinkSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = PrimaryPink,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // ACTIVE BROADCAST NOTIFICATION (IF LIVE SOS)
                // ==========================================
                if (isSosActive) {
                    ActiveSosCard(
                        responderCount = responderAckCount,
                        onStopBroadcast = { viewModel.stopSos() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // ==========================================
                // INCOMING DISTRESS ALERT BANNER (IF RECEIVED)
                // ==========================================
                latestAlert?.let { alert ->
                    IncomingAlertBanner(
                        alert = alert,
                        onOpenAlert = {
                            onNavigateToAlert(
                                alert.packet.senderId,
                                alert.packet.lat,
                                alert.packet.lon,
                                alert.rssi
                            )
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // ==========================================
                // 2. HERO BANNER (Matching Image 1 & 2)
                // "Your Safety, Our Priority", Subtitle, SOS Pill Button, Woman Art
                // ==========================================
                HeroBannerCard(
                    onImmediateHelpClick = triggerSos,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // ==========================================
                // 3. QUICK ACTIONS SECTION (Matching Image 2)
                // 4 Cards: Emergency SOS, Share Location, Safety Mesh, Quick Record
                // ==========================================
                SectionHeader(
                    title = "Quick Actions",
                    actionText = "View All",
                    onActionClick = onNavigateToSafetyAlerts,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Emergency",
                        subtitle = "Alert nearby contacts and authorities.",
                        icon = Icons.Default.Warning,
                        badgeText = "SOS",
                        onClick = triggerSos,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Share Location",
                        subtitle = "Let trusted contacts know where you are.",
                        icon = Icons.Default.LocationOn,
                        onClick = onNavigateToMap,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Safety Mesh",
                        subtitle = "Connect with nearby women & community.",
                        icon = Icons.Default.Shield,
                        onClick = onNavigateToBleDebug,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Quick Record",
                        subtitle = "Capture audio/video evidence offline.",
                        icon = Icons.Default.Videocam,
                        onClick = onNavigateToQuickRecord,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 4. SAFETY STATUS CARD (Matching Image 2)
                // "You are Safe", Location state, Share Location button
                // ==========================================
                SectionHeader(
                    title = "Safety Status",
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                SafetyStatusCard(
                    isSafe = !isSosActive,
                    lastUpdatedText = lastUpdatedText,
                    onShareLocationClick = onNavigateToMap,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // ==========================================
                // 5. LATEST UPDATES SECTION (Matching Image 2)
                // Safety Tips, Community Support, Emergency Contacts
                // ==========================================
                SectionHeader(
                    title = "Latest Updates",
                    actionText = "View All",
                    onActionClick = onNavigateToResources,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LatestUpdatesCard(
                    onTipsClick = onNavigateToResources,
                    onCommunityClick = onNavigateToCommunity,
                    onContactsClick = onNavigateToContacts,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // ==========================================
                // 6. BOTTOM STAY SAFE BANNER (Matching Image 1 & 2)
                // "You are not alone. Help is just a tap away."
                // ==========================================
                StaySafeBanner(
                    onStaySafeClick = triggerSos,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Fullscreen Countdown Overlay with Haptic Ticks
            CountdownOverlay(
                isCountingDown = isCountingDown,
                remainingSeconds = countdownSeconds,
                onCancel = { viewModel.cancelCountdown() }
            )
        }
    }
}

/**
 * Calm active broadcast indicator shown when SOS is live
 */
@Composable
private fun ActiveSosCard(
    responderCount: Int,
    onStopBroadcast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, CardShape, spotColor = EmergencyRed.copy(alpha = 0.25f)),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmergencyRed)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmergencyRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISTRESS SIGNAL BROADCASTING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = EmergencyRed
                    )
                }

                Text(
                    text = "0xFDE1 • 60s",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Help is being alerted. Stay calm.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = DarkHeading
            )
            Text(
                text = "Nearby phones are passing your alert along over offline BLE mesh.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Responder Counter Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (responderCount > 0) SafeGreenSurface else SoftPinkSurface)
                    .border(1.dp, if (responderCount > 0) SafeGreen else BorderDivider, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (responderCount > 0) Icons.Default.CheckCircle else Icons.Default.Radio,
                    contentDescription = null,
                    tint = if (responderCount > 0) SafeGreen else PrimaryPink,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (responderCount > 0) "$responderCount Nearby Responder(s) Acknowledged!" else "Listening for Nearby Responders...",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = if (responderCount > 0) SafeGreen else DarkHeading
                    )
                    Text(
                        text = if (responderCount > 0) "A bystander phone has received your distress packet." else "Transmitting 17-byte BLE beacon across nearby mesh.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onStopBroadcast,
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "I'm Safe (Stop Broadcast)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * High-priority alert banner for incoming nearby distress beacons
 */
@Composable
private fun IncomingAlertBanner(
    alert: ReceivedSos,
    onOpenAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenAlert),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SoftPinkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPink)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🚨 Nearby Distress Beacon Detected",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = EmergencyRed
                    )
                    Text(
                        text = "Sender #${alert.packet.senderId} • Tap to view distance & assistance tools",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Alert",
                tint = PrimaryPink,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
