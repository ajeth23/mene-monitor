package app.mene.monitor.presentation.diagnostics

import app.mene.monitor.domain.model.DiagnosticCategory
import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.model.TestStatus
import app.mene.monitor.domain.usecase.GetDiagnosticItemsUseCase
import app.mene.monitor.domain.usecase.RunDiagnosticTestUseCase
import app.mene.monitor.fakes.FakeDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeDeviceRepository()

    private val testItem = DiagnosticItem(
        id = "vibration",
        title = "Vibration Motor",
        description = "Tests haptic feedback motor",
        category = DiagnosticCategory.AUTOMATED,
        status = TestStatus.NOT_TESTED
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository.diagnosticItemsToReturn = listOf(testItem)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads items and calculates report`() = runTest {
        val viewModel = DiagnosticsViewModel(
            GetDiagnosticItemsUseCase(fakeRepository),
            RunDiagnosticTestUseCase(fakeRepository)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(1, state.report.totalTests)
        assertEquals(0, state.report.passedTests)
    }

    @Test
    fun `running single test updates status to passed`() = runTest {
        val viewModel = DiagnosticsViewModel(
            GetDiagnosticItemsUseCase(fakeRepository),
            RunDiagnosticTestUseCase(fakeRepository)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(DiagnosticsAction.RunSingleTest("vibration"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val updatedItem = state.items.first { it.id == "vibration" }
        assertEquals(TestStatus.PASSED, updatedItem.status)
        assertEquals(1, state.report.passedTests)
    }

    @Test
    fun `dismiss manual notice updates state flag`() = runTest {
        val viewModel = DiagnosticsViewModel(
            GetDiagnosticItemsUseCase(fakeRepository),
            RunDiagnosticTestUseCase(fakeRepository)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(DiagnosticsAction.DismissManualTestNotice)
        assertFalse(viewModel.uiState.value.showManualTestNotice)
    }

    @Test
    fun `output remains incomplete until user confirms it`() = runTest {
        fakeRepository.diagnosticResultStatus = TestStatus.NEEDS_CONFIRMATION
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        viewModel.onAction(DiagnosticsAction.RunSingleTest("vibration"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.report.completedTests)
        assertEquals(1, viewModel.uiState.value.report.pendingConfirmationTests)
        viewModel.onAction(DiagnosticsAction.CompleteInteractiveTest("vibration", true))
        assertEquals(1, viewModel.uiState.value.report.passedTests)
        assertEquals(100, viewModel.uiState.value.report.completionPercentage)
    }

    @Test
    fun `negative confirmation records failed observation`() = runTest {
        fakeRepository.diagnosticResultStatus = TestStatus.NEEDS_CONFIRMATION
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        viewModel.onAction(DiagnosticsAction.RunSingleTest("vibration"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(DiagnosticsAction.CompleteInteractiveTest("vibration", false))
        assertEquals(1, viewModel.uiState.value.report.failedTests)
        assertEquals(1, viewModel.uiState.value.report.completedTests)
    }

    @Test
    fun `untested and unavailable checks do not lower passed result percentage`() = runTest {
        fakeRepository.diagnosticItemsToReturn = listOf(
            testItem.copy(status = TestStatus.PASSED),
            testItem.copy(id = "display", isInteractive = true),
            testItem.copy(id = "flashlight", status = TestStatus.UNSUPPORTED),
            testItem.copy(id = "audio", status = TestStatus.PERMISSION_REQUIRED)
        )
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        val report = viewModel.uiState.value.report
        assertEquals(100, report.healthScorePercentage)
        assertEquals(25, report.completionPercentage)
        assertEquals(1, report.unsupportedTests)
        assertEquals(1, report.permissionRequiredTests)
    }

    @Test
    fun `unrun output cannot be confirmed`() = runTest {
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        viewModel.onAction(DiagnosticsAction.CompleteInteractiveTest("vibration", true))
        assertEquals(TestStatus.NOT_TESTED, viewModel.uiState.value.items.single().status)
    }

    @Test
    fun `leaving full screen test does not record failure`() = runTest {
        fakeRepository.diagnosticItemsToReturn = listOf(testItem.copy(id = "display", isInteractive = true))
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        viewModel.onAction(DiagnosticsAction.StartInteractiveTest("display"))
        assertEquals("display", viewModel.uiState.value.activeInteractiveTest)
        viewModel.onAction(DiagnosticsAction.CancelInteractiveTest)
        assertEquals(null, viewModel.uiState.value.activeInteractiveTest)
        assertEquals(TestStatus.NOT_TESTED, viewModel.uiState.value.items.single().status)
    }

    @Test
    fun `notice hides after final manual check and completed tests are excluded`() = runTest {
        fakeRepository.diagnosticItemsToReturn = listOf(
            testItem.copy(id = "display", isInteractive = true),
            testItem.copy(id = "touchscreen", isInteractive = true)
        )
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        viewModel.onAction(DiagnosticsAction.RunFullScan)
        testDispatcher.scheduler.advanceUntilIdle()
        org.junit.Assert.assertTrue(viewModel.uiState.value.showManualTestNotice)
        viewModel.onAction(DiagnosticsAction.CompleteInteractiveTest("display", true))
        assertEquals(listOf("touchscreen"), viewModel.uiState.value.pendingManualItems.map { it.id })
        org.junit.Assert.assertTrue(viewModel.uiState.value.showManualTestNotice)
        viewModel.onAction(DiagnosticsAction.CompleteInteractiveTest("touchscreen", true))
        assertFalse(viewModel.uiState.value.showManualTestNotice)
        viewModel.onAction(DiagnosticsAction.RunFullScan)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showManualTestNotice)
    }

    @Test
    fun `output confirmation hides notice even when user finds an issue`() = runTest {
        fakeRepository.diagnosticResultStatus = TestStatus.NEEDS_CONFIRMATION
        val viewModel = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        viewModel.onAction(DiagnosticsAction.RunFullScan)
        testDispatcher.scheduler.advanceUntilIdle()
        org.junit.Assert.assertTrue(viewModel.uiState.value.showManualTestNotice)
        viewModel.onAction(DiagnosticsAction.CompleteInteractiveTest("vibration", false))
        assertFalse(viewModel.uiState.value.showManualTestNotice)
        assertEquals(TestStatus.FAILED, viewModel.uiState.value.items.single().status)
    }
    @Test
    fun `guided sensor review does not count as passed and closes test`() = runTest {
        fakeRepository.diagnosticItemsToReturn = listOf(testItem.copy(id = "sensors", isInteractive = true))
        val vm = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        vm.onAction(DiagnosticsAction.StartInteractiveTest("sensors"))
        vm.onAction(DiagnosticsAction.CompleteSensorTest(TestStatus.WARNING, "One sensor needs review"))
        assertEquals(TestStatus.WARNING, vm.uiState.value.items.single().status)
        assertEquals(0, vm.uiState.value.report.passedTests)
        assertEquals(null, vm.uiState.value.activeInteractiveTest)
        assertTrue(vm.uiState.value.pendingManualItems.isEmpty())
    }

    @Test
    fun `sensor completion outside active test is ignored`() = runTest {
        fakeRepository.diagnosticItemsToReturn = listOf(testItem.copy(id = "sensors", isInteractive = true))
        val vm = DiagnosticsViewModel(GetDiagnosticItemsUseCase(fakeRepository), RunDiagnosticTestUseCase(fakeRepository))
        vm.onAction(DiagnosticsAction.CompleteSensorTest(TestStatus.PASSED, "Stale result"))
        assertEquals(TestStatus.NOT_TESTED, vm.uiState.value.items.single().status)
    }
}
