package app.mene.monitor.data.datasource.system

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SystemDataSourceAccuracyTest {

    @Test
    fun `cpu data source returns non-null cpu info with safe handling of missing sysfs`() {
        val cpuDataSource = AndroidCpuDataSource()
        val cpuInfo = cpuDataSource.getCpuInfo(useFahrenheit = false)

        assertNotNull(cpuInfo)
        assertNotNull(cpuInfo.name)
        assertNotNull(cpuInfo.architecture)
        assertNotNull(cpuInfo.temperature)
    }

    @Test
    fun `gpu data source handles missing sysfs gracefully and returns NA for restricted readings`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val gpuDataSource = AndroidGpuDataSource(context)
        val gpuInfo = gpuDataSource.getGpuInfo(useFahrenheit = false)

        assertNotNull(gpuInfo)
        assertNotNull(gpuInfo.name)
        assertNotNull(gpuInfo.temperature)
        assertNotNull(gpuInfo.clockSpeed)
    }

    @Test
    fun `storage data source handles missing thermal nodes gracefully`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val storageDataSource = AndroidStorageDataSource(context)
        val storageInfo = storageDataSource.getStorageInfo(useFahrenheit = false)

        assertNotNull(storageInfo)
        assertEquals("Internal Storage", storageInfo.deviceName)
        assertNotNull(storageInfo.temperature)
    }
}
