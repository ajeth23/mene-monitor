package app.mene.monitor.presentation.home

import app.mene.monitor.domain.usecase.GetDeviceInfoUseCase
import app.mene.monitor.domain.usecase.ObserveBatteryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveCpuInfoUseCase
import app.mene.monitor.domain.usecase.ObserveGpuInfoUseCase
import app.mene.monitor.domain.usecase.ObserveMemoryInfoUseCase
import app.mene.monitor.domain.usecase.ObserveNetworkInfoUseCase
import app.mene.monitor.domain.usecase.ObserveStorageInfoUseCase
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeDeviceRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads device info and receives flow emissions`() = runTest {
        val viewModel = HomeViewModel(
            GetDeviceInfoUseCase(fakeRepository),
            ObserveCpuInfoUseCase(fakeRepository),
            ObserveGpuInfoUseCase(fakeRepository),
            ObserveMemoryInfoUseCase(fakeRepository),
            ObserveStorageInfoUseCase(fakeRepository),
            ObserveBatteryInfoUseCase(fakeRepository),
            ObserveNetworkInfoUseCase(fakeRepository)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(fakeRepository.deviceInfoToReturn, state.device)
        assertEquals(fakeRepository.cpuInfoToReturn, state.cpu)
        assertEquals(fakeRepository.gpuInfoToReturn, state.gpu)
        assertEquals(fakeRepository.memoryInfoToReturn, state.memory)
        assertEquals(fakeRepository.storageInfoToReturn, state.storage)
        assertEquals(fakeRepository.batteryInfoToReturn, state.battery)
        assertEquals(fakeRepository.networkInfoToReturn, state.network)
    }

    @Test
    fun `refresh action reloads device info`() = runTest {
        val viewModel = HomeViewModel(
            GetDeviceInfoUseCase(fakeRepository),
            ObserveCpuInfoUseCase(fakeRepository),
            ObserveGpuInfoUseCase(fakeRepository),
            ObserveMemoryInfoUseCase(fakeRepository),
            ObserveStorageInfoUseCase(fakeRepository),
            ObserveBatteryInfoUseCase(fakeRepository),
            ObserveNetworkInfoUseCase(fakeRepository)
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, fakeRepository.getDeviceInfoCallCount)

        viewModel.onAction(HomeAction.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, fakeRepository.getDeviceInfoCallCount)
    }
}
