package app.mene.monitor.data.repository

import app.mene.monitor.fakes.FakeDeviceRepository
import app.mene.monitor.presentation.theme.AppThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceRepositoryTest {

    private val repository = FakeDeviceRepository()

    @Test
    fun `set and get refresh interval works correctly`() = runTest {
        repository.setRefreshInterval(5L)
        assertEquals(5L, repository.getRefreshInterval())
    }

    @Test
    fun `set and get temperature scale works correctly`() = runTest {
        repository.setUseFahrenheit(true)
        assertTrue(repository.isUseFahrenheit())
    }

    @Test
    fun `set and get theme mode works correctly`() = runTest {
        repository.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, repository.getThemeMode())
    }
}
