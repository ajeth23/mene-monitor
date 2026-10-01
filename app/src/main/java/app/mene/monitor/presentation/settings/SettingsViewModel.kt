package app.mene.monitor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.mene.monitor.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import app.mene.monitor.presentation.theme.AppThemeMode

data class SettingsUiState(
    val refreshIntervalSeconds: Long = 2L,
    val useFahrenheit: Boolean = false,
    val isAutoStartEnabled: Boolean = true,
    val isHighPrecisionSensors: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

sealed interface SettingsAction {
    data class SetRefreshInterval(val seconds: Long) : SettingsAction
    data class SetUseFahrenheit(val enabled: Boolean) : SettingsAction
    data class SetAutoStart(val enabled: Boolean) : SettingsAction
    data class SetHighPrecision(val enabled: Boolean) : SettingsAction
    data class SetThemeMode(val themeMode: AppThemeMode) : SettingsAction
}

class SettingsViewModel(
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            refreshIntervalSeconds = updateSettingsUseCase.getRefreshInterval(),
            useFahrenheit = updateSettingsUseCase.isUseFahrenheit(),
            themeMode = updateSettingsUseCase.getThemeMode()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetRefreshInterval -> {
                updateSettingsUseCase.setRefreshInterval(action.seconds)
                _uiState.update { it.copy(refreshIntervalSeconds = action.seconds) }
            }
            is SettingsAction.SetUseFahrenheit -> {
                updateSettingsUseCase.setUseFahrenheit(action.enabled)
                _uiState.update { it.copy(useFahrenheit = action.enabled) }
            }
            is SettingsAction.SetAutoStart -> {
                _uiState.update { it.copy(isAutoStartEnabled = action.enabled) }
            }
            is SettingsAction.SetHighPrecision -> {
                _uiState.update { it.copy(isHighPrecisionSensors = action.enabled) }
            }
            is SettingsAction.SetThemeMode -> {
                updateSettingsUseCase.setThemeMode(action.themeMode)
                _uiState.update { it.copy(themeMode = action.themeMode) }
            }
        }
    }

    companion object {
        fun provideFactory(updateSettingsUseCase: UpdateSettingsUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(updateSettingsUseCase) as T
                }
            }
    }
}
