package app.mene.monitor.presentation.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.NetworkInfo
import app.mene.monitor.domain.usecase.ObserveNetworkInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NetworkUiState(
    val isLoading: Boolean = true,
    val network: NetworkInfo? = null,
    val error: String? = null
)

sealed interface NetworkAction {
    data object Refresh : NetworkAction
}

class NetworkViewModel(
    private val observeNetworkInfoUseCase: ObserveNetworkInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
    }

    fun onAction(action: NetworkAction) {
        when (action) {
            NetworkAction.Refresh -> observeNetwork()
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            observeNetworkInfoUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { net ->
                    _uiState.update { it.copy(network = net, isLoading = false) }
                }
        }
    }

    companion object {
        fun provideFactory(observeNetworkInfoUseCase: ObserveNetworkInfoUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NetworkViewModel(observeNetworkInfoUseCase) as T
                }
            }
    }
}
