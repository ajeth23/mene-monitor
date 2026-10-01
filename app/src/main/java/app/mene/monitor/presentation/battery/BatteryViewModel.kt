package app.mene.monitor.presentation.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.BatteryInfo
import app.mene.monitor.domain.usecase.ObserveBatteryInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BatteryUiState(
    val isLoading: Boolean = true,
    val battery: BatteryInfo? = null,
    val error: String? = null
)

sealed interface BatteryAction {
    data object Refresh : BatteryAction
}

class BatteryViewModel(
    private val observeBatteryInfoUseCase: ObserveBatteryInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    init {
        observeBattery()
    }

    fun onAction(action: BatteryAction) {
        when (action) {
            BatteryAction.Refresh -> observeBattery()
        }
    }

    private fun observeBattery() {
        viewModelScope.launch {
            observeBatteryInfoUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { bat ->
                    _uiState.update { it.copy(battery = bat, isLoading = false) }
                }
        }
    }

    companion object {
        fun provideFactory(observeBatteryInfoUseCase: ObserveBatteryInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BatteryViewModel(observeBatteryInfoUseCase) as T
                }
            }
    }
}
