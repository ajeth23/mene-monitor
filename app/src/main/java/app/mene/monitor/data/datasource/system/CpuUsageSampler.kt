package app.mene.monitor.data.datasource.system

import kotlin.math.roundToInt

/** Interval utilization from /proc/stat; guest time is already included in user/nice. */
internal class CpuUsageSampler {
    private var previous: Pair<Long, Long>? = null

    fun sample(line: String?): Int? {
        val tokens = line?.trim()?.split(Regex("\\s+"))
        val counters = tokens?.drop(1)?.take(8)?.mapNotNull { it.toLongOrNull() }
        if (tokens?.firstOrNull() != "cpu" || counters == null || counters.size < 4 ||
            counters.size != minOf(8, tokens.size - 1) || counters.any { it < 0 }) {
            previous = null
            return null
        }
        val total = counters.sum()
        val idle = counters[3] + (counters.getOrNull(4) ?: 0)
        val old = previous
        previous = total to idle
        if (old == null) return null
        val elapsed = total - old.first
        val idleElapsed = idle - old.second
        if (elapsed <= 0 || idleElapsed !in 0..elapsed) return null
        return ((elapsed - idleElapsed) * 100.0 / elapsed).roundToInt()
    }
}
