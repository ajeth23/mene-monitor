package app.mene.monitor.presentation.storage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Storage
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
import app.mene.monitor.presentation.theme.AccentStorage
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.StorageCardBg
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun StorageScreen(
    uiState: StorageUiState,
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
            title = "Storage",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        val storage = uiState.storage

        if (storage == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentStorage)
            }
            return
        }

        if (storage != null) {
            val usedStr = Formatters.formatBytes(storage.usedBytes)
            val totalStr = Formatters.formatBytes(storage.totalBytes)
            val availStr = Formatters.formatBytes(storage.availableBytes)

            val progress by animateFloatAsState(
                targetValue = (storage.usagePercentage / 100f).coerceIn(0f, 1f),
                animationSpec = tween(500),
                label = "storage_progress"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Hero Storage Card
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
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = AccentStorage,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "${storage.usagePercentage}%",
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
                                .background(AccentStorage)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Usage History Chart (Orange)
                UsageChart(
                    dataPoints = storage.usageHistory,
                    lineColor = AccentStorage,
                    title = "Usage History"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Drive Details Card
                val details = listOf(
                    InfoItem("Device Name", storage.deviceName),
                    InfoItem("Mount Point", storage.mountPoint),
                    InfoItem("Capacity", totalStr),
                    InfoItem("Used Space", usedStr),
                    InfoItem("Available Space", availStr),
                    InfoItem("File System", storage.fileSystem),
                    InfoItem("Temperature", storage.temperature),
                    InfoItem("Health", storage.health, AccentMemory)
                )
                DetailsCard(
                    title = "Drive Details",
                    items = details,
                    modifier = Modifier.testTag("storage_details_card")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
