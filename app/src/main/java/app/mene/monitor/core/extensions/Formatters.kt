package app.mene.monitor.core.extensions

import java.util.Locale

object Formatters {

    fun formatBytes(bytes: Long, binary: Boolean = false): String {
        if (bytes <= 0) return "0 B"
        val unit = if (binary) 1024.0 else 1000.0
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit)).toInt()
        val pre = if (binary) {
            listOf("B", "KiB", "MiB", "GiB", "TiB")
        } else {
            listOf("B", "KB", "MB", "GB", "TB")
        }
        val safeExp = exp.coerceIn(0, pre.size - 1)
        val value = bytes / Math.pow(unit, safeExp.toDouble())
        return if (safeExp == 0) {
            String.format(Locale.US, "%d %s", bytes, pre[safeExp])
        } else {
            String.format(Locale.US, "%.1f %s", value, pre[safeExp])
        }
    }

    fun formatNetworkSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0.0 Kbps"
        val bitsPerSec = bytesPerSec * 8.0
        return when {
            bitsPerSec >= 1_000_000_000 -> String.format(Locale.US, "%.1f Gbps", bitsPerSec / 1_000_000_000.0)
            bitsPerSec >= 1_000_000 -> String.format(Locale.US, "%.1f Mbps", bitsPerSec / 1_000_000.0)
            bitsPerSec >= 1_000 -> String.format(Locale.US, "%.1f Kbps", bitsPerSec / 1_000.0)
            else -> String.format(Locale.US, "%.0f bps", bitsPerSec)
        }
    }

    fun formatFrequency(mhz: Int): String {
        return if (mhz >= 1000) {
            String.format(Locale.US, "%.2f GHz", mhz / 1000.0)
        } else if (mhz > 0) {
            "$mhz MHz"
        } else {
            "N/A"
        }
    }

    fun formatTemperature(celsius: Float, useFahrenheit: Boolean = false): String {
        return if (celsius <= 0f) {
            "Not available"
        } else if (useFahrenheit) {
            val f = (celsius * 9f / 5f) + 32f
            String.format(Locale.US, "%.1f°F", f)
        } else {
            String.format(Locale.US, "%.0f°C", celsius)
        }
    }

    fun formatUptime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        val days = millis / (1000 * 60 * 60 * 24)
        return if (days > 0) {
            "${days}d ${hours}h ${minutes}m"
        } else {
            "${hours}h ${minutes}m ${seconds}s"
        }
    }
}
