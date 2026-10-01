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
}
