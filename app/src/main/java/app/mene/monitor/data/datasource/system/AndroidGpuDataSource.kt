package app.mene.monitor.data.datasource.system

import android.content.Context
import android.opengl.GLES20
import android.os.Build
import app.mene.monitor.core.extensions.Formatters
import app.mene.monitor.domain.model.GpuInfo
import java.io.File
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class AndroidGpuDataSource(
    private val context: Context
) {
    private val usageHistory = ArrayDeque<Float>()
    private var cachedGpuName: String? = null
    private var cachedVendor: String? = null
    private var cachedDriverVersion: String? = null
    private var cachedApiVersion: String? = null

    init {
        detectGpuProperties()
    }

    fun getGpuInfo(useFahrenheit: Boolean = false): GpuInfo {
        if (cachedGpuName == null) {
            detectGpuProperties()
        }

        val usage = readGpuUtilizationPercentage()
        val tempCelsius = readGpuTemperatureCelsius()
        val tempFormatted = if (tempCelsius != null) Formatters.formatTemperature(tempCelsius, useFahrenheit) else "N/A"
        val clockSpeed = readGpuClockSpeed() ?: "N/A"

        run {
            synchronized(usageHistory) {
                if (usageHistory.size >= 25) usageHistory.removeFirst()
                usageHistory.addLast((usage ?: 0).toFloat())
            }
        }

        return GpuInfo(
            name = cachedGpuName ?: "N/A",
            vendor = cachedVendor ?: "N/A",
            usagePercentage = usage ?: 0,
            hasUsageReading = usage != null,
            temperature = tempFormatted,
            clockSpeed = clockSpeed,
            vram = "Shared System Memory",
            driverVersion = cachedDriverVersion ?: "N/A",
            apiVersion = cachedApiVersion ?: "OpenGL ES / Vulkan",
            busInfo = "Integrated SoC Interconnect",
            usageHistory = synchronized(usageHistory) { usageHistory.toList() }
        )
    }

    private fun readSysfs(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.bufferedReader().use { it.readLine() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readGpuUtilizationPercentage(): Int? {
        val paths = listOf(
            // MediaTek ged driver (Dimensity / Helio)
            "/sys/module/ged/parameters/gpu_loading",
            "/sys/kernel/ged/hal/gpu_utilization",
            "/sys/class/misc/mali0/device/gpu_loading",

            // Qualcomm Adreno (Snapdragon)
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
            "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load",
            "/sys/class/devfreq/kgsl-3d0/gpu_load",
            "/sys/class/devfreq/3d00000.qcom,kgsl-3d0/gpu_load",
            "/sys/class/devfreq/1c00000.qcom,kgsl-3d0/gpu_load",
            "/sys/devices/platform/soc/3d00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/gpu_busy_percentage",

            // ARM Mali (Tensor, Exynos, Dimensity, Kirin)
            "/sys/devices/platform/mali.0/utilization",
            "/sys/devices/platform/13040000.mali/utilization",
            "/sys/devices/platform/1c500000.mali/utilization",
            "/sys/class/devfreq/1c500000.mali/utilization",
            "/sys/class/devfreq/13040000.mali/utilization",
            "/sys/class/devfreq/gpufreq/utilization",

            // Samsung Xclipse / AMD RDNA
            "/sys/kernel/gpu/gpu_busy",
            "/sys/devices/platform/17000000.gpu/utilization"
        )
        for (p in paths) {
            val line = readSysfs(p)?.trim().orEmpty()
            val valInt = line.replace("%", "").toIntOrNull()
            if (valInt != null && valInt in 0..100) {
                return valInt
            }
        }

        // Qualcomm gpubusy: "busy_time total_time" format
        try {
            val tokens = readSysfs("/sys/class/kgsl/kgsl-3d0/gpubusy")?.trim().orEmpty().split("\\s+".toRegex())
            if (tokens.size >= 2) {
                val busy = tokens[0].toFloatOrNull() ?: 0f
                val total = tokens[1].toFloatOrNull() ?: 0f
                if (total > 0f) {
                    return ((busy / total) * 100f).toInt().coerceIn(0, 100)
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun readGpuClockSpeed(): String? {
        val paths = listOf(
            // MediaTek ged driver
            "/sys/module/ged/parameters/gpu_cur_freq",
            "/sys/module/ged/parameters/current_freq",
            "/sys/class/misc/mali0/device/cur_freq",

            // Qualcomm Adreno
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
            "/sys/class/devfreq/kgsl-3d0/cur_freq",
            "/sys/class/devfreq/3d00000.qcom,kgsl-3d0/cur_freq",
            "/sys/class/devfreq/1c00000.qcom,kgsl-3d0/cur_freq",
            "/sys/devices/platform/soc/3d00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/gpuclk",
            "/sys/devices/platform/soc/1c00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/devfreq/cur_freq",

            // ARM Mali (Tensor, Exynos, MediaTek, Kirin)
            "/sys/devices/platform/mali.0/clock",
            "/sys/devices/platform/13040000.mali/cur_freq",
            "/sys/devices/platform/1c500000.mali/cur_freq",
            "/sys/class/devfreq/1c500000.mali/cur_freq",
            "/sys/class/devfreq/13040000.mali/cur_freq",
            "/sys/class/devfreq/gpufreq/cur_freq",

            // Samsung Xclipse / AMD RDNA
            "/sys/kernel/gpu/gpu_clock",
            "/sys/devices/platform/17000000.gpu/cur_freq"
        )
        for (p in paths) {
            val raw = readSysfs(p)?.trim().orEmpty().toLongOrNull() ?: continue
            val formatted = formatGpuClock(raw)
            if (formatted != null) return formatted
        }
        return null
    }

    private fun formatGpuClock(raw: Long): String? {
        return when {
            // Reported in Hz (e.g. 680000000 -> 680 MHz)
            raw >= 10_000_000L -> "${raw / 1_000_000L} MHz"
            // Reported in kHz (e.g. 680000 -> 680 MHz)
            raw in 50_000L until 10_000_000L -> "${raw / 1_000L} MHz"
            // Reported in MHz (e.g. 680 -> 680 MHz)
            raw in 50L until 50_000L -> "$raw MHz"
            else -> null
        }
    }

    private fun detectGpuProperties() {
        try {
            val egl = EGLContext.getEGL() as EGL10
            val dpy: EGLDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            val vers = IntArray(2)
            egl.eglInitialize(dpy, vers)

            val configSpec = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            egl.eglChooseConfig(dpy, configSpec, configs, 1, numConfig)

            if (numConfig[0] > 0) {
                val attribList = intArrayOf(0x3098, 2, EGL10.EGL_NONE)
                val ctx = egl.eglCreateContext(dpy, configs[0], EGL10.EGL_NO_CONTEXT, attribList)
                val pbuffer = egl.eglCreatePbufferSurface(dpy, configs[0], intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE))
                egl.eglMakeCurrent(dpy, pbuffer, pbuffer, ctx)

                val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
                val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
                val version = GLES20.glGetString(GLES20.GL_VERSION)

                if (!renderer.isNullOrBlank()) cachedGpuName = renderer
                if (!vendor.isNullOrBlank()) cachedVendor = vendor
                if (!version.isNullOrBlank()) {
                    cachedDriverVersion = version
                    cachedApiVersion = version.substringBefore(" ")
                }

                egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
                egl.eglDestroySurface(dpy, pbuffer)
                egl.eglDestroyContext(dpy, ctx)
            }
            egl.eglTerminate(dpy)
        } catch (_: Throwable) {
            val hw = Build.HARDWARE
            if (hw.isNotBlank() && hw != Build.UNKNOWN) {
                cachedGpuName = hw.replaceFirstChar { it.uppercase() }
                cachedVendor = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            }
        }
    }

    private fun readGpuTemperatureCelsius(): Float? {
        for (i in 0..35) {
            val typeFile = File("/sys/class/thermal/thermal_zone$i/type")
            val tempFile = File("/sys/class/thermal/thermal_zone$i/temp")
            if (tempFile.exists() && tempFile.canRead()) {
                try {
                    val type = if (typeFile.exists() && typeFile.canRead()) typeFile.readText().trim().lowercase() else ""
                    if (type.contains("gpu") || type.contains("gpuss") || type.contains("g3d") || type.contains("mali")) {
                        val raw = tempFile.readText().trim().toFloatOrNull() ?: continue
                        val temp = if (raw > 1000) raw / 1000f else raw
                        if (temp in 10f..115f) return temp
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }
}
