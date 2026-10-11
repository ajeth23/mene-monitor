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
import kotlinx.coroutines.CancellationException

data class DiagnosticsUiState(
    val isRunningFullScan: Boolean = false,
    val items: List<DiagnosticItem> = emptyList(),
    val report: DiagnosticsReport = DiagnosticsReport(),
    val activeInteractiveTest: String? = null,
    val showManualTestNotice: Boolean = false,
    val currentRunningTestId: String? = null,
    val currentProgressIndex: Int = 0,
    val totalAutomatedTests: Int = 0
) {
    val pendingManualItems: List<DiagnosticItem> get() = items.filter { it.needsManualVerification() }
}

private fun DiagnosticItem.needsManualVerification(): Boolean =
    status == TestStatus.NEEDS_CONFIRMATION ||
        (isInteractive && status in setOf(TestStatus.NOT_TESTED, TestStatus.RUNNING))

sealed interface DiagnosticsAction {
    data object RunFullScan : DiagnosticsAction
    data class RunSingleTest(val id: String) : DiagnosticsAction
    data class StartInteractiveTest(val id: String) : DiagnosticsAction
    data class CompleteInteractiveTest(val id: String, val passed: Boolean) : DiagnosticsAction
    data class CompleteSensorTest(val status: TestStatus, val detail: String) : DiagnosticsAction
    data object CancelInteractiveTest : DiagnosticsAction
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
                if (!_uiState.value.isRunningFullScan && _uiState.value.currentRunningTestId == null && _uiState.value.items.none { it.status == TestStatus.RUNNING } &&
                    _uiState.value.items.any { it.id == action.id && it.isInteractive }) {
                    _uiState.update { it.copy(activeInteractiveTest = action.id) }
                }
            }
            is DiagnosticsAction.CompleteInteractiveTest -> {
                completeInteractiveTest(action.id, action.passed)
            }
            is DiagnosticsAction.CompleteSensorTest -> completeSensorTest(action.status, action.detail)
            DiagnosticsAction.CancelInteractiveTest -> {
                _uiState.update { it.copy(activeInteractiveTest = null) }
            }
            DiagnosticsAction.DismissManualTestNotice -> {
                _uiState.update { it.copy(showManualTestNotice = false) }
            }
        }
    }

    private fun runFullScan() {
        if (_uiState.value.isRunningFullScan || _uiState.value.currentRunningTestId != null || _uiState.value.items.any { it.status == TestStatus.RUNNING }) return
        _uiState.update { it.copy(isRunningFullScan = true) }
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

                    kotlinx.coroutines.delay(500L)

                    val tested = runSafely(item.id)
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
                    showManualTestNotice = updatedList.any { item -> item.needsManualVerification() }
                )
            }
        }
    }

    private fun runSingleTest(id: String) {
        if (_uiState.value.items.none { it.id == id && !it.isInteractive }) return
        if (_uiState.value.isRunningFullScan || _uiState.value.currentRunningTestId != null || _uiState.value.items.any { it.status == TestStatus.RUNNING }) return
        _uiState.update { it.copy(currentRunningTestId = id) }
        viewModelScope.launch {
            val list = _uiState.value.items.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index != -1) {
                list[index] = list[index].copy(status = TestStatus.RUNNING)
                _uiState.update { it.copy(items = list.toList()) }

                kotlinx.coroutines.delay(600L)

                val tested = runSafely(id)
                list[index] = tested
                _uiState.update {
                    it.copy(
                        items = list.toList(),
                        report = calculateReport(list),
                        showManualTestNotice = it.showManualTestNotice && list.any { item -> item.needsManualVerification() },
                        currentRunningTestId = null
                    )
                }
            }
        }
    }

    private fun completeInteractiveTest(id: String, passed: Boolean) {
        val list = _uiState.value.items.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && !_uiState.value.isRunningFullScan && _uiState.value.currentRunningTestId == null &&
            (list[index].isInteractive || list[index].status == TestStatus.NEEDS_CONFIRMATION)) {
            list[index] = list[index].copy(
                status = if (passed) TestStatus.PASSED else TestStatus.FAILED,
                detailMessage = if (passed) "User confirmed the expected result" else "User did not observe the expected result",
                nextStep = if (passed) null else list[index].nextStep ?: "Repeat the test. If the issue persists, consider having the device inspected."
            )
            _uiState.update {
                it.copy(
                    items = list.toList(),
                    report = calculateReport(list),
                    showManualTestNotice = it.showManualTestNotice && list.any { item -> item.needsManualVerification() },
                    activeInteractiveTest = null
                )
            }
        }
    }

    private fun completeSensorTest(status: TestStatus, detail: String) {
        if (_uiState.value.activeInteractiveTest != "sensors" || status !in
            setOf(TestStatus.PASSED, TestStatus.FAILED, TestStatus.WARNING, TestStatus.UNSUPPORTED)) return
        _uiState.update { state ->
            val items = state.items.map { item ->
                if (item.id == "sensors") item.copy(status = status, detailMessage = detail,
                    nextStep = if (status == TestStatus.WARNING || status == TestStatus.FAILED)
                        "Retry in the foreground with sensor access enabled. No response alone does not establish hardware failure." else null)
                else item
            }
            state.copy(items = items, report = calculateReport(items), activeInteractiveTest = null,
                showManualTestNotice = state.showManualTestNotice && items.any { it.needsManualVerification() })
        }
    }

    private suspend fun runSafely(id: String): DiagnosticItem {
        return try {
            runDiagnosticTestUseCase(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value.items.first { it.id == id }.copy(
                status = TestStatus.WARNING,
                detailMessage = "The check could not finish",
                nextStep = "Retry the check. An interrupted check does not confirm hardware failure."
            )
        }
    }

    private fun calculateReport(items: List<DiagnosticItem>): DiagnosticsReport {
        val total = items.size
        val passed = items.count { it.status == TestStatus.PASSED }
        val completed = items.count { it.status in setOf(TestStatus.PASSED, TestStatus.FAILED, TestStatus.WARNING) }
        val score = if (completed > 0) (passed * 100) / completed else 0
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
