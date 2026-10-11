package app.mene.monitor.data.datasource.system

import android.app.ActivityManager
import android.content.Context
import app.mene.monitor.domain.model.MemoryInfo
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

class AndroidMemoryDataSource(
    private val context: Context
) {
    private val usageHistory = ArrayDeque<Float>()
    private val usageTimestamps = ArrayDeque<Long>()

    fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val total = memoryInfo.totalMem.coerceAtLeast(0)
        val available = memoryInfo.availMem.coerceIn(0, total)
        val used = (total - available).coerceAtLeast(0)
        val precisePercentage = if (total > 0) (used.toDouble() / total * 100).toFloat() else null
        val percentage = precisePercentage?.roundToInt() ?: 0

        val swapInfo = readSwapInfo()

        synchronized(usageHistory) {
            if (precisePercentage != null) {
                if (usageHistory.size >= 60) { usageHistory.removeFirst(); usageTimestamps.removeFirst() }
                usageHistory.addLast(precisePercentage)
                usageTimestamps.addLast(android.os.SystemClock.elapsedRealtime())
            }
        }

        return MemoryInfo(
            totalBytes = total,
            usedBytes = used,
            availableBytes = available,
            usagePercentage = percentage,
            memoryType = "Unavailable",
            memorySpeed = "Unavailable",
            swapTotalBytes = swapInfo.first,
            swapUsedBytes = (swapInfo.first - swapInfo.second).coerceAtLeast(0),
            isLowMemory = memoryInfo.lowMemory,
            usageTimestamps = synchronized(usageHistory) { usageTimestamps.toList() },
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
