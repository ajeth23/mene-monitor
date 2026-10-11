package app.mene.monitor.presentation.diagnostics.tests

/** Checks responsiveness, not calibration or accuracy. Invalid samples never count as a response. */
internal class SensorResponseTracker(private val minimumChange: Float) {
    private var baseline: FloatArray? = null
    var hasReading: Boolean = false
        private set
    var responded: Boolean = false
        private set

    fun record(values: FloatArray) {
        if (values.isEmpty() || values.any { !it.isFinite() }) return
        hasReading = true
        val first = baseline
        if (first == null) {
            baseline = values.copyOf()
        } else if (values.size == first.size && values.indices.any { kotlin.math.abs(values[it] - first[it]) >= minimumChange }) {
            responded = true
        }
    }
}
