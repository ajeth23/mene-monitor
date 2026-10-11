package app.mene.monitor.presentation.cpu

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Memory
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
import app.mene.monitor.presentation.components.CoreUsageRow
import app.mene.monitor.presentation.components.DetailsCard
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.components.InfoItem
import app.mene.monitor.presentation.components.UsageChart
import app.mene.monitor.presentation.components.UsageGaugeWithStats
import app.mene.monitor.presentation.theme.AccentCpu
import app.mene.monitor.presentation.theme.CpuCardBg
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun CpuScreen(
    uiState: CpuUiState,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HardwareHeader(
            title = "CPU",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick,
            readingUnavailable = uiState.cpu?.hasUsageReading == false
        )

        val cpu = uiState.cpu

        if (cpu == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCpu)
            }
            return
        }

        if (cpu != null) {
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
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = AccentCpu,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = cpu.name,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cpu.coreCount} Cores / ${cpu.threadCount} Threads",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Base Clock: ${cpu.baseFrequency}",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val hasCpuTemp = cpu.temperature.isNotBlank() && cpu.temperature != "N/A"
                val displayTemp = if (hasCpuTemp) cpu.temperature else uiState.batteryTemperature ?: "N/A"
                val tempLabel = if (hasCpuTemp) "CPU Temp" else if (uiState.batteryTemperature != null) "Battery Temp" else "Temperature"

                // Circular Gauge & Stats
                UsageGaugeWithStats(
                    percentage = cpu.usagePercentage,
                    hasReading = cpu.hasUsageReading,
                    temperature = displayTemp,
                    tempLabel = tempLabel,
                    clockSpeed = cpu.currentFrequency,
                    accentColor = AccentCpu
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Usage History Chart
                UsageChart(
                    dataPoints = cpu.usageHistory,
                    lineColor = AccentCpu,
                    title = "CPU Usage",
                    timestamps = cpu.usageTimestamps,
                    currentValue = if (!cpu.hasUsageReading) null else cpu.usagePercentage.toFloat()
                )

                Spacer(modifier = Modifier.height(16.dp))

                UsageChart(
                    dataPoints = cpu.temperatureHistory,
                    timestamps = cpu.temperatureTimestamps,
                    lineColor = AccentCpu,
                    title = "CPU / SoC Temperature",
                    minValue = if (cpu.temperatureUnit == "°F") 32f else 0f,
                    maxValue = if (cpu.temperatureUnit == "°F") 212f else 100f,
                    unit = cpu.temperatureUnit,
                    unavailableMessage = "Temperature unavailable on this device"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Core Usage List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Core Usage & Clock Speeds",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    cpu.cores.forEach { core ->
                        CoreUsageRow(core = core, accentColor = AccentCpu)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CPU Details Card
                val details = listOf(
                    InfoItem("CPU Usage", "${cpu.usagePercentage}%"),
                    InfoItem("CPU Sensor Temp", cpu.temperature),
                    InfoItem("Battery Temp", uiState.batteryTemperature ?: "Unavailable"),
                    InfoItem("Architecture", cpu.architecture),
                    InfoItem("Base Clock", cpu.baseFrequency),
                    InfoItem("Current Average Clock", cpu.currentFrequency),
                    InfoItem("Active Cores", "${cpu.cores.count { it.frequencyMhz > 0 }} of ${cpu.coreCount}"),
                    InfoItem("Governor", cpu.governor),
                    InfoItem("L1 Cache", cpu.l1Cache),
                    InfoItem("L2 Cache", cpu.l2Cache),
                    InfoItem("L3 Cache", cpu.l3Cache),
                    InfoItem("TDP", cpu.tdp),
                    InfoItem("Process Node", cpu.processNode)
                )
                DetailsCard(
                    title = "CPU Details",
                    items = details,
                    modifier = Modifier.testTag("cpu_details_card"),
                    expandable = true
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
