package com.brokencoders.narisuraksha.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.ui.components.AppBottomBar
import com.brokencoders.narisuraksha.ui.components.AppCard
import com.brokencoders.narisuraksha.ui.components.CountdownOverlay
import com.brokencoders.narisuraksha.ui.components.DangerAppButton
import com.brokencoders.narisuraksha.ui.components.PrimaryAppButton
import com.brokencoders.narisuraksha.ui.components.SecondaryAppButton
import com.brokencoders.narisuraksha.ui.components.SettingsDialog
import com.brokencoders.narisuraksha.ui.components.SosPulseButton
import com.brokencoders.narisuraksha.ui.components.StatusBadge
import com.brokencoders.narisuraksha.ui.theme.BorderDivider
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedDark
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSafeZones: () -> Unit,
    onNavigateToDecoy: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToBleDebug: () -> Unit,
    onNavigateToAlert: (senderId: Short, lat: Float, lon: Float, rssi: Int) -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.ensureGuardianScanActive()
    }

    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val isSosActive by viewModel.isSosActive.collectAsState()
    val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()
    val isScanEnabled by viewModel.isScanEnabled.collectAsState()
    val isDecoyEnabled by viewModel.isDecoyEnabled.collectAsState()
    val secretPin by viewModel.secretDecoyCode.collectAsState()
    val responderAckCount by viewModel.responderAckCount.collectAsState()
    val latestAlert by viewModel.latestReceivedAlert.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val isSirenOn by viewModel.isSirenOn.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()

    var isSettingsOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "safe_zones" -> onNavigateToSafeZones()
                        "emergency_contacts" -> onNavigateToContacts()
                        "map" -> onNavigateToMap()
                    }
                }
            )
        }
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
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar (Calm, trustworthy identity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Nari-Suraksha",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SafeGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = SafeGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Network gayab, fir bhi help alive.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    // Top utility icons: 56x56dp minimum touch target
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onNavigateToDecoy,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SoftPinkSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Decoy Calculator",
                                tint = PrimaryPink,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SoftPinkSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = PrimaryPink,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(
                            onClick = { isSettingsOpen = true },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SoftPinkSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = PrimaryPink,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Active Incoming Distress Alert Banner
                latestAlert?.let { alert ->
                    AppCard(
                        backgroundColor = EmergencyRed,
                        borderColor = EmergencyRedDark,
                        onClick = {
                            onNavigateToAlert(
                                alert.packet.senderId,
                                alert.packet.lat,
                                alert.packet.lon,
                                alert.rssi
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Alert",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nearby Distress Signal",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to view estimated distance & responder tools",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // System Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusBadge(
                        title = if (isScanEnabled) "BLE Guardian Active" else "BLE Scanner Off",
                        isActive = isScanEnabled,
                        activeColor = SafeGreen
                    )
                    StatusBadge(
                        title = if (isShakeEnabled) "3-Shake Sensor On" else "Shake Disabled",
                        isActive = isShakeEnabled,
                        activeColor = VigilanceAmber
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Stress-Proof Hero SOS Trigger Button (Center of screen, generous padding)
                SosPulseButton(
                    isBroadcasting = isSosActive,
                    onClick = {
                        if (isSosActive) {
                            viewModel.stopSos()
                        } else {
                            viewModel.triggerSos()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Shake indicator prompt
                Text(
                    text = "Tap button or shake phone 3 times in danger",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // Responder Acknowledgment Card
                if (isSosActive) {
                    Spacer(modifier = Modifier.height(20.dp))

                    AppCard(
                        backgroundColor = if (responderAckCount > 0) SafeGreen.copy(alpha = 0.12f) else SoftPinkSurface,
                        borderColor = if (responderAckCount > 0) SafeGreen else BorderDivider
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (responderAckCount > 0) SafeGreen else PrimaryPink),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (responderAckCount > 0) Icons.Default.CheckCircle else Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (responderAckCount > 0) "$responderAckCount Nearby Responder(s) Responded!" else "Broadcasting Peer-to-Peer...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = if (responderAckCount > 0) SafeGreen else PrimaryPink
                                )
                                Text(
                                    text = if (responderAckCount > 0) "A nearby person acknowledged your signal." else "Transmitting continuous distress beacon over BLE radio.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DangerAppButton(
                        text = "I am safe (Stop broadcast)",
                        onClick = { viewModel.stopSos() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Emergency Contacts Alert Quick Section
                    if (emergencyContacts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        AppCard {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Emergency Contacts",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SoftPinkSurface)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CELLULAR BACKUP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = PrimaryPink
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                emergencyContacts.take(2).forEach { contact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = contact.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                ),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${contact.relationship} • ${contact.phoneNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.callContact(contact.phoneNumber) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.height(48.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Call", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Emergency Deterrents (Siren & Flashlight)
                AppCard {
                    Column {
                        Text(
                            text = "Emergency Deterrents",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DeterrentToolButton(
                                title = if (isSirenOn) "Stop Siren" else "Loud Siren",
                                icon = if (isSirenOn) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                isActive = isSirenOn,
                                activeColor = EmergencyRed,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.toggleSiren() }
                            )
                            DeterrentToolButton(
                                title = if (isFlashlightOn) "Torch Off" else "Strobe Torch",
                                icon = if (isFlashlightOn) Icons.Outlined.FlashlightOff else Icons.Default.FlashlightOn,
                                isActive = isFlashlightOn,
                                activeColor = VigilanceAmber,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.toggleFlashlight() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Protection Toggles Card
                AppCard {
                    Column {
                        Text(
                            text = "Offline Protection Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingToggleRow(
                            title = "Shake to Trigger SOS",
                            subtitle = "3 rapid shakes (2.7g) starts 3s countdown",
                            icon = Icons.Default.Vibration,
                            checked = isShakeEnabled,
                            onCheckedChange = { viewModel.setShakeDetectionEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingToggleRow(
                            title = "Background BLE Guardian",
                            subtitle = "Listens for distress alerts even with screen locked",
                            icon = Icons.Default.Sensors,
                            checked = isScanEnabled,
                            onCheckedChange = { viewModel.setGuardianScanEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingToggleRow(
                            title = "Decoy Calculator Mode",
                            subtitle = "Disguise app as calculator with PIN trigger ($secretPin)",
                            icon = Icons.Default.Calculate,
                            checked = isDecoyEnabled,
                            onCheckedChange = { viewModel.setDecoyModeEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Countdown Overlay
            CountdownOverlay(
                isCountingDown = isCountingDown,
                remainingSeconds = countdownSeconds,
                onCancel = { viewModel.cancelCountdown() }
            )

            // Settings Dialog
            if (isSettingsOpen) {
                SettingsDialog(
                    currentPin = secretPin,
                    anonymousDeviceId = viewModel.anonymousDeviceId,
                    onSavePin = { viewModel.updateSecretDecoyCode(it) },
                    onDismiss = { isSettingsOpen = false },
                    onOpenBleDebug = onNavigateToBleDebug
                )
            }
        }
    }
}

@Composable
private fun DeterrentToolButton(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor else SoftPinkSurface,
            contentColor = if (isActive) Color.White else PrimaryPink
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(64.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SoftPinkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPink,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryPink,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD4B8C4)
            )
        )
    }
}
