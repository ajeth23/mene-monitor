package app.mene.monitor.presentation.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.presentation.components.DetailsCard
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.components.InfoItem
import app.mene.monitor.presentation.components.UsageChart
import app.mene.monitor.presentation.theme.AccentMemory
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.MemoryCardBg
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun MemoryScreen(
    uiState: MemoryUiState,
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
            title = "Memory",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        val mem = uiState.memory

        if (mem == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentMemory)
            }
            return
        }

        if (mem != null) {
            val usedStr = Formatters.formatBytes(mem.usedBytes)
            val totalStr = Formatters.formatBytes(mem.totalBytes)
            val availStr = Formatters.formatBytes(mem.availableBytes)
            val swapTotalStr = Formatters.formatBytes(mem.swapTotalBytes)
            val swapUsedStr = Formatters.formatBytes(mem.swapUsedBytes)

            val progress by animateFloatAsState(
                targetValue = (mem.usagePercentage / 100f).coerceIn(0f, 1f),
                animationSpec = tween(500),
                label = "mem_progress"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Big Hero Memory Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = null,
                            tint = AccentMemory,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "${mem.usagePercentage}%",
                                color = TextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$usedStr / $totalStr",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wide progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentMemory)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Usage History Chart (Green)
                UsageChart(
                    dataPoints = mem.usageHistory,
                    lineColor = AccentMemory,
                    title = "Usage History"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Memory Details Card
                val details = listOf(
                    InfoItem("Total RAM", totalStr),
                    InfoItem("Used RAM", usedStr),
                    InfoItem("Available RAM", availStr),
                    InfoItem("Speed", mem.memorySpeed),
                    InfoItem("Type", mem.memoryType),
                    InfoItem("Swap / ZRAM Total", swapTotalStr),
                    InfoItem("Swap / ZRAM Used", swapUsedStr),
                    InfoItem("Low Memory Status", if (mem.isLowMemory) "Warning: Low" else "Normal")
                )
                DetailsCard(
                    title = "Memory Details",
                    items = details,
                    modifier = Modifier.testTag("memory_details_card")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
