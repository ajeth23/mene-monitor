package app.mene.monitor.data.repository

import android.os.Build
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
import app.mene.monitor.domain.model.DiagnosticCategory
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
    private val themeModeState = MutableStateFlow(app.mene.monitor.presentation.theme.AppThemeMode.LIGHT)

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
            emit(storageDataSource.getStorageInfo())
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeBatteryInfo(): Flow<BatteryInfo> = flow {
        while (true) {
            emit(batteryDataSource.getBatteryInfo(useFahrenheit.get()))
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeNetworkInfo(): Flow<NetworkInfo> = flow {
        while (true) {
            emit(networkDataSource.getNetworkInfo())
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override fun observeSensors(): Flow<List<SensorInfo>> = flow {
        while (true) {
            emit(sensorDataSource.getSensorsList(useFahrenheit.get()))
            delay(refreshIntervalMs.get())
        }
    }.flowOn(dispatchers.io)

    override suspend fun getSystemInfo(): SystemInfo = withContext(dispatchers.io) {
        val dev = getDeviceInfo()
        val allSensors = sensorDataSource.getSensorsList(useFahrenheit.get())
        SystemInfo(
            device = dev,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            javaVm = System.getProperty("java.vm.name") ?: "ART",
            openGlVersion = "OpenGL ES 3.2",
            sensorCount = allSensors.size,
            bootloader = Build.BOOTLOADER,
            radioVersion = Build.getRadioVersion() ?: "Unknown",
            fingerprint = Build.FINGERPRINT
        )
    }

    override fun setRefreshInterval(seconds: Long) {
        refreshIntervalMs.set(seconds * 1000L)
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
        val item = getDiagnosticItems().firstOrNull { it.id == id } ?: DiagnosticItem(
            id = id,
            title = id,
            description = "",
            category = DiagnosticCategory.AUTOMATED
        )
        when (id) {
            "vibration" -> {
                val success = diagnosticsDataSource.runVibrationTest()
                item.copy(
                    status = if (success) TestStatus.NEEDS_CONFIRMATION else TestStatus.WARNING,
                    detailMessage = if (success) "Pattern vibration emitted" else "Device vibrator is unavailable or failed",
                    nextStep = "Confirm whether you felt the pattern vibrate."
                )
            }
            "flashlight" -> {
                val success = diagnosticsDataSource.testFlashlightStrobe()
                item.copy(
                    status = if (success) TestStatus.NEEDS_CONFIRMATION else TestStatus.WARNING,
                    detailMessage = if (success) "Strobe tested & turned off" else "Could not activate the flashlight",
                    nextStep = "Confirm whether the LED lit up. If not, close camera apps and retry."
                )
            }
            "audio" -> {
                val success = diagnosticsDataSource.playAudioTestTone()
                item.copy(
                    status = if (success) TestStatus.NEEDS_CONFIRMATION else TestStatus.WARNING,
                    detailMessage = if (success) "Played 440 Hz test tone" else "Could not play the test tone",
                    nextStep = "Confirm whether you heard the tone from the phone speaker. Check media volume and disconnect Bluetooth or headphones before retrying."
                )
            }
            "sensors" -> item.copy(
                detailMessage = "Open the guided test to check live sensor responses",
                nextStep = "Tilt, rotate, and cover sensors as instructed."
            )
            "battery" -> {
                val battery = batteryDataSource.getBatteryInfo(useFahrenheit.get())
                val healthy = battery.health.equals("good", ignoreCase = true) || battery.health.equals("healthy", ignoreCase = true)
                item.copy(
                    status = if (healthy) TestStatus.PASSED else TestStatus.WARNING,
                    detailMessage = "Charge: ${battery.chargeLabel}, Android health: ${battery.health}, Temp: ${battery.temperature}, Voltage: ${battery.voltageFormatted}",
                    nextStep = if (healthy) "Android reports good health. This does not measure battery capacity or verify charging performance." else "Review Battery details. If the phone is hot, let it cool before retesting; persistent health warnings may require service."
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
