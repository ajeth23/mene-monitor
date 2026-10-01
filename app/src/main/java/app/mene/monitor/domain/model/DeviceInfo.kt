package app.mene.monitor.domain.model

data class DeviceInfo(
    val deviceName: String,
    val model: String,
    val manufacturer: String,
    val brand: String,
    val board: String,
    val hardware: String,
    val processorName: String,
    val gpuName: String,
    val operatingSystem: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val kernelVersion: String,
    val buildNumber: String,
    val screenResolution: String,
    val screenDensityDpi: Int,
    val refreshRateHz: Float,
    val uptimeFormatted: String
)
