package com.brokencoders.narisuraksha.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.ui.components.AppCard
import com.brokencoders.narisuraksha.ui.components.PrimaryAppButton
import com.brokencoders.narisuraksha.ui.components.SecondaryAppButton
import com.brokencoders.narisuraksha.ui.theme.BorderDivider
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber

@SuppressLint("BatteryLife")
@Composable
fun OnboardingScreen(
    onComplete: (customPin: String) -> Unit
) {
    val context = LocalContext.current
    var hasAllPermissions by remember { mutableStateOf(PermissionHelper.hasAllPermissions(context)) }
    var isBatteryOptIgnored by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }

    var userCustomPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAllPermissions = PermissionHelper.hasAllPermissions(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Shield Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SoftPinkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = PrimaryPink,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nari-Suraksha",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Autonomous Offline Safety & Decoy Guardian",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryPink
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Protects you even without cellular or internet connectivity by broadcasting emergency distress signals peer-to-peer over Bluetooth Low Energy.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mandatory Custom PIN Setup Card
            AppCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SoftPinkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = PrimaryPink,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Set Secret Decoy PIN (Required)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "4-digit code to trigger SOS inside the disguised calculator",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userCustomPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                userCustomPin = it
                                pinError = null
                            }
                        },
                        label = { Text("Enter 4-Digit Secret PIN") },
                        placeholder = { Text("e.g. 5821") },
                        isError = pinError != null,
                        supportingText = {
                            if (pinError != null) {
                                Text(text = pinError!!, color = EmergencyRed)
                            } else {
                                Text(text = "Do not use default 1234 for your safety", color = TextSecondary)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = PrimaryPink,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions Checklist
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PermissionItem(
                    title = "Bluetooth Low Energy (Nearby)",
                    subtitle = "Broadcast & receive offline beacons from nearby phones.",
                    icon = Icons.Default.Bluetooth,
                    iconTint = PrimaryPink,
                    isGranted = PermissionHelper.hasBluetoothPermissions(context)
                )

                PermissionItem(
                    title = "GPS / Device Location",
                    subtitle = "Provides exact GPS coordinates in SOS packet without internet.",
                    icon = Icons.Default.MyLocation,
                    iconTint = PrimaryPink,
                    isGranted = PermissionHelper.hasLocationPermission(context)
                )

                PermissionItem(
                    title = "Audio Recording",
                    subtitle = "Records local audio evidence securely during confirmed SOS.",
                    icon = Icons.Default.Mic,
                    iconTint = SafeGreen,
                    isGranted = PermissionHelper.hasAudioPermission(context)
                )

                PermissionItem(
                    title = "High Priority Notifications",
                    subtitle = "Alerts with sound and vibration if someone triggers SOS nearby.",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = VigilanceAmber,
                    isGranted = hasAllPermissions
                )

                PermissionItem(
                    title = "Battery Optimization Exemption",
                    subtitle = "Allows background scanner to listen when screen is off.",
                    icon = Icons.Default.BatteryAlert,
                    iconTint = PrimaryPink,
                    isGranted = isBatteryOptIgnored
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            if (!hasAllPermissions) {
                PrimaryAppButton(
                    text = "Grant Permissions",
                    onClick = {
                        val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
                        permissionLauncher.launch(permissions)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (!isBatteryOptIgnored) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                // fallback
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text(
                        text = "Disable Battery Optimization",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryAppButton(
                text = "Initialize Guardian",
                enabled = hasAllPermissions && userCustomPin.length == 4,
                onClick = {
                    if (userCustomPin.length != 4) {
                        pinError = "Please enter a 4-digit PIN for Decoy Mode"
                        Toast.makeText(context, "Please set a 4-digit Decoy PIN", Toast.LENGTH_SHORT).show()
                        return@PrimaryAppButton
                    }
                    onComplete(userCustomPin)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isGranted: Boolean
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftPinkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = SafeGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
