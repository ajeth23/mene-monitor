package app.mene.monitor.presentation.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import app.mene.monitor.presentation.components.DetailsCard
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.components.InfoItem
import app.mene.monitor.presentation.components.UsageChart
import app.mene.monitor.presentation.components.UsageGaugeWithStats
import app.mene.monitor.presentation.theme.AccentBattery
import app.mene.monitor.presentation.theme.AccentMemory
import app.mene.monitor.presentation.theme.BatteryCardBg
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun BatteryScreen(
    uiState: BatteryUiState,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HardwareHeader(
            title = "Battery",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        val bat = uiState.battery

        if (bat == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBattery)
            }
            return
        }

        if (bat != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header spec row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = AccentBattery,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "${bat.chargeLabel} charge",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${bat.chargingState} • ${bat.powerSource}",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = bat.estimatedTimeRemaining,
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Circular Gauge & Stats
                if (bat.percentage >= 0) UsageGaugeWithStats(
                    percentage = bat.percentage,
                    temperature = bat.temperature,
                    clockSpeed = bat.voltageFormatted,
                    accentColor = AccentBattery,
                    clockLabel = "Voltage"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Usage History Chart (Teal)
                UsageChart(
                    dataPoints = bat.usageHistory,
                    lineColor = AccentBattery,
                    title = "Charge Level History",
                    timestamps = bat.usageTimestamps,
                    currentValue = bat.percentage.takeIf { it in 0..100 }?.toFloat()
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailsCard(
                    title = "Battery Health",
                    items = listOf(
                        InfoItem("Capacity health", bat.healthLabel),
                        InfoItem("Android condition", bat.health)
                    )
                )
                Text(
                    text = if (bat.healthPercentage != null)
                        "Estimated from device-reported full charge and design capacities. This differs from the current charge level."
                        else "This device does not expose the capacity readings needed for a health percentage. Android condition is a separate status.",
                    color = TextSecondary, fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp)
                )
                // Battery Details Card
                val details = listOf(
                    InfoItem("Status", bat.chargingState),
                    InfoItem("Power Source", bat.powerSource),
                    InfoItem("Android condition", bat.health),
                    InfoItem("Temperature", bat.temperature),
                    InfoItem("Voltage", bat.voltageFormatted),
                    InfoItem("Current flow (signed)", bat.currentMa?.let { "$it mA" } ?: "Unavailable"),
                    InfoItem("Design capacity", bat.designCapacityMah?.let { "$it mAh" } ?: "Unavailable"),
                    InfoItem("Full charge capacity", bat.fullChargeCapacityMah?.let { "$it mAh" } ?: "Unavailable"),
                    InfoItem("Remaining charge", bat.capacityMah?.let { "$it mAh" } ?: "Unavailable"),
                    InfoItem("Technology", bat.technology),
                    InfoItem("Estimated Time", bat.estimatedTimeRemaining)
                )
                DetailsCard(
                    title = "Battery Details",
                    items = details,
                    modifier = Modifier.testTag("battery_details_card")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
