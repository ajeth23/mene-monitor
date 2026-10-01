package app.mene.monitor.data.datasource.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import app.mene.monitor.domain.model.MemoryInfo
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

class AndroidMemoryDataSource(
    private val context: Context
) {
    private val usageHistory = ArrayDeque<Float>(List(20) { 45f })

    fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val total = memoryInfo.totalMem.coerceAtLeast(1024 * 1024 * 1024)
        val available = memoryInfo.availMem
        val used = (total - available).coerceAtLeast(0)
        val percentage = ((used.toDouble() / total.toDouble()) * 100.0).roundToInt().coerceIn(1, 100)

        val swapInfo = readSwapInfo()

        synchronized(usageHistory) {
            if (usageHistory.size >= 25) usageHistory.removeFirst()
            usageHistory.addLast(percentage.toFloat())
        }

        val memType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            "LPDDR5X"
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "LPDDR5"
        } else {
            "LPDDR4X"
        }

        val memSpeed = if (memType == "LPDDR5X") "4266 MHz" else if (memType == "LPDDR5") "3200 MHz" else "2133 MHz"

        return MemoryInfo(
            totalBytes = total,
            usedBytes = used,
            availableBytes = available,
            usagePercentage = percentage,
            memoryType = memType,
            memorySpeed = memSpeed,
            swapTotalBytes = swapInfo.first,
            swapUsedBytes = (swapInfo.first - swapInfo.second).coerceAtLeast(0),
            isLowMemory = memoryInfo.lowMemory,
            usageHistory = synchronized(usageHistory) { usageHistory.toList() }
        )
    }

    private fun readSwapInfo(): Pair<Long, Long> {
        var swapTotal = 0L
        var swapFree = 0L
        try {
            BufferedReader(FileReader("/proc/meminfo")).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val l = line!!
                    if (l.startsWith("SwapTotal:")) {
                        val kb = l.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                        swapTotal = kb * 1024
                    } else if (l.startsWith("SwapFree:")) {
                        val kb = l.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                        swapFree = kb * 1024
                    }
                }
            }
        } catch (_: Exception) {}
        return Pair(swapTotal, swapFree)
    }
}
