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
    val usageHistory: List<Float>
)
