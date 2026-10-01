package app.mene.monitor.presentation.gpu

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
import androidx.compose.material.icons.filled.Speed
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
import app.mene.monitor.presentation.theme.AccentGpu
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.GpuCardBg
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun GpuScreen(
    uiState: GpuUiState,
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
            title = "GPU",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        val gpu = uiState.gpu

        if (gpu == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGpu)
            }
            return
        }

        if (gpu != null) {
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
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = AccentGpu,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = gpu.name,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = gpu.vendor,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = gpu.apiVersion,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Circular Gauge & Stats
                UsageGaugeWithStats(
                    percentage = gpu.usagePercentage,
                    temperature = gpu.temperature,
                    clockSpeed = gpu.clockSpeed,
                    accentColor = AccentGpu,
                    clockLabel = "Clock Speed"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Usage History Chart (Purple)
                UsageChart(
                    dataPoints = gpu.usageHistory,
                    lineColor = AccentGpu,
                    title = "Usage History"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // GPU Details Card
                val details = listOf(
                    InfoItem("Name", gpu.name),
                    InfoItem("Vendor", gpu.vendor),
                    InfoItem("Clock Speed", gpu.clockSpeed),
                    InfoItem("VRAM", gpu.vram),
                    InfoItem("Driver Version", gpu.driverVersion),
                    InfoItem("API Version", gpu.apiVersion),
                    InfoItem("Bus / Architecture", gpu.busInfo)
                )
                DetailsCard(
                    title = "GPU Details",
                    items = details,
                    modifier = Modifier.testTag("gpu_details_card")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
