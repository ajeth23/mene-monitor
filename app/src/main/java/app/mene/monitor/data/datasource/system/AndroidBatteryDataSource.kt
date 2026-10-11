package app.mene.monitor.data.datasource.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.BatteryInfo
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

class AndroidBatteryDataSource(
    private val context: Context,
    private val readCapacity: (String) -> Long? = { name ->
        try { File("/sys/class/power_supply/battery/$name").readText().trim().toLongOrNull() }
        catch (_: Exception) { null }
    }
) {
    private val usageHistory = ArrayDeque<Float>()
    private val usageTimestamps = ArrayDeque<Long>()

    fun getBatteryInfo(useFahrenheit: Boolean = false): BatteryInfo {
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        fun property(id: Int): Int? = try {
            manager?.getIntProperty(id)?.takeUnless { it == Int.MIN_VALUE }
        } catch (_: Exception) { null }
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = BatteryReadingValues.chargePercentage(level, scale)
            ?: property(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 } ?: -1
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val chargingState = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }
        val powerSource = when (battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            0 -> "Battery"
            else -> "Unknown"
        }
        val health = when (battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }
        val voltageMv = battery?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 }
        val voltage = voltageMv?.let { String.format(Locale.getDefault(), "%.2f V", it / 1000.0) } ?: "Unavailable"
        val temperatureRaw = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val temperature = temperatureRaw?.takeUnless { it == Int.MIN_VALUE }?.let {
            Formatters.formatTemperature(it / 10f, useFahrenheit)
        } ?: "Unavailable"
        val currentMa = property(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.let { it / 1000 }
        val remainingMah = property(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)?.takeIf { it >= 0 }?.let { it / 1000 }
        // Standard kernel charge attributes are in microamp-hours. Read the matched pair from one supply.
        val fullUah = readCapacity("charge_full")?.takeIf { it > 0 }
        val designUah = readCapacity("charge_full_design")?.takeIf { it > 0 }
        val fullMah = fullUah?.let { (it / 1000).takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt() }
        val designMah = designUah?.let { (it / 1000).takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt() }
        val healthPercentage = BatteryReadingValues.healthPercentage(fullUah, designUah)
        val timeToFull = if (isCharging && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try { manager?.computeChargeTimeRemaining()?.takeIf { it > 0 } } catch (_: Exception) { null }
        } else null
        val timeRemaining = when {
            status == BatteryManager.BATTERY_STATUS_FULL -> "Fully charged"
            timeToFull != null -> "~${(timeToFull / 60000.0).roundToInt().coerceAtLeast(1)} min to full (Android estimate)"
            else -> "Time estimate unavailable"
        }
        val history: List<Float>
        val timestamps: List<Long>
        synchronized(usageHistory) {
            if (percentage >= 0) {
                if (usageHistory.size >= 60) { usageHistory.removeFirst(); usageTimestamps.removeFirst() }
                usageHistory.addLast(percentage.toFloat())
                usageTimestamps.addLast(SystemClock.elapsedRealtime())
            }
            history = usageHistory.toList()
            timestamps = usageTimestamps.toList()
        }
        return BatteryInfo(
            percentage = percentage, chargingState = chargingState, isCharging = isCharging,
            health = health, temperature = temperature, voltageFormatted = voltage, voltageMv = voltageMv,
            currentMa = currentMa, capacityMah = remainingMah, designCapacityMah = designMah,
            designCapacityWh = "Unavailable", fullChargeCapacityWh = "Unavailable",
            powerSource = powerSource, technology = battery?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown",
            estimatedTimeRemaining = timeRemaining, usageHistory = history, usageTimestamps = timestamps,
            fullChargeCapacityMah = fullMah, healthPercentage = healthPercentage
        )
    }
}

internal object BatteryReadingValues {
    fun chargePercentage(level: Int, scale: Int): Int? =
        if (scale > 0 && level in 0..scale) (level.toDouble() * 100 / scale).roundToInt() else null

    fun healthPercentage(fullUah: Long?, designUah: Long?): Int? {
        if (fullUah == null || designUah == null || fullUah <= 0 || designUah <= 0) return null
        val ratio = fullUah.toDouble() / designUah
        // Reject incompatible or clearly invalid capacity pairs; new batteries can slightly exceed design capacity.
        return if (ratio in 0.01..1.2) (ratio * 100).roundToInt().coerceAtMost(100) else null
    }
}
