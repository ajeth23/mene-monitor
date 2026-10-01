package app.mene.monitor.presentation.gpu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.GpuInfo
import app.mene.monitor.domain.usecase.ObserveGpuInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GpuUiState(
    val isLoading: Boolean = true,
    val gpu: GpuInfo? = null,
    val error: String? = null
)

sealed interface GpuAction {
    data object Refresh : GpuAction
}

class GpuViewModel(
    private val observeGpuInfoUseCase: ObserveGpuInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GpuUiState())
    val uiState: StateFlow<GpuUiState> = _uiState.asStateFlow()

    init {
        observeGpu()
    }

    fun onAction(action: GpuAction) {
        when (action) {
            GpuAction.Refresh -> observeGpu()
        }
    }

    private fun observeGpu() {
        viewModelScope.launch {
            observeGpuInfoUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { gpuInfo ->
                    _uiState.update { it.copy(gpu = gpuInfo, isLoading = false) }
                }
        }
    }

    companion object {
        fun provideFactory(observeGpuInfoUseCase: ObserveGpuInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GpuViewModel(observeGpuInfoUseCase) as T
                }
            }
    }
}
