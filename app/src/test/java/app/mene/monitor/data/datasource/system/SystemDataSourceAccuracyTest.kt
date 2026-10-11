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

    @Test
    fun `battery does not invent capacity or health when device nodes are unavailable`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val battery = AndroidBatteryDataSource(context, readCapacity = { null }).getBatteryInfo()
        org.junit.Assert.assertNull(battery.designCapacityMah)
        org.junit.Assert.assertNull(battery.fullChargeCapacityMah)
        org.junit.Assert.assertNull(battery.healthPercentage)
        assertEquals("Unavailable", battery.healthLabel)
        org.junit.Assert.assertTrue(battery.usageHistory.size <= 1)
    }

    @Test
    fun `flashlight does not require camera permission`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = org.robolectric.Shadows.shadowOf(context.packageManager)
        packageManager.setSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_FLASH, true)
        org.junit.Assert.assertNull(AndroidDiagnosticsDataSource(context).outputAvailability("flashlight"))
    }

    @Test
    fun `memory history preserves changes smaller than one percent`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val shadow = org.robolectric.Shadows.shadowOf(manager)
        val source = AndroidMemoryDataSource(context)
        shadow.setMemoryInfo(android.app.ActivityManager.MemoryInfo().apply {
            totalMem = 1000000000L
            availMem = 501000000L
        })
        val first = source.getMemoryInfo()
        shadow.setMemoryInfo(android.app.ActivityManager.MemoryInfo().apply {
            totalMem = 1000000000L
            availMem = 499000000L
            lowMemory = true
        })
        val second = source.getMemoryInfo()
        assertEquals(50, first.usagePercentage)
        assertEquals(50, second.usagePercentage)
        assertEquals(49.9f, second.usageHistory[0], 0.001f)
        assertEquals(50.1f, second.usageHistory[1], 0.001f)
        org.junit.Assert.assertTrue(second.isLowMemory)
        assertEquals("Unavailable", second.memoryType)
        assertEquals("Unavailable", second.memorySpeed)
    }

    @Test
    fun `missing memory totals do not invent a reading or history`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        org.robolectric.Shadows.shadowOf(manager).setMemoryInfo(android.app.ActivityManager.MemoryInfo())
        val reading = AndroidMemoryDataSource(context).getMemoryInfo()
        org.junit.Assert.assertFalse(reading.hasReading)
        org.junit.Assert.assertNull(reading.preciseUsagePercentage)
        org.junit.Assert.assertTrue(reading.usageHistory.isEmpty())
    }
}
