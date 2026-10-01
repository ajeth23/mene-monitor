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
                            text = "${bat.percentage}% Charged",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${bat.chargingState} • ${bat.powerSource}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = bat.estimatedTimeRemaining,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Circular Gauge & Stats
                UsageGaugeWithStats(
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
                    title = "Charge Level History"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Battery Details Card
                val details = listOf(
                    InfoItem("Status", bat.chargingState),
                    InfoItem("Power Source", bat.powerSource),
                    InfoItem("Health", bat.health, AccentMemory),
                    InfoItem("Temperature", bat.temperature),
                    InfoItem("Voltage", "${bat.voltageMv} mV (${bat.voltageFormatted})"),
                    InfoItem("Current Flow", "${bat.currentMa} mA"),
                    InfoItem("Design Capacity", "${bat.designCapacityMah} mAh (${bat.designCapacityWh})"),
                    InfoItem("Remaining Capacity", "${bat.capacityMah} mAh (${bat.fullChargeCapacityWh})"),
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
