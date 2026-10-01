package app.mene.monitor.core.di

import android.content.Context
import app.mene.monitor.core.dispatcher.CoroutineDispatchers
import app.mene.monitor.core.dispatcher.DefaultCoroutineDispatchers
import app.mene.monitor.data.datasource.system.AndroidBatteryDataSource
import app.mene.monitor.data.datasource.system.AndroidCpuDataSource
import app.mene.monitor.data.datasource.system.AndroidDeviceDataSource
import app.mene.monitor.data.datasource.system.AndroidDiagnosticsDataSource
import app.mene.monitor.data.datasource.system.AndroidGpuDataSource
import app.mene.monitor.data.datasource.system.AndroidMemoryDataSource
import app.mene.monitor.data.datasource.system.AndroidNetworkDataSource
import app.mene.monitor.data.datasource.system.AndroidSensorDataSource
import app.mene.monitor.data.datasource.system.AndroidStorageDataSource
import app.mene.monitor.data.repository.DeviceRepositoryImpl
import app.mene.monitor.domain.repository.DeviceRepository
import app.mene.monitor.domain.usecase.GetDiagnosticItemsUseCase
import app.mene.monitor.domain.usecase.GetDeviceInfoUseCase
import app.mene.monitor.domain.usecase.GetSystemInfoUseCase
import app.mene.monitor.domain.usecase.ObserveBatteryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveCpuInfoUseCase
import app.mene.monitor.domain.usecase.ObserveGpuInfoUseCase
import app.mene.monitor.domain.usecase.ObserveMemoryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveNetworkInfoUseCase
import app.mene.monitor.domain.usecase.ObserveSensorsUseCase
import app.mene.monitor.domain.usecase.ObserveStorageInfoUseCase
import app.mene.monitor.domain.usecase.RunDiagnosticTestUseCase
import app.mene.monitor.domain.usecase.UpdateSettingsUseCase

interface AppContainer {
    val deviceRepository: DeviceRepository
    val getDeviceInfoUseCase: GetDeviceInfoUseCase
    val observeCpuInfoUseCase: ObserveCpuInfoUseCase
    val observeGpuInfoUseCase: ObserveGpuInfoUseCase
    val observeMemoryInfoUseCase: ObserveMemoryInfoUseCase
    val observeStorageInfoUseCase: ObserveStorageInfoUseCase
    val observeBatteryInfoUseCase: ObserveBatteryInfoUseCase
    val observeSensorsUseCase: ObserveSensorsUseCase
    val observeNetworkInfoUseCase: ObserveNetworkInfoUseCase
    val getSystemInfoUseCase: GetSystemInfoUseCase
    val updateSettingsUseCase: UpdateSettingsUseCase
    val getDiagnosticItemsUseCase: GetDiagnosticItemsUseCase
    val runDiagnosticTestUseCase: RunDiagnosticTestUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val dispatchers: CoroutineDispatchers by lazy {
        DefaultCoroutineDispatchers()
    }

    private val deviceDataSource by lazy { AndroidDeviceDataSource(context) }
    private val cpuDataSource by lazy { AndroidCpuDataSource() }
    private val gpuDataSource by lazy { AndroidGpuDataSource(context) }
    private val memoryDataSource by lazy { AndroidMemoryDataSource(context) }
    private val storageDataSource by lazy { AndroidStorageDataSource(context) }
    private val batteryDataSource by lazy { AndroidBatteryDataSource(context) }
    private val sensorDataSource by lazy { AndroidSensorDataSource(context) }
    private val networkDataSource by lazy { AndroidNetworkDataSource(context) }
    private val diagnosticsDataSource by lazy { AndroidDiagnosticsDataSource(context) }

    override val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(
            deviceDataSource = deviceDataSource,
            cpuDataSource = cpuDataSource,
            gpuDataSource = gpuDataSource,
            memoryDataSource = memoryDataSource,
            storageDataSource = storageDataSource,
            batteryDataSource = batteryDataSource,
            sensorDataSource = sensorDataSource,
            networkDataSource = networkDataSource,
            diagnosticsDataSource = diagnosticsDataSource,
            dispatchers = dispatchers
        )
    }

    override val getDeviceInfoUseCase by lazy { GetDeviceInfoUseCase(deviceRepository) }
    override val observeCpuInfoUseCase by lazy { ObserveCpuInfoUseCase(deviceRepository) }
    override val observeGpuInfoUseCase by lazy { ObserveGpuInfoUseCase(deviceRepository) }
    override val observeMemoryInfoUseCase by lazy { ObserveMemoryInfoUseCase(deviceRepository) }
    override val observeStorageInfoUseCase by lazy { ObserveStorageInfoUseCase(deviceRepository) }
    override val observeBatteryInfoUseCase by lazy { ObserveBatteryInfoUseCase(deviceRepository) }
    override val observeSensorsUseCase by lazy { ObserveSensorsUseCase(deviceRepository) }
    override val observeNetworkInfoUseCase by lazy { ObserveNetworkInfoUseCase(deviceRepository) }
    override val getSystemInfoUseCase by lazy { GetSystemInfoUseCase(deviceRepository) }
    override val updateSettingsUseCase by lazy { UpdateSettingsUseCase(deviceRepository) }
    override val getDiagnosticItemsUseCase by lazy { GetDiagnosticItemsUseCase(deviceRepository) }
    override val runDiagnosticTestUseCase by lazy { RunDiagnosticTestUseCase(deviceRepository) }
}
