package app.mene.monitor.presentation.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.AppThemeMode
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HardwareHeader(
            title = "Settings",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Data Polling & Monitoring",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Refresh Interval Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Sensor Refresh Interval",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Controls polling frequency for CPU, GPU, and thermal sensors.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1L to "1 sec (Fast)", 2L to "2 sec (Balanced)", 5L to "5 sec (Eco)").forEach { (sec, label) ->
                        val selected = uiState.refreshIntervalSeconds == sec
                        val bg = if (selected) AccentPrimary else Color(0xFF1B2438)
                        val textCol = if (selected) DarkBackground else TextSecondary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { onAction(SettingsAction.SetRefreshInterval(sec)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = textCol,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Appearance & Theme (Material 3)",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Theme Mode Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "App Theme Mode",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Adapts dynamically to system dark mode & Android 12+ wallpaper colors.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        AppThemeMode.SYSTEM to "System",
                        AppThemeMode.DARK to "Dark",
                        AppThemeMode.LIGHT to "Light",
                        AppThemeMode.DYNAMIC to "Material You"
                    ).forEach { (mode, label) ->
                        val selected = uiState.themeMode == mode
                        val bg = if (selected) AccentPrimary else Color(0xFF1B2438)
                        val textCol = if (selected) DarkBackground else TextSecondary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { onAction(SettingsAction.SetThemeMode(mode)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = textCol,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Units & Formatting",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Temperature Unit Switch Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Temperature Unit",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.useFahrenheit) "Fahrenheit (°F)" else "Celsius (°C)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = uiState.useFahrenheit,
                        onCheckedChange = { onAction(SettingsAction.SetUseFahrenheit(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPrimary,
                            checkedTrackColor = Color(0xFF1E3A5F)
                        ),
                        modifier = Modifier.testTag("switch_temperature_unit")
                    )
                }

                HorizontalDivider(
                    color = Color(0x15FFFFFF),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High Precision Sensors",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Query hardware registers directly where allowed",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = uiState.isHighPrecisionSensors,
                        onCheckedChange = { onAction(SettingsAction.SetHighPrecision(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPrimary,
                            checkedTrackColor = Color(0xFF1E3A5F)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
