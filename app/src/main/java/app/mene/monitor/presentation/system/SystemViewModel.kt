package app.mene.monitor.presentation.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.SystemInfo
import app.mene.monitor.domain.usecase.GetSystemInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SystemUiState(
    val isLoading: Boolean = true,
    val systemInfo: SystemInfo? = null,
    val error: String? = null
)

sealed interface SystemAction {
    data object Refresh : SystemAction
}

class SystemViewModel(
    private val getSystemInfoUseCase: GetSystemInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemUiState())
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    init {
        loadSystemInfo()
    }

    fun onAction(action: SystemAction) {
        when (action) {
            SystemAction.Refresh -> loadSystemInfo()
        }
    }

    private fun loadSystemInfo() {
        viewModelScope.launch {
            try {
                val info = getSystemInfoUseCase()
                _uiState.update { it.copy(systemInfo = info, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun provideFactory(getSystemInfoUseCase: GetSystemInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SystemViewModel(getSystemInfoUseCase) as T
                }
            }
    }
}
