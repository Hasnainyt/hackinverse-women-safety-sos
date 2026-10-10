package com.brokencoders.narisuraksha.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.data.SafeZoneCategory
import com.brokencoders.narisuraksha.data.SafeZoneItem
import com.brokencoders.narisuraksha.data.SafeZonesRepository
import com.brokencoders.narisuraksha.ui.components.AppBottomBar
import com.brokencoders.narisuraksha.ui.components.AppCard
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
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentCoords by viewModel.currentCoordinates.collectAsState()
    val latestAlert by viewModel.latestReceivedAlert.collectAsState()

    var selectedFilter by remember { mutableStateOf<SafeZoneCategory?>(null) }
    var selectedSafeZone by remember { mutableStateOf<SafeZoneItem?>(null) }

    val userLat = currentCoords?.lat ?: 28.6139
    val userLon = currentCoords?.lon ?: 77.2090
    val isGpsActive = currentCoords != null

    val safeZones = remember(currentCoords) {
        SafeZonesRepository.preCachedSafeZones.sortedBy { zone ->
            SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
        }
    }

    val filteredZones = remember(safeZones, selectedFilter) {
        if (selectedFilter == null) safeZones else safeZones.filter { it.category == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SoftPinkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tactical Radar & Map",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshLocation() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = PrimaryPink, modifier = Modifier.size(26.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = "map",
                onNavigate = { route ->
                    if (route != "map") onBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Telemetry Header Card
                LocationTelemetryCard(
                    coords = currentCoords,
                    isGpsActive = isGpsActive,
                    onOpenExternalMap = {
                        openExternalMaps(context, userLat, userLon)
                    }
                )
            }

            // Tactical Proximity Radar Canvas Card
            item {
                AppCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Proximity Radar (5 km)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SafeGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "100% OFFLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = SafeGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Radar View
                        TacticalRadarView(
                            userLat = userLat,
                            userLon = userLon,
                            safeZones = safeZones,
                            selectedZone = selectedSafeZone,
                            activeDistressAlert = latestAlert,
                            onSelectZone = { selectedSafeZone = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Radar Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(PrimaryPink, "You")
                            LegendItem(SafeGreen, "Hospital")
                            LegendItem(GuardianBlue, "Police")
                            LegendItem(PrimaryPink, "Women Desk")
                            if (latestAlert != null) {
                                LegendItem(EmergencyRed, "SOS Alert")
                            }
                        }
                    }
                }
            }

            // Selected Safe Zone Highlight (if tapped)
            selectedSafeZone?.let { zone ->
                item {
                    val distKm = SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
                    AppCard(
                        backgroundColor = SoftPinkSurface,
                        borderColor = PrimaryPink
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected Safe Haven",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryPink
                                )
                                Text(
                                    text = SafeZonesRepository.formatDistance(distKm),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = SafeGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = zone.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = zone.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { dialPhone(context, zone.emergencyContact) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Call ${zone.emergencyContact}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                                SecondaryAppButton(
                                    text = "Directions",
                                    icon = Icons.Default.Directions,
                                    onClick = { openDirections(context, zone.lat, zone.lon, zone.name) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${safeZones.size})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryPink,
                            selectedLabelColor = Color.White,
                            containerColor = SoftPinkSurface,
                            labelColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(44.dp)
                    )
                    SafeZoneCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedFilter == cat,
                            onClick = { selectedFilter = if (selectedFilter == cat) null else cat },
                            label = { Text(cat.label.take(14), fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPink,
                                selectedLabelColor = Color.White,
                                containerColor = SoftPinkSurface,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }
            }

            // Safe Zones Directory List
            items(filteredZones, key = { it.id }) { zone ->
                val distKm = SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
                SafeZoneCard(
                    zone = zone,
                    distanceText = SafeZonesRepository.formatDistance(distKm),
                    isSelected = selectedSafeZone?.id == zone.id,
                    onSelect = { selectedSafeZone = zone },
                    onCall = { dialPhone(context, zone.emergencyContact) },
                    onDirections = { openDirections(context, zone.lat, zone.lon, zone.name) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LocationTelemetryCard(
    coords: com.brokencoders.narisuraksha.capture.Coordinates?,
    isGpsActive: Boolean,
    onOpenExternalMap: () -> Unit
) {
    AppCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isGpsActive) SafeGreen else VigilanceAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGpsActive) "Live GPS Telemetry" else "Offline Baseline Location",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isGpsActive) SafeGreen else VigilanceAmber
                    )
                }

                Button(
                    onClick = onOpenExternalMap,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Maps", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TelemetryParam("LATITUDE", coords?.let { String.format(Locale.US, "%.5f", it.lat) } ?: "28.61393")
                TelemetryParam("LONGITUDE", coords?.let { String.format(Locale.US, "%.5f", it.lon) } ?: "77.20902")
                TelemetryParam("ACCURACY", coords?.accuracy?.let { "±${it.toInt()} m" } ?: "±12 m")
                TelemetryParam(
                    "UPDATED",
                    coords?.timestamp?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Cached"
                )
            }
        }
    }
}

@Composable
private fun TelemetryParam(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            ),
            color = TextPrimary
        )
    }
}

