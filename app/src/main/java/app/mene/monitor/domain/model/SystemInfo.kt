package app.mene.monitor.domain.model

data class SystemInfo(
    val device: DeviceInfo,
    val supportedAbis: List<String>,
    val javaVm: String,
    val openGlVersion: String,
    val sensorCount: Int,
    val bootloader: String,
    val radioVersion: String,
    val fingerprint: String
)
