package app.mene.monitor.data.datasource.system

import android.content.Context
import android.os.Environment
import android.os.StatFs
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.StorageInfo
import java.io.File
import kotlin.math.roundToInt

class AndroidStorageDataSource(
    private val context: Context
) {
    private val usageHistory = ArrayDeque<Float>()

    fun getStorageInfo(useFahrenheit: Boolean = false): StorageInfo {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val total = totalBlocks * blockSize
        val available = availableBlocks * blockSize
        val used = (total - available).coerceAtLeast(0)
        val percentage = if (total > 0) ((used.toDouble() / total.toDouble()) * 100.0).roundToInt().coerceIn(0, 100) else 0

        synchronized(usageHistory) {
            if (usageHistory.size >= 25) usageHistory.removeFirst()
            usageHistory.addLast(percentage.toFloat())
        }

        val tempCelsius = readStorageTemperatureCelsius()
        val tempFormatted = if (tempCelsius != null) Formatters.formatTemperature(tempCelsius, useFahrenheit) else "N/A"

        return StorageInfo(
            deviceName = "Internal Storage",
            mountPoint = path.absolutePath,
            totalBytes = total,
            usedBytes = used,
            availableBytes = available,
            usagePercentage = percentage,
            fileSystem = "N/A",
            health = "N/A",
            temperature = tempFormatted,
            isRemovable = false,
            usageHistory = synchronized(usageHistory) { usageHistory.toList() }
        )
    }

    private fun readStorageTemperatureCelsius(): Float? {
        for (i in 0..15) {
            val typeFile = File("/sys/class/thermal/thermal_zone$i/type")
            val tempFile = File("/sys/class/thermal/thermal_zone$i/temp")
            if (tempFile.exists() && tempFile.canRead()) {
                try {
                    val type = if (typeFile.exists() && typeFile.canRead()) typeFile.readText().trim().lowercase() else ""
                    if (type.contains("ufs") || type.contains("emmc") || type.contains("nand") || type.contains("storage")) {
                        val raw = tempFile.readText().trim().toFloatOrNull() ?: continue
                        val temp = if (raw > 1000) raw / 1000f else raw
                        if (temp in 10f..90f) return temp
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }
}