@Composable
private fun TacticalRadarView(
    userLat: Double,
    userLon: Double,
    safeZones: List<SafeZoneItem>,
    selectedZone: SafeZoneItem?,
    activeDistressAlert: com.brokencoders.narisuraksha.ble.ReceivedSos?,
    onSelectZone: (SafeZoneItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SoftPinkSurface)
            .border(1.5.dp, BorderDivider, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.height.coerceAtMost(size.width) / 2f - 24.dp.toPx()

            // Concentric range rings: 1.25km, 2.5km, 3.75km, 5km
            val ringIntervals = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            val strokeDashed = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            ringIntervals.forEach { fraction ->
                drawCircle(
                    color = PrimaryPink.copy(alpha = 0.25f),
                    radius = maxRadius * fraction,
                    center = center,
                    style = strokeDashed
                )
            }

            // Crosshairs
            drawLine(PrimaryPink.copy(alpha = 0.15f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius), strokeWidth = 1.dp.toPx())
            drawLine(PrimaryPink.copy(alpha = 0.15f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y), strokeWidth = 1.dp.toPx())

            // Plot Safe Zones (max distance cap 5.0 km)
            safeZones.forEach { zone ->
                val distKm = SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
                val bearing = SafeZonesRepository.calculateBearing(userLat, userLon, zone.lat, zone.lon)
                val clampedDist = distKm.coerceAtMost(5.0)
                val radiusPx = (clampedDist / 5.0) * maxRadius

                val angleRad = Math.toRadians(bearing.toDouble() - 90.0)
                val targetX = center.x + (radiusPx * cos(angleRad)).toFloat()
                val targetY = center.y + (radiusPx * sin(angleRad)).toFloat()

                val pinColor = when (zone.category) {
                    SafeZoneCategory.POLICE -> GuardianBlue
                    SafeZoneCategory.HOSPITAL -> SafeGreen
                    SafeZoneCategory.WOMEN_HELP_DESK -> PrimaryPink
                    SafeZoneCategory.METRO_SECURITY -> VigilanceAmber
                }

                val isSelected = selectedZone?.id == zone.id

                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 9.dp.toPx() else 6.dp.toPx(),
                    center = Offset(targetX, targetY)
                )
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 12.dp.toPx(),
                        center = Offset(targetX, targetY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Active distress alert pin (if alert has valid GPS)
            activeDistressAlert?.let { alert ->
                if (alert.packet.lat != 0f && alert.packet.lon != 0f) {
                    val alertDist = SafeZonesRepository.calculateDistanceKm(userLat, userLon, alert.packet.lat.toDouble(), alert.packet.lon.toDouble())
                    val alertBearing = SafeZonesRepository.calculateBearing(userLat, userLon, alert.packet.lat.toDouble(), alert.packet.lon.toDouble())
                    val radiusPx = (alertDist.coerceAtMost(5.0) / 5.0) * maxRadius
                    val angleRad = Math.toRadians(alertBearing.toDouble() - 90.0)
                    val targetX = center.x + (radiusPx * cos(angleRad)).toFloat()
                    val targetY = center.y + (radiusPx * sin(angleRad)).toFloat()

                    drawCircle(color = EmergencyRed, radius = 10.dp.toPx(), center = Offset(targetX, targetY))
                    drawCircle(color = Color.White, radius = 14.dp.toPx(), center = Offset(targetX, targetY), style = Stroke(width = 2.dp.toPx()))
                }
            }

            // Center User Dot
            drawCircle(color = PrimaryPink, radius = 8.dp.toPx(), center = center)
            drawCircle(color = Color.White, radius = 11.dp.toPx(), center = center, style = Stroke(width = 2.5.dp.toPx()))
        }

        // Cardinal directions labels
        Text("N", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryPink), modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
        Text("S", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp))
        Text("E", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
        Text("W", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary), modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
    }
}

@Composable
private fun SafeZoneCard(
    zone: SafeZoneItem,
    distanceText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    AppCard(
        backgroundColor = if (isSelected) SoftPinkSurface else MaterialTheme.colorScheme.surface,
        borderColor = if (isSelected) PrimaryPink else BorderDivider,
        onClick = onSelect
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when (zone.category) {
                                SafeZoneCategory.POLICE -> GuardianBlue.copy(alpha = 0.15f)
                                SafeZoneCategory.HOSPITAL -> SafeGreen.copy(alpha = 0.15f)
                                SafeZoneCategory.WOMEN_HELP_DESK -> PrimaryPink.copy(alpha = 0.15f)
                                SafeZoneCategory.METRO_SECURITY -> VigilanceAmber.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (zone.category) {
                            SafeZoneCategory.POLICE -> Icons.Default.LocalPolice
                            SafeZoneCategory.HOSPITAL -> Icons.Default.LocalHospital
                            SafeZoneCategory.WOMEN_HELP_DESK -> Icons.Default.Security
                            SafeZoneCategory.METRO_SECURITY -> Icons.Default.Train
                        },
                        contentDescription = null,
                        tint = when (zone.category) {
                            SafeZoneCategory.POLICE -> GuardianBlue
                            SafeZoneCategory.HOSPITAL -> SafeGreen
                            SafeZoneCategory.WOMEN_HELP_DESK -> PrimaryPink
                            SafeZoneCategory.METRO_SECURITY -> VigilanceAmber
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = zone.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = SafeGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call ${zone.emergencyContact}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                SecondaryAppButton(
                    text = "Map",
                    icon = Icons.Default.Directions,
                    onClick = onDirections,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = TextSecondary)
    }
}

private fun dialPhone(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No map application installed.", Toast.LENGTH_SHORT).show()
    }
}

private fun openExternalMaps(context: Context, lat: Double, lon: Double) {
    try {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(My+Location)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No map application installed.", Toast.LENGTH_SHORT).show()
    }
}
