package app.mene.monitor.data.datasource.system

import android.os.Build
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.CoreInfo
import app.mene.monitor.domain.model.CpuInfo
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.roundToInt

class AndroidCpuDataSource {

    private val usageHistory = ArrayDeque<Float>()
    private var lastTotalTime = 0L
    private var lastIdleTime = 0L
    private val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

    fun getCpuInfo(useFahrenheit: Boolean = false): CpuInfo {
        val arch = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else System.getProperty("os.arch") ?: "ARM64"
        val cpuName = getCpuModelName()
        val manufacturer = getCpuManufacturer(cpuName)

        val overallUsage = calculateProcStatCpuUsage()

        val cores = mutableListOf<CoreInfo>()
        var totalFrequencyMhz = 0
        var activeCoresCount = 0

        for (i in 0 until coreCount) {
            val freqKHz = readCoreFrequencyKHz(i)
            val maxFreqKHz = readCoreMaxFrequencyKHz(i)
            val freqMhz = if (freqKHz > 0) freqKHz / 1000 else if (maxFreqKHz > 0) (maxFreqKHz / 1000 * 0.65).toInt() else 0

            val coreUsage = if (maxFreqKHz > 0 && freqKHz > 0) {
                ((freqKHz.toFloat() / maxFreqKHz.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
            } else {
                0
            }

            if (freqMhz > 0) {
                totalFrequencyMhz += freqMhz
                activeCoresCount++
            }

            cores.add(
                CoreInfo(
                    index = i + 1,
                    usagePercentage = coreUsage,
                    frequencyMhz = freqMhz,
                    formattedFrequency = if (freqMhz > 0) Formatters.formatFrequency(freqMhz) else "Offline / Sleep"
                )
            )
        }

        val avgFreqMhz = if (activeCoresCount > 0) totalFrequencyMhz / activeCoresCount else 0

        val baseFreqMhz = readMinFrequencyMhz()
        val baseFreqFormatted = if (baseFreqMhz > 0) Formatters.formatFrequency(baseFreqMhz) else "N/A"
        val curFreqFormatted = if (avgFreqMhz > 0) Formatters.formatFrequency(avgFreqMhz) else "N/A"

        val tempCelsius = readCpuTemperatureCelsius()
        val tempFormatted = if (tempCelsius != null) Formatters.formatTemperature(tempCelsius, useFahrenheit) else "N/A"

        // Update history strictly when valid overall usage is computed
        overallUsage?.let { usageVal ->
            synchronized(usageHistory) {
                if (usageHistory.size >= 25) usageHistory.removeFirst()
                usageHistory.addLast(usageVal.toFloat())
            }
        }

        return CpuInfo(
            name = cpuName,
            manufacturer = manufacturer,
            architecture = arch,
            coreCount = coreCount,
            threadCount = coreCount,
            baseFrequency = baseFreqFormatted,
            currentFrequency = curFreqFormatted,
            usagePercentage = overallUsage ?: 0,
            temperature = tempFormatted,
            cores = cores,
            l1Cache = "N/A",
            l2Cache = "N/A",
            l3Cache = "N/A",
            governor = readGovernor() ?: "N/A",
            tdp = "N/A",
            processNode = "N/A",
            usageHistory = synchronized(usageHistory) { usageHistory.toList() }
        )
    }

    private fun calculateProcStatCpuUsage(): Int? {
        return try {
            val file = File("/proc/stat")
            if (!file.exists() || !file.canRead()) return null

            val line = file.bufferedReader().use { it.readLine() } ?: return null
            val tokens = line.split("\\s+".toRegex())
            if (tokens.size < 5 || tokens[0] != "cpu") return null

            val user = tokens[1].toLongOrNull() ?: 0L
            val nice = tokens[2].toLongOrNull() ?: 0L
            val system = tokens[3].toLongOrNull() ?: 0L
            val idle = tokens[4].toLongOrNull() ?: 0L
            val iowait = tokens.getOrNull(5)?.toLongOrNull() ?: 0L
            val irq = tokens.getOrNull(6)?.toLongOrNull() ?: 0L
            val softirq = tokens.getOrNull(7)?.toLongOrNull() ?: 0L

            val totalTime = user + nice + system + idle + iowait + irq + softirq
            val idleTime = idle + iowait

            val deltaTotal = totalTime - lastTotalTime
            val deltaIdle = idleTime - lastIdleTime

            lastTotalTime = totalTime
            lastIdleTime = idleTime

            if (deltaTotal > 0) {
                val usageFraction = (deltaTotal - deltaIdle).toDouble() / deltaTotal.toDouble()
                (usageFraction * 100.0).roundToInt().coerceIn(0, 100)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readCoreFrequencyKHz(coreIndex: Int): Int {
        val paths = listOf(
            "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq",
            "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_cur_freq"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val line = file.readText().trim()
                    return line.toIntOrNull() ?: 0
                }
            } catch (_: Exception) {}
        }
        return 0
    }

    private fun readCoreMaxFrequencyKHz(coreIndex: Int): Int {
        val paths = listOf(
            "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq",
            "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_max_freq"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val line = file.readText().trim()
                    return line.toIntOrNull() ?: 0
                }
            } catch (_: Exception) {}
        }
        return 0
    }

    private fun readMinFrequencyMhz(): Int {
        try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq")
            if (file.exists() && file.canRead()) {
                val khz = file.readText().trim().toIntOrNull()
                if (khz != null && khz > 0) return khz / 1000
            }
        } catch (_: Exception) {}
        return 0
    }

