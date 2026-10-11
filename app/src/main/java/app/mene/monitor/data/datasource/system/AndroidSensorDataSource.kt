package app.mene.monitor.data.datasource.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import app.mene.monitor.domain.model.SensorCategory
import app.mene.monitor.domain.model.SensorInfo
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class AndroidSensorDataSource(
    private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val latestReadings = ConcurrentHashMap<Int, FloatArray>()

    init {
        registerListeners()
    }

    private fun registerListeners() {
        if (sensorManager == null) return
        val sensorsToListen = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_AMBIENT_TEMPERATURE
        )
        for (type in sensorsToListen) {
            val sensor = sensorManager.getDefaultSensor(type)
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            latestReadings[event.sensor.type] = event.values.clone()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getSensorsList(useFahrenheit: Boolean = false): List<SensorInfo> {
        val result = mutableListOf<SensorInfo>()

        // Show only temperatures actually exposed by the device.
        val zones = runCatching { File("/sys/class/thermal").listFiles()?.filter { it.name.startsWith("thermal_zone") } }.getOrNull().orEmpty()
        zones.forEachIndexed { index, zone ->
            runCatching {
                val type = File(zone, "type").readText().trim()
                val raw = File(zone, "temp").readText().trim().toFloatOrNull() ?: return@runCatching
                val temperature = raw / 1000f // Linux thermal ABI uses millidegrees Celsius.
                if (type.isNotBlank() && temperature.isFinite() && temperature in -20f..150f) {
                    result.add(SensorInfo(id = 1000 + index, name = type.replace('_', ' '), typeName = type,
                        vendor = "Device thermal sensor", category = SensorCategory.TEMPERATURE,
                        valueFormatted = formatTemp(temperature, useFahrenheit), unit = if (useFahrenheit) "°F" else "°C"))
                }
            }
        }
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        if (batteryRaw != null && batteryRaw != Int.MIN_VALUE) {
            result.add(SensorInfo(id = 1990, name = "Battery", typeName = "Battery temperature",
                vendor = "Android battery service", category = SensorCategory.TEMPERATURE,
                valueFormatted = formatTemp(batteryRaw / 10f, useFahrenheit), unit = if (useFahrenheit) "°F" else "°C"))
        }
        readFanRpm()?.let { rpm ->
            result.add(SensorInfo(id = 2001, name = "Fan Speed", typeName = "Active Cooling",
                vendor = "Device fan sensor", category = SensorCategory.FAN, valueFormatted = "$rpm", unit = "RPM"))
        }

        // 3. HARDWARE MOTION & ENVIRONMENT SENSORS
        val allSensors = sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
        for ((index, s) in allSensors.take(30).withIndex()) {
            val reading = latestReadings[s.type]
            val valueStr = when (s.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    if (reading != null && reading.size >= 3) {
                        String.format(Locale.US, "X: %.1f  Y: %.1f  Z: %.1f", reading[0], reading[1], reading[2])
                    } else "Waiting for reading"
                }
                Sensor.TYPE_GYROSCOPE -> {
                    if (reading != null && reading.size >= 3) {
                        String.format(Locale.US, "X: %.2f  Y: %.2f  Z: %.2f", reading[0], reading[1], reading[2])
                    } else "Waiting for reading"
                }
                Sensor.TYPE_LIGHT -> {
                    if (reading != null && reading.isNotEmpty()) {
                        String.format(Locale.US, "%.0f lx", reading[0])
                    } else "Not available"
                }
                Sensor.TYPE_PRESSURE -> {
                    if (reading != null && reading.isNotEmpty()) {
                        String.format(Locale.US, "%.1f hPa", reading[0])
                    } else "Not available"
                }
                Sensor.TYPE_PROXIMITY -> {
                    if (reading != null && reading.isNotEmpty()) {
                        String.format(Locale.US, "%.1f cm", reading[0])
                    } else "Waiting for reading"
                }
                Sensor.TYPE_AMBIENT_TEMPERATURE -> reading?.firstOrNull()?.let { formatTemp(it, useFahrenheit) } ?: "Waiting for reading"
                else -> "Not monitored"
            }

            val cat = when (s.type) {
                Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GRAVITY,
                Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ROTATION_VECTOR -> SensorCategory.MOTION
                Sensor.TYPE_AMBIENT_TEMPERATURE -> SensorCategory.TEMPERATURE
                Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE, Sensor.TYPE_RELATIVE_HUMIDITY -> SensorCategory.ENVIRONMENT
                Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_PROXIMITY -> SensorCategory.POSITION
                else -> SensorCategory.OTHER
            }

            result.add(
                SensorInfo(
                    id = 3000 + index,
                    name = s.name,
                    typeName = s.stringType ?: "android.sensor",
                    vendor = s.vendor ?: "Device OEM",
                    category = cat,
                    valueFormatted = valueStr,
                    unit = "",
                    isAvailable = true,
                    powerMa = s.power,
                    maxRange = s.maximumRange,
                    resolution = s.resolution
                )
            )
        }

        return result
    }

    private fun readFanRpm(): Int? {
        val fanPaths = listOf(
            "/sys/class/hwmon/hwmon0/fan1_input"
        )
        for (p in fanPaths) {
            try {
                val f = File(p)
                if (f.exists() && f.canRead()) {
                    val v = f.readText().trim().toIntOrNull()
                    if (v != null) return v
                }
            } catch (_: Exception) {}
        }
        return null // Indicates passive / no active fan, will show "0 RPM" or "Not available"
    }

    private fun formatTemp(celsius: Float, useFahrenheit: Boolean): String {
        return if (useFahrenheit) {
            String.format(Locale.US, "%.0f°F", (celsius * 9f / 5f) + 32f)
        } else {
            String.format(Locale.US, "%.0f°C", celsius)
        }
    }
}
