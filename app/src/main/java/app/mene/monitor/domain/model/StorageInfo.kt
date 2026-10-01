package app.mene.monitor.domain.model

data class StorageInfo(
    val deviceName: String,
    val mountPoint: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val usagePercentage: Int,
    val fileSystem: String,
    val health: String,
    val temperature: String,
    val isRemovable: Boolean,
    val usageHistory: List<Float>
)
