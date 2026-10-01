package app.mene.monitor.domain.repository

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
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    suspend fun getDeviceInfo(): DeviceInfo
    fun observeCpuInfo(): Flow<CpuInfo>
    fun observeGpuInfo(): Flow<GpuInfo>
    fun observeMemoryInfo(): Flow<MemoryInfo>
    fun observeStorageInfo(): Flow<StorageInfo>
    fun observeBatteryInfo(): Flow<BatteryInfo>
    fun observeSensors(): Flow<List<SensorInfo>>
    fun observeNetworkInfo(): Flow<NetworkInfo>
    suspend fun getSystemInfo(): SystemInfo

    fun setRefreshInterval(seconds: Long)
    fun getRefreshInterval(): Long
    fun setUseFahrenheit(enabled: Boolean)
    fun isUseFahrenheit(): Boolean
    fun setThemeMode(mode: app.mene.monitor.presentation.theme.AppThemeMode)
    fun getThemeMode(): app.mene.monitor.presentation.theme.AppThemeMode
    fun observeThemeMode(): Flow<app.mene.monitor.presentation.theme.AppThemeMode>

    fun getDiagnosticItems(): List<DiagnosticItem>
    suspend fun runDiagnosticTest(id: String): DiagnosticItem
    fun toggleFlashlight(enable: Boolean? = null): Boolean
    fun runVibrationTest(): Boolean
    suspend fun playAudioTestTone(): Boolean
}
