package app.mene.monitor.presentation.more

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.theme.AccentBattery
import app.mene.monitor.presentation.theme.AccentNetwork
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.AccentSensors
import app.mene.monitor.presentation.theme.AccentStorage
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun MoreScreen(
    onNavigateStorage: () -> Unit,
    onNavigateBattery: () -> Unit,
    onNavigateSensors: () -> Unit,
    onNavigateNetwork: () -> Unit,
    onNavigateSystemInfo: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HardwareHeader(title = "Hardware & Utilities")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MoreNavigationItem(
                title = "Storage & Disks",
                subtitle = "UFS storage partitions, free space & wear",
                icon = Icons.Default.Storage,
                iconColor = AccentStorage,
                testTag = "more_item_storage",
                onClick = onNavigateStorage
            )

            MoreNavigationItem(
                title = "Battery & Power",
                subtitle = "Charge level, health, voltage, current & rate",
                icon = Icons.Default.BatteryChargingFull,
                iconColor = AccentBattery,
                testTag = "more_item_battery",
                onClick = onNavigateBattery
            )

            MoreNavigationItem(
                title = "Sensors & Cooling",
                subtitle = "Thermal zones, fans, accelerometer & environment",
                icon = Icons.Default.Sensors,
                iconColor = AccentSensors,
                testTag = "more_item_sensors",
                onClick = onNavigateSensors
            )

            MoreNavigationItem(
                title = "Network & Connectivity",
                subtitle = "Wi-Fi link, carrier throughput & local IP",
                icon = Icons.Default.Wifi,
                iconColor = AccentNetwork,
                testTag = "more_item_network",
                onClick = onNavigateNetwork
            )

            MoreNavigationItem(
                title = "System Specifications",
                subtitle = "Android version, kernel, display, board & ABIs",
                icon = Icons.Default.PhoneAndroid,
                iconColor = AccentPrimary,
                testTag = "more_item_system",
                onClick = onNavigateSystemInfo
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Preferences",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )

            MoreNavigationItem(
                title = "Settings",
                subtitle = "Refresh rate, temperature units & display",
                icon = Icons.Default.Settings,
                iconColor = TextSecondary,
                testTag = "more_item_settings",
                onClick = onNavigateSettings
            )

            MoreNavigationItem(
                title = "About Mene Monitor",
                subtitle = "Version 1.0.0 • Architecture & Hardware Diagnostics",
                icon = Icons.Default.Info,
                iconColor = TextSecondary,
                testTag = "more_item_about",
                onClick = onNavigateAbout
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoreNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