    private fun readGovernor(): String? {
        try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
            if (file.exists() && file.canRead()) {
                val text = file.readText().trim()
                if (text.isNotBlank()) return text.replaceFirstChar { it.uppercase() }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun readCpuTemperatureCelsius(): Float? {
        for (i in 0..15) {
            val typeFile = File("/sys/class/thermal/thermal_zone$i/type")
            val tempFile = File("/sys/class/thermal/thermal_zone$i/temp")
            if (tempFile.exists() && tempFile.canRead()) {
                try {
                    val type = if (typeFile.exists() && typeFile.canRead()) typeFile.readText().trim().lowercase() else ""
                    if (type.contains("cpu") || type.contains("soc") || type.contains("tsens") || type.isEmpty()) {
                        val rawTemp = tempFile.readText().trim().toFloatOrNull()
                        if (rawTemp != null && rawTemp > 0) {
                            val temp = if (rawTemp > 1000) rawTemp / 1000f else rawTemp
                            if (temp in 10f..115f) return temp
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }

    private fun getCpuModelName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.SOC_MODEL.isNotBlank() && Build.SOC_MODEL != Build.UNKNOWN) {
            return Build.SOC_MODEL
        }
        try {
            BufferedReader(FileReader("/proc/cpuinfo")).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.startsWith("Hardware", ignoreCase = true) || trimmed.startsWith("model name", ignoreCase = true)) {
                        val parts = trimmed.split(":")
                        if (parts.size > 1 && parts[1].isNotBlank()) {
                            return parts[1].trim()
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        val hw = Build.HARDWARE
        return if (hw.isNotBlank() && hw != Build.UNKNOWN) {
            hw.replaceFirstChar { it.uppercase() }
        } else {
            "ARM Processor"
        }
    }

    private fun getCpuManufacturer(modelName: String): String {
        val lower = modelName.lowercase()
        return when {
            lower.contains("snapdragon") || lower.contains("qualcomm") || lower.contains("qcom") -> "Qualcomm"
            lower.contains("dimensity") || lower.contains("helio") || lower.contains("mediatek") -> "MediaTek"
            lower.contains("exynos") || lower.contains("samsung") -> "Samsung"
            lower.contains("tensor") || lower.contains("google") -> "Google"
            lower.contains("ryzen") || lower.contains("amd") -> "AMD"
            lower.contains("intel") || lower.contains("core") -> "Intel"
            lower.contains("apple") || lower.contains("bionic") -> "Apple"
            else -> "ARM Ltd."
        }
    }
}
