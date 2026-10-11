package app.mene.monitor.domain.model

data class BatteryInfo(
    val percentage: Int,
    val chargingState: String,
    val isCharging: Boolean,
    val health: String,
    val temperature: String,
    val voltageFormatted: String,
    val voltageMv: Int?,
    val currentMa: Int?,
    val capacityMah: Int?,
    val designCapacityMah: Int?,
    val designCapacityWh: String,
    val fullChargeCapacityWh: String,
    val powerSource: String,
    val technology: String,
    val estimatedTimeRemaining: String,
    val usageHistory: List<Float>,
    val fullChargeCapacityMah: Int? = null,
    val healthPercentage: Int? = null,
    val usageTimestamps: List<Long> = emptyList()
) {
    val chargeLabel: String get() = if (percentage in 0..100) "$percentage%" else "Unavailable"
    val healthLabel: String get() = healthPercentage?.let { "$it% (estimated)" } ?: "Unavailable"
}
