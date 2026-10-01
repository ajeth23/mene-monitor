package app.mene.monitor.presentation.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.MemoryInfo
import app.mene.monitor.domain.usecase.ObserveMemoryInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryUiState(
    val isLoading: Boolean = true,
    val memory: MemoryInfo? = null,
    val error: String? = null
)

sealed interface MemoryAction {
    data object Refresh : MemoryAction
}

class MemoryViewModel(
    private val observeMemoryInfoUseCase: ObserveMemoryInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        observeMemory()
    }

    fun onAction(action: MemoryAction) {
        when (action) {
            MemoryAction.Refresh -> observeMemory()
        }
    }

    private fun observeMemory() {
        viewModelScope.launch {
            observeMemoryInfoUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { memInfo ->
                    _uiState.update { it.copy(memory = memInfo, isLoading = false) }
                }
        }
    }

    companion object {
        fun provideFactory(observeMemoryInfoUseCase: ObserveMemoryInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MemoryViewModel(observeMemoryInfoUseCase) as T
                }
            }
    }
}
