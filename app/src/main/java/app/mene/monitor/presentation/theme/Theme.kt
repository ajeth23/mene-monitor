package app.mene.monitor.presentation.theme

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class AppThemeMode {
    SYSTEM,   // Follow System Dark/Light + Dynamic Color on Android 12+
    DARK,     // Dark Glass / Cyber Mode
    LIGHT,    // Light Material 3 Mode
    DYNAMIC   // Force Material You Dynamic Color (API 31+)
}

// Static Dark Color Scheme
val RawDarkBackground = Color(0xFF08141F)
val RawDarkSurface = Color(0xFF141C2B)
val RawDarkSurfaceVariant = Color(0xFF1C263A)
val RawDarkSurfaceElevated = Color(0xFF1C263A)
val RawDarkBorder = Color.Transparent
val RawDarkBorderSubtle = Color.Transparent

val RawTextPrimaryDark = Color(0xFFF8FAFC)
val RawTextSecondaryDark = Color(0xFF94A3B8)
val RawTextMutedDark = Color(0xFF64748B)

val RawDarkSurfaceContainer = Color(0xFF141C2B)
val RawDarkSurfaceContainerLow = Color(0xFF101724)
val RawDarkSurfaceContainerHigh = Color(0xFF192336)
val RawDarkSurfaceContainerHighest = Color(0xFF1E2A40)

private val DarkColorScheme = darkColorScheme(
    primary = RawAccentPrimary,
    onPrimary = RawDarkBackground,
    primaryContainer = RawDarkSurfaceVariant,
    onPrimaryContainer = RawAccentPrimary,
    secondary = AccentCpu,
    onSecondary = RawDarkBackground,
    tertiary = AccentGpu,
    onTertiary = RawDarkBackground,
    background = RawDarkBackground,
    onBackground = RawTextPrimaryDark,
    surface = RawDarkSurface,
    surfaceContainer = RawDarkSurfaceContainer,
    surfaceContainerLow = RawDarkSurfaceContainerLow,
    surfaceContainerHigh = RawDarkSurfaceContainerHigh,
    surfaceContainerHighest = RawDarkSurfaceContainerHighest,
    onSurface = RawTextPrimaryDark,
    surfaceVariant = RawDarkSurfaceVariant,
    onSurfaceVariant = RawTextSecondaryDark,
    outline = RawTextMutedDark,
    outlineVariant = RawDarkBorderSubtle
)

// Static Light Color Scheme (Soft Slate Canvas + Crisp White Cards + Slate Container, Borderless)
val RawLightBackground = Color(0xFFF8FAFC)
val RawLightSurface = Color(0xFFFFFFFF)
val RawLightSurfaceContainer = Color(0xFFF1F5F9)
val RawLightSurfaceContainerLow = Color(0xFFF8FAFC)
val RawLightSurfaceContainerHigh = Color(0xFFE2E8F0)
val RawLightSurfaceContainerHighest = Color(0xFFCBD5E1)
val RawLightSurfaceVariant = Color(0xFFE2E8F0)
val RawLightSurfaceElevated = Color(0xFFFFFFFF)
val RawLightBorder = Color.Transparent
val RawLightBorderSubtle = Color.Transparent

val RawTextPrimaryLight = Color(0xFF0F172A)
val RawTextSecondaryLight = Color(0xFF475569)
val RawTextMutedLight = Color(0xFF64748B)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0077A3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF001E2B),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    tertiary = Color(0xFF9333EA),
    onTertiary = Color.White,
    background = RawLightBackground,
    onBackground = RawTextPrimaryLight,
    surface = RawLightSurface,
    surfaceContainer = RawLightSurfaceContainer,
    surfaceContainerLow = RawLightSurfaceContainerLow,
    surfaceContainerHigh = RawLightSurfaceContainerHigh,
    surfaceContainerHighest = RawLightSurfaceContainerHighest,
    onSurface = RawTextPrimaryLight,
    surfaceVariant = RawLightSurfaceVariant,
    onSurfaceVariant = RawTextSecondaryLight,
    outline = RawTextMutedLight,
    outlineVariant = RawLightBorder
)

val MeneShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MeneMonitorTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.DYNAMIC -> systemInDark
    }

    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val useDynamicColor = (themeMode == AppThemeMode.SYSTEM || themeMode == AppThemeMode.DYNAMIC) && supportsDynamicColor

    val colorScheme = when {
        useDynamicColor -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: (view.context as? ContextWrapper)?.baseContext as? Activity
            activity?.window?.let { window ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                    window.isStatusBarContrastEnforced = false
                }
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark

                // Match system navigation bar to bottom navigation container surface
                val navBarColor = if (isDark) colorScheme.background else colorScheme.surfaceContainer
                window.navigationBarColor = navBarColor.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MeneShapes,
        typography = MaterialTheme.typography,
        content = content
    )
}
