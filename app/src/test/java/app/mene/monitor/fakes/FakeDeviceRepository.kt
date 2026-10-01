package app.mene.monitor.fakes

import app.mene.monitor.domain.model.BatteryInfo
import app.mene.monitor.domain.model.CpuInfo
import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.model.DeviceInfo
import app.mene.monitor.domain.model.GpuInfo
import app.mene.monitor.domain.model.MemoryInfo
import app.mene.monitor.domain.model.NetworkInfo
import app.mene.monitor.domain.model.SensorInfo
import app.mene.monitor.domain.model.StorageInfo
import app.mene.monitor.domain.model.SystemInfo
import app.mene.monitor.domain.model.TestStatus
import app.mene.monitor.domain.repository.DeviceRepository
import app.mene.monitor.presentation.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeDeviceRepository : DeviceRepository {

    var deviceInfoToReturn: DeviceInfo = DeviceInfo(
        deviceName = "Pixel 8 Pro",
        model = "Pixel 8 Pro",
        manufacturer = "Google",
        brand = "google",
        board = "husky",
        hardware = "zuma",
        processorName = "Tensor G3",
        gpuName = "Mali-G715",
        operatingSystem = "Android 15",
        androidVersion = "15",
        apiLevel = 35,
        securityPatch = "2024-11-01",
        kernelVersion = "6.1.75",
        buildNumber = "AP3A.241105.007",
        screenResolution = "1344 x 2992",
        screenDensityDpi = 480,
        refreshRateHz = 120f,
        uptimeFormatted = "12h 34m"
    )

    var cpuInfoToReturn: CpuInfo = CpuInfo(
        name = "Tensor G3",
        manufacturer = "Google",
        architecture = "ARM64",
        coreCount = 8,
        threadCount = 8,
        baseFrequency = "2.8 GHz",
        currentFrequency = "2.2 GHz",
        usagePercentage = 35,
        temperature = "42°C",
        cores = emptyList(),
        l1Cache = "64 KB",
        l2Cache = "512 KB",
        l3Cache = "8 MB",
        governor = "Schedutil",
        tdp = "10 W",
        processNode = "4 nm",
        usageHistory = emptyList()
    )

    var gpuInfoToReturn: GpuInfo = GpuInfo(
        name = "Mali-G715",
        vendor = "ARM",
        usagePercentage = 28,
        temperature = "40°C",
        clockSpeed = "890 MHz",
        vram = "4 GB",
        driverVersion = "v1.2",
        apiVersion = "OpenGL ES 3.2",
        busInfo = "Bus 0",
        usageHistory = emptyList()
    )

    var memoryInfoToReturn: MemoryInfo = MemoryInfo(
        totalBytes = 12800000000L,
        usedBytes = 6400000000L,
        availableBytes = 6400000000L,
        usagePercentage = 50,
        memoryType = "LPDDR5X",
        memorySpeed = "4266 MHz",
        swapTotalBytes = 4000000000L,
        swapUsedBytes = 1000000000L,
        isLowMemory = false,
        usageHistory = emptyList()
    )

    var storageInfoToReturn: StorageInfo = StorageInfo(
        deviceName = "Internal Storage",
        mountPoint = "/data",
        totalBytes = 256000000000L,
        usedBytes = 128000000000L,
        availableBytes = 128000000000L,
        usagePercentage = 50,
        fileSystem = "ext4",
        health = "Good",
        temperature = "32°C",
        isRemovable = false,
        usageHistory = emptyList()
    )

    var batteryInfoToReturn: BatteryInfo = BatteryInfo(
        percentage = 85,
        chargingState = "Charging",
        isCharging = true,
        health = "Good",
        temperature = "38°C",
        voltageFormatted = "4.1V",
        voltageMv = 4100,
        currentMa = 500,
        capacityMah = 4200,
        designCapacityMah = 5000,
        designCapacityWh = "5.0 Wh",
        fullChargeCapacityWh = "4.2 Wh",
        powerSource = "AC Adapter",
        technology = "Li-ion",
        estimatedTimeRemaining = "1h 20m",
        usageHistory = emptyList()
    )

    var sensorsToReturn: List<SensorInfo> = emptyList()

    var networkInfoToReturn: NetworkInfo = NetworkInfo(
        isConnected = true,
        networkType = "Wi-Fi",
        downloadSpeedBps = 5200000L,
        uploadSpeedBps = 1100000L,
        formattedDownloadSpeed = "5.2 MB/s",
        formattedUploadSpeed = "1.1 MB/s",
        ipAddress = "192.168.1.10",
        linkSpeedMbps = 866,
        frequencyMhz = 5200,
        signalStrength = "Excellent (-55 dBm)",
        ssid = "Mene-WiFi",
        usageHistory = emptyList()
    )

    var diagnosticItemsToReturn: List<DiagnosticItem> = listOf(
        DiagnosticItem(
            id = "vibration",
            title = "Vibration Motor",
            description = "Tests haptic feedback motor",
            category = app.mene.monitor.domain.model.DiagnosticCategory.AUTOMATED,
            status = TestStatus.NOT_TESTED
        )
    )

    var getDeviceInfoCallCount = 0
    private var refreshInterval = 2L
    private var useFahrenheit = false
    private val themeModeState = MutableStateFlow(AppThemeMode.SYSTEM)

    override suspend fun getDeviceInfo(): DeviceInfo {
        getDeviceInfoCallCount++
        return deviceInfoToReturn
    }

    override fun observeCpuInfo(): Flow<CpuInfo> = flowOf(cpuInfoToReturn)
    override fun observeGpuInfo(): Flow<GpuInfo> = flowOf(gpuInfoToReturn)
    override fun observeMemoryInfo(): Flow<MemoryInfo> = flowOf(memoryInfoToReturn)
    override fun observeStorageInfo(): Flow<StorageInfo> = flowOf(storageInfoToReturn)
    override fun observeBatteryInfo(): Flow<BatteryInfo> = flowOf(batteryInfoToReturn)
    override fun observeSensors(): Flow<List<SensorInfo>> = flowOf(sensorsToReturn)
    override fun observeNetworkInfo(): Flow<NetworkInfo> = flowOf(networkInfoToReturn)

    override suspend fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            device = deviceInfoToReturn,
            supportedAbis = listOf("arm64-v8a"),
            javaVm = "ART",
            openGlVersion = "OpenGL ES 3.2",
            sensorCount = sensorsToReturn.size,
            bootloader = "husky-1.0",
            radioVersion = "g5300g-24",
            fingerprint = "google/husky/husky:15/AP3A.241105.007/123456:user/release-keys"
        )
    }

    override fun setRefreshInterval(seconds: Long) { refreshInterval = seconds }
    override fun getRefreshInterval(): Long = refreshInterval
    override fun setUseFahrenheit(enabled: Boolean) { useFahrenheit = enabled }
    override fun isUseFahrenheit(): Boolean = useFahrenheit
    override fun setThemeMode(mode: AppThemeMode) { themeModeState.value = mode }
    override fun getThemeMode(): AppThemeMode = themeModeState.value
    override fun observeThemeMode(): Flow<AppThemeMode> = themeModeState.asStateFlow()

    override fun getDiagnosticItems(): List<DiagnosticItem> = diagnosticItemsToReturn

    override suspend fun runDiagnosticTest(id: String): DiagnosticItem {
        val item = diagnosticItemsToReturn.firstOrNull { it.id == id } ?: DiagnosticItem(
            id = id,
            title = "Test",
            description = "Test",
            category = app.mene.monitor.domain.model.DiagnosticCategory.AUTOMATED,
            status = TestStatus.PASSED
        )
        return item.copy(status = TestStatus.PASSED)
    }

    override fun toggleFlashlight(enable: Boolean?): Boolean = true
    override fun runVibrationTest(): Boolean = true
    override suspend fun playAudioTestTone(): Boolean = true
}
