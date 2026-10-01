package app.mene.monitor.presentation.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.SensorCategory
import app.mene.monitor.domain.model.SensorInfo
import app.mene.monitor.domain.usecase.ObserveSensorsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensorsUiState(
    val isLoading: Boolean = true,
    val allSensors: List<SensorInfo> = emptyList(),
    val selectedCategory: SensorCategory? = null,
    val error: String? = null
) {
    val filteredSensors: List<SensorInfo>
        get() = if (selectedCategory == null) {
            allSensors
        } else {
            allSensors.filter { it.category == selectedCategory }
        }
}

sealed interface SensorsAction {
    data class SelectCategory(val category: SensorCategory?) : SensorsAction
    data object Refresh : SensorsAction
}

class SensorsViewModel(
    private val observeSensorsUseCase: ObserveSensorsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorsUiState())
    val uiState: StateFlow<SensorsUiState> = _uiState.asStateFlow()

    init {
        observeSensors()
    }

    fun onAction(action: SensorsAction) {
        when (action) {
            is SensorsAction.SelectCategory -> {
                _uiState.update { it.copy(selectedCategory = action.category) }
            }
            SensorsAction.Refresh -> observeSensors()
        }
    }

    private fun observeSensors() {
        viewModelScope.launch {
            observeSensorsUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { list ->
                    _uiState.update { it.copy(allSensors = list, isLoading = false) }
                }
        }
    }

    companion object {
        fun provideFactory(observeSensorsUseCase: ObserveSensorsUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SensorsViewModel(observeSensorsUseCase) as T
                }
            }
    }
}
