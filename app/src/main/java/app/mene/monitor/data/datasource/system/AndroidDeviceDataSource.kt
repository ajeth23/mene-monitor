package app.mene.monitor.data.datasource.system

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.SystemClock
import android.view.WindowManager
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.DeviceInfo
import java.io.BufferedReader
import java.io.FileReader

class AndroidDeviceDataSource(
    private val context: Context
) {
    fun getDeviceInfo(): DeviceInfo {
        val dm = Resources.getSystem().displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val densityDpi = dm.densityDpi

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.display
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay
            }
        } else {
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay
        }
        val refreshRate = display?.refreshRate ?: 60.0f

        val kernel = readKernelVersion()
        val uptime = Formatters.formatUptime(SystemClock.elapsedRealtime())

        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val deviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }

        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { it.isNotBlank() && it != Build.UNKNOWN } ?: Build.HARDWARE
        } else {
            Build.HARDWARE
        }

        return DeviceInfo(
            deviceName = deviceName,
            model = model,
            manufacturer = manufacturer,
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            processorName = soc,
            gpuName = "Hardware Accelerator", // Refined dynamically by GpuDataSource
            operatingSystem = "Android ${Build.VERSION.RELEASE}",
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            kernelVersion = kernel,
            buildNumber = Build.DISPLAY,
            screenResolution = "${width} x ${height}",
            screenDensityDpi = densityDpi,
            refreshRateHz = refreshRate,
            uptimeFormatted = uptime
        )
    }

    private fun readKernelVersion(): String {
        return try {
            BufferedReader(FileReader("/proc/version")).use { reader ->
                val line = reader.readLine() ?: ""
                val regex = Regex("Linux version ([^ ]+)")
                val match = regex.find(line)
                match?.groupValues?.get(1) ?: System.getProperty("os.version") ?: "Linux"
            }
        } catch (e: Exception) {
            System.getProperty("os.version") ?: "Linux"
        }
    }
}
