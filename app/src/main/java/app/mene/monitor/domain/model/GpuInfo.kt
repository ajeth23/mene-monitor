package app.mene.monitor.domain.model

data class GpuInfo(
    val name: String,
    val vendor: String,
    val usagePercentage: Int,
    val temperature: String,
    val clockSpeed: String,
    val vram: String,
    val driverVersion: String,
    val apiVersion: String,
    val busInfo: String,
    val usageHistory: List<Float>
)
