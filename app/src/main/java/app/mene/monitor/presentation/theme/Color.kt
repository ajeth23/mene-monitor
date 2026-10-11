package app.mene.monitor.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Adaptive Background and Surface Properties
val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

// Adaptive Clean Card Surface (Flat, Minimal, No Elevation)
val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val bg = MaterialTheme.colorScheme.background
        val isDark = bg.luminance() < 0.5f

        return if (isDark) {
            Color.White.copy(alpha = 0.02f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    }

val DarkSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return if (isDark) {
            val variant = MaterialTheme.colorScheme.surfaceContainerHigh
            val bg = MaterialTheme.colorScheme.background
            if (variant != bg && variant != Color.Unspecified) {
                variant
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
            }
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    }

val DarkSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceContainerHighest

// Borderless, Flat Design: No strokes on cards
val DarkBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = Color.Transparent

val DarkBorderSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = Color.Transparent

// Adaptive Text Colors
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

// Primary Accent Color (Matches app icon #01F8EF in Dark, Accessible High-Contrast in Light/Dynamic)
val RawAccentPrimary = Color(0xFF01F8EF)

val AccentPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return if (isDark) RawAccentPrimary else MaterialTheme.colorScheme.primary
    }

// Feature Accent Colors
val AccentCpu = Color(0xFF3B82F6)
val AccentGpu = Color(0xFFA855F7)
val AccentMemory = Color(0xFF10B981)
val AccentStorage = Color(0xFFF97316)
val AccentBattery = Color(0xFF14B8A6)
val AccentSensors = Color(0xFF8B5CF6)
val AccentNetwork = Color(0xFF06B6D4)
val AccentWarning = Color(0xFFF59E0B)
val AccentError = Color(0xFFEF4444)

// Card Container Background (Unified Single Minimal Surface)
val CpuCardBg: Color @Composable @ReadOnlyComposable get() = DarkSurface
val GpuCardBg: Color @Composable @ReadOnlyComposable get() = DarkSurface
val MemoryCardBg: Color @Composable @ReadOnlyComposable get() = DarkSurface
val StorageCardBg: Color @Composable @ReadOnlyComposable get() = DarkSurface
val BatteryCardBg: Color @Composable @ReadOnlyComposable get() = DarkSurface
val NetworkCardBg: Color @Composable @ReadOnlyComposable get() = DarkSurface
