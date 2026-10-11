package com.brokencoders.narisuraksha.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Nari-Suraksha Design System Color Tokens
// Flat & Solid colors only - ZERO gradients
// ==========================================

// --- Light Mode Core Palette ---
val PrimaryPink = Color(0xFFC2185B)
val PrimaryPinkPressed = Color(0xFFA0144A)
val SoftPinkSurface = Color(0xFFFCE4EC)
val AppBackground = Color(0xFFFFF7FA)
val CardBackground = Color(0xFFFFFFFF)
val BorderDivider = Color(0xFFF2C9D8)

// --- Typography Tokens ---
val TextPrimary = Color(0xFF2B1B22)
val TextSecondary = Color(0xFF6B5560)
val TextMuted = Color(0xFF8C737E)

// --- Semantic Role Tokens ---
// Emergency Red is STRICTLY for active distress / danger states
val EmergencyRed = Color(0xFFD32F2F)
val EmergencyRedDark = Color(0xFFB71C1C)
val EmergencyRedSurface = Color(0xFFFFEBEE)

// Safe Green is for confirmations, ACKs, and healthy mesh states
val SafeGreen = Color(0xFF2E7D32)
val SafeGreenSurface = Color(0xFFE8F5E9)

// Guardian Blue is for information, responder roles, and relays
val GuardianBlue = Color(0xFF1565C0)
val GuardianBlueSurface = Color(0xFFE3F2FD)

// Amber is for warnings, cooldowns, and low budgets
val WarningAmber = Color(0xFFB26A00)
val WarningAmberSurface = Color(0xFFFFF3E0)

// Backward compatibility alias
val VigilanceAmber = WarningAmber

// --- Dark Mode Palette (Deep Plum Identity) ---
val DarkBackground = Color(0xFF1E1218)
val DarkSurface = Color(0xFF2A1A22)
val DarkSoftPinkSurface = Color(0xFF3D2633)
val DarkBorderDivider = Color(0xFF4A2E3D)
val DarkPrimaryPink = Color(0xFFF06292)
val DarkTextPrimary = Color(0xFFFFF7FA)
val DarkTextSecondary = Color(0xFFD4B8C4)
val DarkTextMuted = Color(0xFFA68D99)

val DarkEmergencyRed = Color(0xFFEF5350)
val DarkEmergencyRedSurface = Color(0xFF3B1E22)
val DarkSafeGreen = Color(0xFF66BB6A)
val DarkSafeGreenSurface = Color(0xFF1B3320)
val DarkGuardianBlue = Color(0xFF64B5F6)
val DarkGuardianBlueSurface = Color(0xFF1A2E40)
val DarkWarningAmber = Color(0xFFFFB74D)
val DarkWarningAmberSurface = Color(0xFF3D2C1A)
