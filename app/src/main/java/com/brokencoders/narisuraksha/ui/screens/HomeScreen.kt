package com.brokencoders.narisuraksha.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.R
import com.brokencoders.narisuraksha.data.EmergencyContactEntity
import com.brokencoders.narisuraksha.ui.components.AppBottomBar
import com.brokencoders.narisuraksha.ui.components.CountdownOverlay
import com.brokencoders.narisuraksha.ui.components.InfoCard
import com.brokencoders.narisuraksha.ui.components.PrimaryButton
import com.brokencoders.narisuraksha.ui.components.SecondaryButton
import com.brokencoders.narisuraksha.ui.components.SettingsDialog
import com.brokencoders.narisuraksha.ui.components.SosButton
import com.brokencoders.narisuraksha.ui.components.StatusChip
import com.brokencoders.narisuraksha.ui.theme.BorderDivider
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedDark
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.WarningAmber
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

/**
 * HomeScreen - Core hub of Nari-Suraksha.
 *
 * Implements the Apple/Google level design craft:
 * 1. One obvious primary action: centered 220dp solid pink SOS button ("Hold for help").
 * 2. StatusChips row: Bluetooth, Location, Mesh relay (On/Off).
 * 3. Responder counter card: "0 people responding", turns Safe Green with check icon when ACKs arrive.
 * 4. Secondary button: "Call primary contact" (2dp pink border, 64dp).
 * 5. Active SOS state: "Help is being alerted. Stay calm.", broadcasting indicator (Emergency Red used ONLY here),
 *    mesh explanation line, and large 64dp "I'm safe" button.
 */
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
    LaunchedEffect(Unit) {
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
    val primaryContact by viewModel.primaryContact.collectAsState()

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
                // Calm, trustworthy Header Bar
                HomeHeader(
                    onNavigateToDecoy = onNavigateToDecoy,
                    onNavigateToHistory = onNavigateToHistory,
                    onOpenSettings = { isSettingsOpen = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Incoming Distress Alert Banner (if peer detected)
                latestAlert?.let { alert ->
                    IncomingDistressBanner(
                        onClick = {
                            onNavigateToAlert(
                                alert.packet.senderId,
                                alert.packet.lat,
                                alert.packet.lon,
                                alert.rssi
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // If SOS is active, present the dedicated Active SOS Experience
                if (isSosActive) {
                    ActiveSosView(
                        responderAckCount = responderAckCount,
                        primaryContact = primaryContact,
                        onStopSos = { viewModel.stopSos() },
                        onCallPrimaryContact = {
                            primaryContact?.let { contact ->
                                viewModel.callContact(contact.phoneNumber)
                            }
                        },
                        isSirenOn = isSirenOn,
                        isFlashlightOn = isFlashlightOn,
                        onToggleSiren = { viewModel.toggleSiren() },
                        onToggleFlashlight = { viewModel.toggleFlashlight() }
                    )
                } else {
                    // Standby Calm Home Experience
                    StandbyHomeView(
                        isScanEnabled = isScanEnabled,
                        responderAckCount = responderAckCount,
                        primaryContact = primaryContact,
                        onTriggerSos = { viewModel.triggerSos() },
                        onCallPrimaryContact = {
                            if (primaryContact != null) {
                                viewModel.callContact(primaryContact!!.phoneNumber)
                            } else {
                                onNavigateToContacts()
                            }
                        },
                        isSirenOn = isSirenOn,
                        isFlashlightOn = isFlashlightOn,
                        onToggleSiren = { viewModel.toggleSiren() },
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        isShakeEnabled = isShakeEnabled,
                        isDecoyEnabled = isDecoyEnabled,
                        secretPin = secretPin,
                        onToggleShake = { viewModel.setShakeDetectionEnabled(it) },
                        onToggleScan = { viewModel.setGuardianScanEnabled(it) },
                        onToggleDecoy = { viewModel.setDecoyModeEnabled(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Full-screen Countdown Overlay (96sp number, haptic tick, 80dp cancel button)
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

/**
 * Top App Header with identity and 56x56dp utility icon buttons.
 */
@Composable
private fun HomeHeader(
    onNavigateToDecoy: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SafeGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.offline_badge),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = SafeGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // 56x56dp minimum touch targets for utility controls
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
                    contentDescription = stringResource(R.string.decoy_calculator_title),
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
                onClick = onOpenSettings,
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
}

/**
 * Incoming distress alert banner from a nearby peer.
 */
@Composable
private fun IncomingDistressBanner(
    onClick: () -> Unit
) {
    InfoCard(
        backgroundColor = EmergencyRed,
        borderColor = EmergencyRedDark,
        onClick = onClick
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
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nearby_distress_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.nearby_distress_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Normal Standby View:
 * 1. Centered 220dp SOS button ("Hold for help", solid pink, white shield icon, slow pulse ring).
 * 2. StatusChips row: Bluetooth, Location, Mesh relay (On/Off).
 * 3. Responder counter card: "0 people responding", turns green when ACKs arrive.
 * 4. Secondary button: "Call primary contact".
 */
@Composable
private fun StandbyHomeView(
    isScanEnabled: Boolean,
    responderAckCount: Int,
    primaryContact: EmergencyContactEntity?,
    onTriggerSos: () -> Unit,
    onCallPrimaryContact: () -> Unit,
    isSirenOn: Boolean,
    isFlashlightOn: Boolean,
    onToggleSiren: () -> Unit,
    onToggleFlashlight: () -> Unit,
    isShakeEnabled: Boolean,
    isDecoyEnabled: Boolean,
    secretPin: String,
    onToggleShake: (Boolean) -> Unit,
    onToggleScan: (Boolean) -> Unit,
    onToggleDecoy: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Centered 220dp solid pink SOS button with white shield icon & slow pulse ring
        SosButton(
            isBroadcasting = false,
            onClick = onTriggerSos,
            label = stringResource(R.string.hold_for_help)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Row of StatusChips: Bluetooth, Location, Mesh relay (On/Off)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            StatusChip(
                label = stringResource(R.string.bluetooth_status),
                icon = Icons.Default.Bluetooth,
                statusColor = SafeGreen
            )
            StatusChip(
                label = stringResource(R.string.location_status),
                icon = Icons.Default.LocationOn,
                statusColor = SafeGreen
            )
            StatusChip(
                label = if (isScanEnabled) stringResource(R.string.mesh_relay_on) else stringResource(R.string.mesh_relay_off),
                icon = Icons.Default.Sensors,
                statusColor = if (isScanEnabled) SafeGreen else WarningAmber
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Responder Counter Card: "0 people responding", turns green when ACKs arrive
        InfoCard(
            backgroundColor = if (responderAckCount > 0) SafeGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
            borderColor = if (responderAckCount > 0) SafeGreen else MaterialTheme.colorScheme.outline
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (responderAckCount > 0) SafeGreen else SoftPinkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (responderAckCount > 0) Icons.Default.CheckCircle else Icons.Default.People,
                        contentDescription = null,
                        tint = if (responderAckCount > 0) Color.White else PrimaryPink,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (responderAckCount > 0) {
                            if (responderAckCount == 1) stringResource(R.string.single_responder)
                            else stringResource(R.string.multiple_responders, responderAckCount)
                        } else {
                            stringResource(R.string.zero_responders)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (responderAckCount > 0) SafeGreen else TextPrimary
                    )
                    Text(
                        text = if (responderAckCount > 0) {
                            stringResource(R.string.responder_acknowledged)
                        } else {
                            "Mesh standby • Peer signals monitored"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. "Call primary contact" as a secondary button (64dp tall, 2dp pink border, surface fill)
        SecondaryButton(
            text = if (primaryContact != null) {
                stringResource(R.string.call_contact, primaryContact.name)
            } else {
                stringResource(R.string.set_primary_contact)
            },
            icon = Icons.Default.Call,
            onClick = onCallPrimaryContact,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Deterrents Card (Loud Siren, Strobe Torch)
        InfoCard {
            Column {
                Text(
                    text = stringResource(R.string.emergency_deterrents),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DeterrentToolButton(
                        title = if (isSirenOn) stringResource(R.string.stop_siren) else stringResource(R.string.loud_siren),
                        icon = if (isSirenOn) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        isActive = isSirenOn,
                        activeColor = EmergencyRed,
                        modifier = Modifier.weight(1f),
                        onClick = onToggleSiren
                    )
                    DeterrentToolButton(
                        title = if (isFlashlightOn) stringResource(R.string.torch_off) else stringResource(R.string.strobe_torch),
                        icon = if (isFlashlightOn) Icons.Outlined.FlashlightOff else Icons.Default.FlashlightOn,
                        isActive = isFlashlightOn,
                        activeColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onToggleFlashlight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Offline Protection Settings Card
        InfoCard {
            Column {
                Text(
                    text = stringResource(R.string.protection_settings),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingToggleRow(
                    title = stringResource(R.string.shake_detection_title),
                    subtitle = stringResource(R.string.shake_detection_subtitle),
                    icon = Icons.Default.Vibration,
                    checked = isShakeEnabled,
                    onCheckedChange = onToggleShake
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingToggleRow(
                    title = stringResource(R.string.guardian_scan_title),
                    subtitle = stringResource(R.string.guardian_scan_subtitle),
                    icon = Icons.Default.Sensors,
                    checked = isScanEnabled,
                    onCheckedChange = onToggleScan
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingToggleRow(
                    title = stringResource(R.string.decoy_calculator_title),
                    subtitle = "PIN: $secretPin • ${stringResource(R.string.decoy_calculator_subtitle)}",
                    icon = Icons.Default.Calculate,
                    checked = isDecoyEnabled,
                    onCheckedChange = onToggleDecoy
                )
            }
        }
    }
}

/**
 * 4. Active SOS Screen / View:
 * Calm confirmation: "Help is being alerted. Stay calm."
 * Broadcasting indicator (Emergency Red used ONLY here),
 * responder count, and clear mesh explanation line:
 * "Nearby phones are passing your alert along."
 * Large 64dp "I'm safe" button.
 */
@Composable
private fun ActiveSosView(
    responderAckCount: Int,
    primaryContact: EmergencyContactEntity?,
    onStopSos: () -> Unit,
    onCallPrimaryContact: () -> Unit,
    isSirenOn: Boolean,
    isFlashlightOn: Boolean,
    onToggleSiren: () -> Unit,
    onToggleFlashlight: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Broadcasting indicator: Emergency Red used ONLY for the live-broadcast indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(EmergencyRed.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    text = stringResource(R.string.broadcasting_sos),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = EmergencyRed
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Calm confirmation: "Help is being alerted. Stay calm."
        Text(
            text = stringResource(R.string.help_alerted_stay_calm),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Clear mesh explanation line: "Nearby phones are passing your alert along."
        Text(
            text = stringResource(R.string.nearby_phones_passing_alert),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Responder Counter Card (turns green with check icon when ACKs arrive)
        InfoCard(
            backgroundColor = if (responderAckCount > 0) SafeGreen.copy(alpha = 0.12f) else SoftPinkSurface,
            borderColor = if (responderAckCount > 0) SafeGreen else BorderDivider
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (responderAckCount > 0) SafeGreen else PrimaryPink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (responderAckCount > 0) Icons.Default.CheckCircle else Icons.Default.People,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (responderAckCount > 0) {
                            if (responderAckCount == 1) stringResource(R.string.single_responder)
                            else stringResource(R.string.multiple_responders, responderAckCount)
                        } else {
                            stringResource(R.string.zero_responders)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (responderAckCount > 0) SafeGreen else PrimaryPink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (responderAckCount > 0) {
                            stringResource(R.string.responder_acknowledged)
                        } else {
                            stringResource(R.string.broadcasting_mesh_desc)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large 64dp "I'm safe" button (Solid Pink primary button, stopping the SOS)
        PrimaryButton(
            text = stringResource(R.string.im_safe),
            icon = Icons.Default.CheckCircle,
            onClick = onStopSos,
            modifier = Modifier.fillMaxWidth()
        )

        // Cellular backup option if primary contact exists
        if (primaryContact != null) {
            Spacer(modifier = Modifier.height(14.dp))
            SecondaryButton(
                text = stringResource(R.string.call_contact, primaryContact.name),
                icon = Icons.Default.Call,
                onClick = onCallPrimaryContact,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick deterrent tools directly available during active emergency
        InfoCard {
            Column {
                Text(
                    text = stringResource(R.string.emergency_deterrents),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DeterrentToolButton(
                        title = if (isSirenOn) stringResource(R.string.stop_siren) else stringResource(R.string.loud_siren),
                        icon = if (isSirenOn) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        isActive = isSirenOn,
                        activeColor = EmergencyRed,
                        modifier = Modifier.weight(1f),
                        onClick = onToggleSiren
                    )
                    DeterrentToolButton(
                        title = if (isFlashlightOn) stringResource(R.string.torch_off) else stringResource(R.string.strobe_torch),
                        icon = if (isFlashlightOn) Icons.Outlined.FlashlightOff else Icons.Default.FlashlightOn,
                        isActive = isFlashlightOn,
                        activeColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onToggleFlashlight
                    )
                }
            }
        }
    }
}

/**
 * Clean Deterrent Tool Button for Siren and Torch.
 */
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

/**
 * Toggle Row for settings items with clear icon, title, and accessible switch.
 */
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
                uncheckedTrackColor = BorderDivider
            )
        )
    }
}
