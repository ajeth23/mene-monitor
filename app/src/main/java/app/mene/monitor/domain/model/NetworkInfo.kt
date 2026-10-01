package app.mene.monitor.domain.model

data class NetworkInfo(
    val isConnected: Boolean,
    val networkType: String,
    val downloadSpeedBps: Long,
    val uploadSpeedBps: Long,
    val formattedDownloadSpeed: String,
    val formattedUploadSpeed: String,
    val ipAddress: String,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int,
    val signalStrength: String,
    val ssid: String,
    val usageHistory: List<Float>
)
