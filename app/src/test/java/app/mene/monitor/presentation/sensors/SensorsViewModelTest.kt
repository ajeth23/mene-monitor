package app.mene.monitor.presentation.sensors

import app.mene.monitor.domain.model.SensorCategory
import app.mene.monitor.domain.model.SensorInfo
import app.mene.monitor.domain.usecase.ObserveSensorsUseCase
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
class SensorsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeDeviceRepository()

    private val tempSensor = SensorInfo(
        id = 1001,
        name = "CPU Package",
        typeName = "Thermal Zone 0",
        vendor = "SoC Thermal",
        category = SensorCategory.TEMPERATURE,
        valueFormatted = "45°C",
        unit = "°C"
    )

    private val motionSensor = SensorInfo(
        id = 3001,
        name = "Accelerometer",
        typeName = "android.sensor.accelerometer",
        vendor = "Bosch",
        category = SensorCategory.MOTION,
        valueFormatted = "X: 0.1 Y: 9.8 Z: 0.2",
        unit = ""
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository.sensorsToReturn = listOf(tempSensor, motionSensor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all sensors`() = runTest {
        val viewModel = SensorsViewModel(ObserveSensorsUseCase(fakeRepository))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.allSensors.size)
        assertEquals(2, state.filteredSensors.size)
    }

    @Test
    fun `selecting category filters sensor list`() = runTest {
        val viewModel = SensorsViewModel(ObserveSensorsUseCase(fakeRepository))

        testDispatcher.scheduler.advanceUntilIdle()

        // Filter by TEMPERATURE
        viewModel.onAction(SensorsAction.SelectCategory(SensorCategory.TEMPERATURE))
        assertEquals(1, viewModel.uiState.value.filteredSensors.size)
        assertEquals(tempSensor, viewModel.uiState.value.filteredSensors.first())

        // Filter by MOTION
        viewModel.onAction(SensorsAction.SelectCategory(SensorCategory.MOTION))
        assertEquals(1, viewModel.uiState.value.filteredSensors.size)
        assertEquals(motionSensor, viewModel.uiState.value.filteredSensors.first())

        // Reset filter to All
        viewModel.onAction(SensorsAction.SelectCategory(null))
        assertEquals(2, viewModel.uiState.value.filteredSensors.size)
    }
}
