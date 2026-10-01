package app.mene.monitor.presentation.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Wifi
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
import app.mene.monitor.presentation.theme.AccentMemory
import app.mene.monitor.presentation.theme.AccentNetwork
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.NetworkCardBg
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun NetworkScreen(
    uiState: NetworkUiState,
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
            title = "Network",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        val net = uiState.network

        if (net == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentNetwork)
            }
            return
        }

        if (net != null) {
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
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = AccentNetwork,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = net.ssid,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${net.networkType} • ${if (net.isConnected) "Connected" else "Disconnected"}",
                            color = if (net.isConnected) AccentMemory else TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "IP: ${net.ipAddress}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Throughput Speeds Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = AccentNetwork,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Download", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = net.formattedDownloadSpeed,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Upload Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Upload", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = net.formattedUploadSpeed,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Network Throughput History Chart
                UsageChart(
                    dataPoints = net.usageHistory,
                    lineColor = AccentNetwork,
                    title = "Throughput Activity (Mbps)",
                    maxValue = 100f
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Network Details Card
                val details = listOf(
                    InfoItem("Status", if (net.isConnected) "Connected" else "Offline", if (net.isConnected) AccentMemory else Color.Red),
                    InfoItem("Connection Type", net.networkType),
                    InfoItem("SSID / Carrier", net.ssid),
                    InfoItem("Local IP Address", net.ipAddress),
                    InfoItem("Link Speed", "${net.linkSpeedMbps} Mbps"),
                    InfoItem("Wi-Fi Frequency", "${net.frequencyMhz} MHz (5 GHz)"),
                    InfoItem("Signal Strength", net.signalStrength)
                )
                DetailsCard(
                    title = "Network Details",
                    items = details,
                    modifier = Modifier.testTag("network_details_card")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
