package app.mene.monitor.data.repository

import app.mene.monitor.core.dispatcher.CoroutineDispatchers
import app.mene.monitor.data.datasource.system.AndroidBatteryDataSource
import app.mene.monitor.data.datasource.system.AndroidCpuDataSource
import app.mene.monitor.data.datasource.system.AndroidDeviceDataSource
import app.mene.monitor.data.datasource.system.AndroidDiagnosticsDataSource
import app.mene.monitor.data.datasource.system.AndroidGpuDataSource
import app.mene.monitor.data.datasource.system.AndroidMemoryDataSource
import app.mene.monitor.data.datasource.system.AndroidNetworkDataSource
import app.mene.monitor.data.datasource.system.AndroidSensorDataSource
import app.mene.monitor.data.datasource.system.AndroidStorageDataSource
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class DeviceRepositoryImpl(
    private val deviceDataSource: AndroidDeviceDataSource,
    private val cpuDataSource: AndroidCpuDataSource,
    private val gpuDataSource: AndroidGpuDataSource,
    private val memoryDataSource: AndroidMemoryDataSource,
    private val storageDataSource: AndroidStorageDataSource,
    private val batteryDataSource: AndroidBatteryDataSource,
    private val sensorDataSource: AndroidSensorDataSource,
    private val networkDataSource: AndroidNetworkDataSource,
    private val diagnosticsDataSource: AndroidDiagnosticsDataSource,
    private val dispatchers: CoroutineDispatchers
) : DeviceRepository {

    private val refreshIntervalMs = AtomicLong(2000L)
    private val useFahrenheit = AtomicBoolean(false)
    private val themeModeState = MutableStateFlow(app.mene.monitor.presentation.theme.AppThemeMode.SYSTEM)

    override suspend fun getDeviceInfo(): DeviceInfo = withContext(dispatchers.io) {
        val base = deviceDataSource.getDeviceInfo()
        val gpu = gpuDataSource.getGpuInfo(useFahrenheit.get())
        base.copy(gpuName = gpu.name)
    }

    override fun observeCpuInfo(): Flow<CpuInfo> = flow {
        while (true) {
            emit(cpuDataSource.getCpuInfo(useFahrenheit.get()))
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeGpuInfo(): Flow<GpuInfo> = flow {
        while (true) {
            emit(gpuDataSource.getGpuInfo(useFahrenheit.get()))
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeMemoryInfo(): Flow<MemoryInfo> = flow {
        while (true) {
            emit(memoryDataSource.getMemoryInfo())
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeStorageInfo(): Flow<StorageInfo> = flow {
        while (true) {
            emit(storageDataSource.getStorageInfo(useFahrenheit.get()))
            delay(refreshIntervalMs.get().coerceAtLeast(3000L))
        }
    }.flowOn(dispatchers.io)

    override fun observeBatteryInfo(): Flow<BatteryInfo> = flow {
        while (true) {
            emit(batteryDataSource.getBatteryInfo(useFahrenheit.get()))
            delay(refreshIntervalMs.get().coerceAtLeast(2000L))
        }
    }.flowOn(dispatchers.io)

    override fun observeSensors(): Flow<List<SensorInfo>> = flow {
        while (true) {
            emit(sensorDataSource.getSensorsList(useFahrenheit.get()))
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeNetworkInfo(): Flow<NetworkInfo> = flow {
        while (true) {
            emit(networkDataSource.getNetworkInfo())
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override suspend fun getSystemInfo(): SystemInfo = withContext(dispatchers.io) {
        val device = getDeviceInfo()
        val sensors = sensorDataSource.getSensorsList(useFahrenheit.get())
        SystemInfo(
            device = device,
            supportedAbis = android.os.Build.SUPPORTED_ABIS.toList(),
            javaVm = System.getProperty("java.vm.name") ?: "Android Runtime (ART)",
            openGlVersion = "OpenGL ES 3.2",
            sensorCount = sensors.size,
            bootloader = android.os.Build.BOOTLOADER,
            radioVersion = android.os.Build.getRadioVersion() ?: "N/A",
            fingerprint = android.os.Build.FINGERPRINT
        )
    }

    override fun setRefreshInterval(seconds: Long) {
        refreshIntervalMs.set(seconds.coerceIn(1, 10) * 1000L)
    }

    override fun getRefreshInterval(): Long {
        return refreshIntervalMs.get() / 1000L
    }

    override fun setUseFahrenheit(enabled: Boolean) {
        useFahrenheit.set(enabled)
    }

    override fun isUseFahrenheit(): Boolean {
        return useFahrenheit.get()
    }

    override fun setThemeMode(mode: app.mene.monitor.presentation.theme.AppThemeMode) {
        themeModeState.value = mode
    }

    override fun getThemeMode(): app.mene.monitor.presentation.theme.AppThemeMode {
        return themeModeState.value
    }

    override fun observeThemeMode(): Flow<app.mene.monitor.presentation.theme.AppThemeMode> {
        return themeModeState.asStateFlow()
    }

    override fun getDiagnosticItems(): List<DiagnosticItem> {
        return diagnosticsDataSource.getInitialDiagnosticItems()
    }

    override suspend fun runDiagnosticTest(id: String): DiagnosticItem = withContext(dispatchers.io) {
        val baseItems = diagnosticsDataSource.getInitialDiagnosticItems()
        val item = baseItems.firstOrNull { it.id == id } ?: return@withContext DiagnosticItem(
            id = id,
            title = "Unknown Test",
            description = "Test not found",
            category = app.mene.monitor.domain.model.DiagnosticCategory.AUTOMATED,
            status = TestStatus.FAILED
        )

        when (id) {
            "vibration" -> {
                val success = diagnosticsDataSource.runVibrationTest()
                item.copy(
                    status = if (success) TestStatus.PASSED else TestStatus.FAILED,
                    detailMessage = if (success) "Haptic motor triggered successfully" else "Failed to trigger vibration"
                )
            }
            "flashlight" -> {
                val success = diagnosticsDataSource.testFlashlightStrobe()
                item.copy(
                    status = if (success) TestStatus.PASSED else TestStatus.FAILED,
                    detailMessage = if (success) "Strobe tested & turned off" else "Flashlight unavailable"
                )
            }
            "audio" -> {
                val success = diagnosticsDataSource.playAudioTestTone()
                item.copy(
                    status = if (success) TestStatus.PASSED else TestStatus.FAILED,
                    detailMessage = if (success) "Played 440 Hz test tone" else "Audio output test failed"
                )
            }
            "sensors" -> {
                val sensors = sensorDataSource.getSensorsList(useFahrenheit.get())
                val passed = sensors.isNotEmpty()
                item.copy(
                    status = if (passed) TestStatus.PASSED else TestStatus.FAILED,
                    detailMessage = if (passed) "${sensors.size} active sensors detected" else "No active sensors detected"
                )
            }
            "battery" -> {
                val battery = batteryDataSource.getBatteryInfo(useFahrenheit.get())
                val healthy = battery.health.lowercase().contains("good") || battery.health.lowercase().contains("healthy") || battery.percentage > 0
                item.copy(
                    status = if (healthy) TestStatus.PASSED else TestStatus.WARNING,
                    detailMessage = "Level: ${battery.percentage}%, Health: ${battery.health}, Temp: ${battery.temperature}"
                )
            }
            else -> item
        }
    }

    override fun toggleFlashlight(enable: Boolean?): Boolean {
        return diagnosticsDataSource.toggleFlashlight(enable)
    }

    override fun runVibrationTest(): Boolean {
        return diagnosticsDataSource.runVibrationTest()
    }

    override suspend fun playAudioTestTone(): Boolean {
        return diagnosticsDataSource.playAudioTestTone()
    }
}
