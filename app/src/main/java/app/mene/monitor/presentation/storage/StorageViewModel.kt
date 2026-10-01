package app.mene.monitor.presentation.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.StorageInfo
import app.mene.monitor.domain.usecase.ObserveStorageInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StorageUiState(
    val isLoading: Boolean = true,
    val storage: StorageInfo? = null,
    val error: String? = null
)

sealed interface StorageAction {
    data object Refresh : StorageAction
}

class StorageViewModel(
    private val observeStorageInfoUseCase: ObserveStorageInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init {
        observeStorage()
    }

    fun onAction(action: StorageAction) {
        when (action) {
            StorageAction.Refresh -> observeStorage()
        }
    }

    private fun observeStorage() {
        viewModelScope.launch {
            observeStorageInfoUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { st ->
                    _uiState.update { it.copy(storage = st, isLoading = false) }
                }
        }
    }

    companion object {
        fun provideFactory(observeStorageInfoUseCase: ObserveStorageInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StorageViewModel(observeStorageInfoUseCase) as T
                }
            }
    }
}
