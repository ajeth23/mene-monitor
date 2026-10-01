package app.mene.monitor.presentation.cpu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.CpuInfo
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
    val error: String? = null
)

sealed interface CpuAction {
    data object Refresh : CpuAction
}

class CpuViewModel(
    private val observeCpuInfoUseCase: ObserveCpuInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CpuUiState())
    val uiState: StateFlow<CpuUiState> = _uiState.asStateFlow()

    init {
        observeCpu()
    }

    fun onAction(action: CpuAction) {
        when (action) {
            CpuAction.Refresh -> observeCpu()
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

    companion object {
        fun provideFactory(observeCpuInfoUseCase: ObserveCpuInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CpuViewModel(observeCpuInfoUseCase) as T
                }
            }
    }
}
