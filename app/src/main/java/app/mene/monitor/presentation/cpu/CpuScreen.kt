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
            onRefreshClick = onRefreshClick
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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cpu.coreCount} Cores / ${cpu.threadCount} Threads",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Base Clock: ${cpu.baseFrequency}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Circular Gauge & Stats
                UsageGaugeWithStats(
                    percentage = cpu.usagePercentage,
                    temperature = cpu.temperature,
                    clockSpeed = cpu.currentFrequency,
                    accentColor = AccentCpu
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
                        text = "Core Usage",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    cpu.cores.forEach { core ->
                        CoreUsageRow(core = core, accentColor = AccentCpu)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Usage History Chart
                UsageChart(
                    dataPoints = cpu.usageHistory,
                    lineColor = AccentCpu,
                    title = "Usage History"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CPU Details Card
                val details = listOf(
                    InfoItem("Architecture", cpu.architecture),
                    InfoItem("Base Clock", cpu.baseFrequency),
                    InfoItem("Current Average Clock", cpu.currentFrequency),
                    InfoItem("L1 Cache", cpu.l1Cache),
                    InfoItem("L2 Cache", cpu.l2Cache),
                    InfoItem("L3 Cache", cpu.l3Cache),
                    InfoItem("Governor", cpu.governor),
                    InfoItem("TDP", cpu.tdp),
                    InfoItem("Process Node", cpu.processNode)
                )
                DetailsCard(
                    title = "CPU Details",
                    items = details,
                    modifier = Modifier.testTag("cpu_details_card")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
