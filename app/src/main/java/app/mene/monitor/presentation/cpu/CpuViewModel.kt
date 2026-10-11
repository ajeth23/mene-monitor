package app.mene.monitor.presentation.cpu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.CpuInfo
import app.mene.monitor.domain.usecase.ObserveBatteryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveCpuInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CpuUiState(
    val isLoading: Boolean = true,
    val cpu: CpuInfo? = null,
    val batteryTemperature: String? = null,
    val error: String? = null
)

sealed interface CpuAction {
    data object Refresh : CpuAction
}

class CpuViewModel(
    private val observeCpuInfoUseCase: ObserveCpuInfoUseCase,
    private val observeBatteryInfoUseCase: ObserveBatteryInfoUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CpuUiState())
    val uiState: StateFlow<CpuUiState> = _uiState.asStateFlow()

    init {
        observeCpu()
        observeBattery()
    }

    fun onAction(action: CpuAction) {
        when (action) {
            CpuAction.Refresh -> {
                observeCpu()
                observeBattery()
            }
        }
    }

    private fun observeCpu() {
        viewModelScope.launch {
            observeCpuInfoUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { cpuInfo ->
                    _uiState.update { it.copy(cpu = cpuInfo, isLoading = false) }
                }
        }
    }

    private fun observeBattery() {
        observeBatteryInfoUseCase?.let { useCase ->
            viewModelScope.launch {
                useCase()
                    .catch { }
                    .collect { batteryInfo ->
                        _uiState.update { it.copy(batteryTemperature = batteryInfo.temperature) }
                    }
            }
        }
    }

    companion object {
        fun provideFactory(
            observeCpuInfoUseCase: ObserveCpuInfoUseCase,
            observeBatteryInfoUseCase: ObserveBatteryInfoUseCase? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CpuViewModel(observeCpuInfoUseCase, observeBatteryInfoUseCase) as T
                }
            }
    }
}
