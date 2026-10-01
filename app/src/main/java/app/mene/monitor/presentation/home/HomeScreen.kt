package app.mene.monitor.presentation.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import app.mene.monitor.R
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.CpuInfo
import app.mene.monitor.domain.model.DeviceInfo
import app.mene.monitor.domain.model.GpuInfo
import app.mene.monitor.domain.model.MemoryInfo
import app.mene.monitor.domain.model.NetworkInfo
import app.mene.monitor.domain.model.StorageInfo
import app.mene.monitor.presentation.components.HardwareDeviceCard
import app.mene.monitor.presentation.components.MetricCardSummary
import app.mene.monitor.presentation.components.MetricCardWithBar
import app.mene.monitor.presentation.components.MetricCardWithSparkline
import app.mene.monitor.presentation.theme.AccentBattery
import app.mene.monitor.presentation.theme.AccentCpu
import app.mene.monitor.presentation.theme.AccentGpu
import app.mene.monitor.presentation.theme.AccentMemory
import app.mene.monitor.presentation.theme.AccentNetwork
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.AccentStorage
import app.mene.monitor.presentation.theme.BatteryCardBg
import app.mene.monitor.presentation.theme.CpuCardBg
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.DarkSurfaceVariant
import app.mene.monitor.presentation.theme.GpuCardBg
import app.mene.monitor.presentation.theme.MemoryCardBg
import app.mene.monitor.presentation.theme.NetworkCardBg
import app.mene.monitor.presentation.theme.StorageCardBg
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateCpu: () -> Unit,
    onNavigateGpu: () -> Unit,
    onNavigateMemory: () -> Unit,
    onNavigateStorage: () -> Unit,
    onNavigateBattery: () -> Unit,
    onNavigateNetwork: () -> Unit,
    onNavigateDiagnostics: () -> Unit,
    onNavigateSystemInfo: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top Header
        HomeTopHeader(onSettingsClick = onNavigateSettings)

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Device Card
        if (uiState.device != null) {
            HardwareDeviceCard(
                device = uiState.device,
                onClick = onNavigateSystemInfo,
                modifier = Modifier.testTag("home_device_card")
            )
        } else if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPrimary, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Premium Hardware Diagnostics Banner
        val diagInteractionSource = remember { MutableInteractionSource() }
        val isDiagPressed by diagInteractionSource.collectIsPressedAsState()
        val diagScale by animateFloatAsState(
            targetValue = if (isDiagPressed) 0.985f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "diag_banner_scale"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = diagScale, scaleY = diagScale)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .clickable(
                    interactionSource = diagInteractionSource,
                    indication = null
                ) { onNavigateDiagnostics() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hardware Diagnostics Suite",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Interactive display, touch, audio & sensor tests",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "Run Tests ➔",
                color = AccentPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metric Cards Grid
        // Row 1: CPU & GPU
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val cpu = uiState.cpu
            MetricCardWithSparkline(
                title = "CPU",
                icon = Icons.Default.Memory,
                accentColor = AccentCpu,
                iconBgColor = CpuCardBg,
                primaryValue = if (cpu != null) "${cpu.usagePercentage}%" else "--%",
                primaryLabel = "Usage",
                secondaryValue = cpu?.temperature ?: "--°C",
                secondaryLabel = "Temp",
                sparklineData = cpu?.usageHistory ?: listOf(10f, 15f, 12f, 18f, 14f),
                onClick = onNavigateCpu,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_cpu")
            )

            val gpu = uiState.gpu
            MetricCardWithSparkline(
                title = "GPU",
                icon = Icons.Default.Speed,
                accentColor = AccentGpu,
                iconBgColor = GpuCardBg,
                primaryValue = if (gpu != null) "${gpu.usagePercentage}%" else "--%",
                primaryLabel = "Usage",
                secondaryValue = gpu?.temperature ?: "--°C",
                secondaryLabel = "Temp",
                sparklineData = gpu?.usageHistory ?: listOf(8f, 12f, 10f, 14f, 9f),
                onClick = onNavigateGpu,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_gpu")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Memory & Storage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val mem = uiState.memory
            val memUsedGb = if (mem != null) Formatters.formatBytes(mem.usedBytes) else "0 GB"
            val memTotalGb = if (mem != null) Formatters.formatBytes(mem.totalBytes) else "0 GB"
            MetricCardWithBar(
                title = "Memory",
                icon = Icons.Default.DeveloperBoard,
                accentColor = AccentMemory,
                iconBgColor = MemoryCardBg,
                percentage = mem?.usagePercentage ?: 0,
                detailsText = "$memUsedGb / $memTotalGb",
                onClick = onNavigateMemory,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_memory")
            )

            val storage = uiState.storage
            val stUsedGb = if (storage != null) Formatters.formatBytes(storage.usedBytes) else "0 GB"
            val stTotalGb = if (storage != null) Formatters.formatBytes(storage.totalBytes) else "0 GB"
            MetricCardWithBar(
                title = "Storage",
                icon = Icons.Default.Storage,
                accentColor = AccentStorage,
                iconBgColor = StorageCardBg,
                percentage = storage?.usagePercentage ?: 0,
                detailsText = "$stUsedGb / $stTotalGb",
                onClick = onNavigateStorage,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_storage")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Battery & Network
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val bat = uiState.battery
            MetricCardSummary(
                title = "Battery",
                icon = Icons.Default.BatteryChargingFull,
                accentColor = AccentBattery,
                iconBgColor = BatteryCardBg,
                mainText = if (bat != null) "${bat.percentage}%" else "--%",
                subText = bat?.estimatedTimeRemaining ?: "Calculating...",
                onClick = onNavigateBattery,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_battery")
            )

            val net = uiState.network
            MetricCardSummary(
                title = "Network",
                icon = Icons.Default.Wifi,
                accentColor = AccentNetwork,
                iconBgColor = NetworkCardBg,
                mainText = if (net != null && net.isConnected) "↓ ${net.formattedDownloadSpeed}" else if (net != null) "Offline" else "...",
                subText = if (net != null && net.isConnected) "↑ ${net.formattedUploadSpeed}" else if (net != null) net.networkType else "--",
                onClick = onNavigateNetwork,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_network")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HomeTopHeader(
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menemonitor),
                contentDescription = "Mene Monitor Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Mene Monitor",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your device, in real time.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.testTag("home_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextSecondary
            )
        }
    }
}
