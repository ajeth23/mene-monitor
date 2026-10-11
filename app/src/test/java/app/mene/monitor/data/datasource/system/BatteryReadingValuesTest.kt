package app.mene.monitor.data.datasource.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryReadingValuesTest {
    @Test fun `charge respects broadcast scale including empty battery`() {
        assertEquals(50, BatteryReadingValues.chargePercentage(100, 200))
        assertEquals(0, BatteryReadingValues.chargePercentage(0, 100))
        assertNull(BatteryReadingValues.chargePercentage(-1, 100))
        assertNull(BatteryReadingValues.chargePercentage(50, 0))
        assertNull(BatteryReadingValues.chargePercentage(101, 100))
    }
    @Test fun `health uses full capacity rather than remaining charge`() {
        assertEquals(80, BatteryReadingValues.healthPercentage(4000000, 5000000))
        assertEquals(100, BatteryReadingValues.healthPercentage(5100000, 5000000))
    }
    @Test fun `unavailable or incompatible capacities cannot produce health percentage`() {
        assertNull(BatteryReadingValues.healthPercentage(null, 5000000))
        assertNull(BatteryReadingValues.healthPercentage(4000000, null))
        assertNull(BatteryReadingValues.healthPercentage(4000000, 0))
        assertNull(BatteryReadingValues.healthPercentage(4000, 5000000))
        assertNull(BatteryReadingValues.healthPercentage(4000000, 5000))
    }
}
