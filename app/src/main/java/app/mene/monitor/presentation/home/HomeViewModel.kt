package app.mene.monitor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.BatteryInfo
import app.mene.monitor.domain.model.CpuInfo
import app.mene.monitor.domain.model.DeviceInfo
import app.mene.monitor.domain.model.GpuInfo
import app.mene.monitor.domain.model.MemoryInfo
import app.mene.monitor.domain.model.NetworkInfo
import app.mene.monitor.domain.model.StorageInfo
import app.mene.monitor.domain.usecase.GetDeviceInfoUseCase
import app.mene.monitor.domain.usecase.ObserveBatteryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveCpuInfoUseCase
import app.mene.monitor.domain.usecase.ObserveGpuInfoUseCase
import app.mene.monitor.domain.usecase.ObserveMemoryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveNetworkInfoUseCase
import app.mene.monitor.domain.usecase.ObserveStorageInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val device: DeviceInfo? = null,
    val cpu: CpuInfo? = null,
    val gpu: GpuInfo? = null,
    val memory: MemoryInfo? = null,
    val storage: StorageInfo? = null,
    val battery: BatteryInfo? = null,
    val network: NetworkInfo? = null,
    val error: String? = null
)

sealed interface HomeAction {
    data object Refresh : HomeAction
}

class HomeViewModel(
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val observeCpuInfoUseCase: ObserveCpuInfoUseCase,
    private val observeGpuInfoUseCase: ObserveGpuInfoUseCase,
    private val observeMemoryInfoUseCase: ObserveMemoryInfoUseCase,
    private val observeStorageInfoUseCase: ObserveStorageInfoUseCase,
    private val observeBatteryInfoUseCase: ObserveBatteryInfoUseCase,
    private val observeNetworkInfoUseCase: ObserveNetworkInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
        startObserving()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> {
                loadDeviceInfo()
            }
        }
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            try {
                val info = getDeviceInfoUseCase()
                _uiState.update { it.copy(device = info, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun startObserving() {
        viewModelScope.launch {
            observeCpuInfoUseCase().catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { cpu -> _uiState.update { it.copy(cpu = cpu) } }
        }
        viewModelScope.launch {
            observeGpuInfoUseCase().catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { gpu -> _uiState.update { it.copy(gpu = gpu) } }
        }
        viewModelScope.launch {
            observeMemoryInfoUseCase().catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { mem -> _uiState.update { it.copy(memory = mem) } }
        }
        viewModelScope.launch {
            observeStorageInfoUseCase().catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { st -> _uiState.update { it.copy(storage = st) } }
        }
        viewModelScope.launch {
            observeBatteryInfoUseCase().catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { bat -> _uiState.update { it.copy(battery = bat) } }
        }
        viewModelScope.launch {
            observeNetworkInfoUseCase().catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { net -> _uiState.update { it.copy(network = net) } }
        }
    }

    companion object {
        fun provideFactory(
            getDeviceInfoUseCase: GetDeviceInfoUseCase,
            observeCpuInfoUseCase: ObserveCpuInfoUseCase,
            observeGpuInfoUseCase: ObserveGpuInfoUseCase,
            observeMemoryInfoUseCase: ObserveMemoryInfoUseCase,
            observeStorageInfoUseCase: ObserveStorageInfoUseCase,
            observeBatteryInfoUseCase: ObserveBatteryInfoUseCase,
            observeNetworkInfoUseCase: ObserveNetworkInfoUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    getDeviceInfoUseCase,
                    observeCpuInfoUseCase,
                    observeGpuInfoUseCase,
                    observeMemoryInfoUseCase,
                    observeStorageInfoUseCase,
                    observeBatteryInfoUseCase,
                    observeNetworkInfoUseCase
                ) as T
            }
        }
    }
}
