package app.mene.monitor.presentation.diagnostics.tests

import org.junit.Assert.*
import org.junit.Test

class SensorResponseTrackerTest {
    @Test fun `unchanged samples do not establish responsiveness`() {
        val tracker = SensorResponseTracker(0.7f)
        repeat(5) { tracker.record(floatArrayOf(0f, 0f, 9.8f)) }
        assertTrue(tracker.hasReading)
        assertFalse(tracker.responded)
    }
    @Test fun `invalid samples are ignored`() {
        val tracker = SensorResponseTracker(1f)
        tracker.record(floatArrayOf(Float.NaN))
        tracker.record(floatArrayOf(Float.POSITIVE_INFINITY))
        tracker.record(floatArrayOf())
        assertFalse(tracker.hasReading)
        assertFalse(tracker.responded)
    }
    @Test fun `response latches after meaningful change on any axis`() {
        val tracker = SensorResponseTracker(0.7f)
        tracker.record(floatArrayOf(0f, 0f, 9.8f))
        tracker.record(floatArrayOf(0f, 1f, 9.8f))
        tracker.record(floatArrayOf(0f, 0f, 9.8f))
        assertTrue(tracker.responded)
    }
    @Test fun `small noise and mismatched dimensions do not pass`() {
        val tracker = SensorResponseTracker(0.7f)
        tracker.record(floatArrayOf(0f, 0f, 9.8f))
        tracker.record(floatArrayOf(0.01f, 0.02f, 9.81f))
        tracker.record(floatArrayOf(3f))
        assertFalse(tracker.responded)
    }
}
