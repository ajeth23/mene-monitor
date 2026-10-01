package app.mene.monitor.domain.model

data class CoreInfo(
    val index: Int,
    val usagePercentage: Int,
    val frequencyMhz: Int,
    val formattedFrequency: String
)

data class CpuInfo(
    val name: String,
    val manufacturer: String,
    val architecture: String,
    val coreCount: Int,
    val threadCount: Int,
    val baseFrequency: String,
    val currentFrequency: String,
    val usagePercentage: Int,
    val temperature: String,
    val cores: List<CoreInfo>,
    val l1Cache: String,
    val l2Cache: String,
    val l3Cache: String,
    val governor: String,
    val tdp: String,
    val processNode: String,
    val usageHistory: List<Float>
)
