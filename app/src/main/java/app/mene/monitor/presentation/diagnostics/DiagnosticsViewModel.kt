package app.mene.monitor.presentation.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.model.DiagnosticsReport
import app.mene.monitor.domain.model.TestStatus
import app.mene.monitor.domain.usecase.GetDiagnosticItemsUseCase
import app.mene.monitor.domain.usecase.RunDiagnosticTestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val isRunningFullScan: Boolean = false,
    val items: List<DiagnosticItem> = emptyList(),
    val report: DiagnosticsReport = DiagnosticsReport(),
    val activeInteractiveTest: String? = null,
    val showManualTestNotice: Boolean = false,
    val currentRunningTestId: String? = null,
    val currentProgressIndex: Int = 0,
    val totalAutomatedTests: Int = 0
)

sealed interface DiagnosticsAction {
    data object RunFullScan : DiagnosticsAction
    data class RunSingleTest(val id: String) : DiagnosticsAction
    data class StartInteractiveTest(val id: String) : DiagnosticsAction
    data class CompleteInteractiveTest(val id: String, val passed: Boolean) : DiagnosticsAction
    data object DismissManualTestNotice : DiagnosticsAction
}

class DiagnosticsViewModel(
    private val getDiagnosticItemsUseCase: GetDiagnosticItemsUseCase,
    private val runDiagnosticTestUseCase: RunDiagnosticTestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        val initialItems = getDiagnosticItemsUseCase()
        _uiState.update {
            it.copy(
                items = initialItems,
                report = calculateReport(initialItems)
            )
        }
    }

    fun onAction(action: DiagnosticsAction) {
        when (action) {
            DiagnosticsAction.RunFullScan -> runFullScan()
            is DiagnosticsAction.RunSingleTest -> runSingleTest(action.id)
            is DiagnosticsAction.StartInteractiveTest -> {
                _uiState.update { it.copy(activeInteractiveTest = action.id) }
            }
            is DiagnosticsAction.CompleteInteractiveTest -> {
                completeInteractiveTest(action.id, action.passed)
            }
            DiagnosticsAction.DismissManualTestNotice -> {
                _uiState.update { it.copy(showManualTestNotice = false) }
            }
        }
    }

    private fun runFullScan() {
        viewModelScope.launch {
            val nonInteractiveItems = _uiState.value.items.filter { !it.isInteractive }
            _uiState.update {
                it.copy(
                    isRunningFullScan = true,
                    showManualTestNotice = false,
                    totalAutomatedTests = nonInteractiveItems.size,
                    currentProgressIndex = 0
                )
            }
            val updatedList = _uiState.value.items.toMutableList()

            var progressCount = 0
            for (i in updatedList.indices) {
                val item = updatedList[i]
                if (!item.isInteractive) {
                    progressCount++
                    updatedList[i] = item.copy(status = TestStatus.RUNNING)
                    _uiState.update {
                        it.copy(
                            items = updatedList.toList(),
                            report = calculateReport(updatedList),
                            currentRunningTestId = item.id,
                            currentProgressIndex = progressCount
                        )
                    }

                    kotlinx.coroutines.delay(450L)
                    val tested = runDiagnosticTestUseCase(item.id)
                    updatedList[i] = tested
                    _uiState.update {
                        it.copy(
                            items = updatedList.toList(),
                            report = calculateReport(updatedList)
                        )
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isRunningFullScan = false,
                    currentRunningTestId = null,
                    showManualTestNotice = true
                )
            }
        }
    }

    private fun runSingleTest(id: String) {
        viewModelScope.launch {
            val list = _uiState.value.items.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index != -1) {
                list[index] = list[index].copy(status = TestStatus.RUNNING)
                _uiState.update { it.copy(items = list.toList()) }

                val tested = runDiagnosticTestUseCase(id)
                list[index] = tested
                _uiState.update {
                    it.copy(
                        items = list.toList(),
                        report = calculateReport(list)
                    )
                }
            }
        }
    }

    private fun completeInteractiveTest(id: String, passed: Boolean) {
        val list = _uiState.value.items.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            list[index] = list[index].copy(
                status = if (passed) TestStatus.PASSED else TestStatus.FAILED,
                detailMessage = if (passed) "User confirmed test passed" else "User marked test as failed"
            )
            _uiState.update {
                it.copy(
                    items = list.toList(),
                    report = calculateReport(list),
                    activeInteractiveTest = null
                )
            }
        }
    }

    private fun calculateReport(items: List<DiagnosticItem>): DiagnosticsReport {
        val total = items.size
        val passed = items.count { it.status == TestStatus.PASSED }
        val score = if (total > 0) (passed * 100) / total else 100
        return DiagnosticsReport(
            healthScorePercentage = score,
            totalTests = total,
            passedTests = passed,
            items = items
        )
    }

    companion object {
        fun provideFactory(
            getDiagnosticItemsUseCase: GetDiagnosticItemsUseCase,
            runDiagnosticTestUseCase: RunDiagnosticTestUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiagnosticsViewModel(
                    getDiagnosticItemsUseCase,
                    runDiagnosticTestUseCase
                ) as T
            }
        }
    }
}
