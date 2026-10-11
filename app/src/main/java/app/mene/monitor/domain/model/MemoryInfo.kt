package app.mene.monitor.domain.model

data class MemoryInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val usagePercentage: Int,
    val memoryType: String,
    val memorySpeed: String,
    val swapTotalBytes: Long,
    val swapUsedBytes: Long,
    val isLowMemory: Boolean,
    val usageTimestamps: List<Long> = emptyList(),
    val usageHistory: List<Float>
) {
    val hasReading: Boolean get() = totalBytes > 0
    val preciseUsagePercentage: Float? get() = if (hasReading)
        (usedBytes.toDouble() / totalBytes * 100).toFloat().coerceIn(0f, 100f) else null
}
