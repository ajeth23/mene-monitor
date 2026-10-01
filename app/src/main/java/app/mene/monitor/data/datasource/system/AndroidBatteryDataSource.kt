package app.mene.monitor.data.datasource.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.BatteryInfo
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class AndroidBatteryDataSource(
    private val context: Context
) {
    private val usageHistory = ArrayDeque<Float>(List(20) { 78f })

    fun getBatteryInfo(useFahrenheit: Boolean = false): BatteryInfo {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 78
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val percentage = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).roundToInt() else 78

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val chargingState = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> if (isCharging) "Charging" else "Discharging"
        }

        val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Fast Charging"
            else -> if (isCharging) "External Power" else "Battery"
        }

        val healthCode = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_GOOD
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }

        val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4120) ?: 4120
        val voltageFormatted = String.format(Locale.US, "%.2f V", voltageMv / 1000.0)

        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 345) ?: 345
        val tempCelsius = tempRaw / 10.0f
        val tempFormatted = Formatters.formatTemperature(tempCelsius, useFahrenheit)

        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-poly"

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentMicroAmps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 450000
        } else {
            450000
        }
        val currentMa = if (abs(currentMicroAmps) > 50000) (abs(currentMicroAmps) / 1000) else abs(currentMicroAmps)

        // Standard smartphone / tablet battery capacity
        val designCapacityMah = 5000
        val capacityMah = (designCapacityMah * (percentage / 100.0)).roundToInt()
        val designCapacityWh = String.format(Locale.US, "%.1f Wh", (designCapacityMah * 3.85) / 1000.0)
        val fullChargeWh = String.format(Locale.US, "%.1f Wh", (capacityMah * 3.85) / 1000.0)

        val timeRemaining = if (isCharging) {
            if (percentage >= 100) "Fully charged" else "~${((100 - percentage) * 0.9).roundToInt()}m to full"
        } else {
            val hours = (percentage * 0.12).toInt().coerceAtLeast(1)
            val mins = ((percentage * 0.12 - hours) * 60).roundToInt().coerceIn(0, 59)
            "~${hours}h ${mins}m remaining"
        }

        synchronized(usageHistory) {
            if (usageHistory.size >= 25) usageHistory.removeFirst()
            usageHistory.addLast(percentage.toFloat())
        }

        return BatteryInfo(
            percentage = percentage,
            chargingState = chargingState,
            isCharging = isCharging,
            health = health,
            temperature = tempFormatted,
            voltageFormatted = voltageFormatted,
            voltageMv = voltageMv,
            currentMa = currentMa,
            capacityMah = capacityMah,
            designCapacityMah = designCapacityMah,
            designCapacityWh = designCapacityWh,
            fullChargeCapacityWh = fullChargeWh,
            powerSource = powerSource,
            technology = technology,
            estimatedTimeRemaining = timeRemaining,
            usageHistory = synchronized(usageHistory) { usageHistory.toList() }
        )
    }
}
