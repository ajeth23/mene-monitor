package app.mene.monitor.presentation.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.presentation.components.DetailsCard
import app.mene.monitor.presentation.components.HardwareDeviceCard
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.components.InfoItem
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.DarkBackground

@Composable
fun SystemScreen(
    uiState: SystemUiState,
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
            title = "System Specifications",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        val sys = uiState.systemInfo

        if (sys == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPrimary)
            }
            return
        }

        if (sys != null) {
            val dev = sys.device
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Hero Device Card
                HardwareDeviceCard(
                    device = dev,
                    onClick = {},
                    modifier = Modifier.testTag("system_device_banner")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Operating System Specs
                val osItems = listOf(
                    InfoItem("OS Version", dev.operatingSystem),
                    InfoItem("API Level", "${dev.apiLevel}"),
                    InfoItem("Security Patch", dev.securityPatch),
                    InfoItem("Kernel Version", dev.kernelVersion),
                    InfoItem("Build Number", dev.buildNumber),
                    InfoItem("System Uptime", dev.uptimeFormatted),
                    InfoItem("Java VM", sys.javaVm),
                    InfoItem("Fingerprint", sys.fingerprint)
                )
                DetailsCard(
                    title = "Operating System",
                    items = osItems,
                    modifier = Modifier.testTag("card_os_details")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Device Hardware Specs
                val hwItems = listOf(
                    InfoItem("Device Model", dev.model),
                    InfoItem("Manufacturer", dev.manufacturer),
                    InfoItem("Brand", dev.brand),
                    InfoItem("Board", dev.board),
                    InfoItem("Hardware ID", dev.hardware),
                    InfoItem("Bootloader", sys.bootloader),
                    InfoItem("Radio / Baseband", sys.radioVersion),
                    InfoItem("Total Sensors Detected", "${sys.sensorCount}")
                )
                DetailsCard(
                    title = "Device Hardware",
                    items = hwItems,
                    modifier = Modifier.testTag("card_hardware_details")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display Specs
                val displayItems = listOf(
                    InfoItem("Screen Resolution", dev.screenResolution),
                    InfoItem("Screen Density", "${dev.screenDensityDpi} dpi"),
                    InfoItem("Display Refresh Rate", "${dev.refreshRateHz.toInt()} Hz")
                )
                DetailsCard(
                    title = "Display & Screen",
                    items = displayItems,
                    modifier = Modifier.testTag("card_display_details")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Instruction Sets / ABIs
                val abiItems = listOf(
                    InfoItem("Primary Architecture", sys.supportedAbis.firstOrNull() ?: "arm64-v8a"),
                    InfoItem("Supported ABIs", sys.supportedAbis.joinToString(", "))
                )
                DetailsCard(
                    title = "Processor Instruction Set",
                    items = abiItems,
                    modifier = Modifier.testTag("card_abi_details")
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
