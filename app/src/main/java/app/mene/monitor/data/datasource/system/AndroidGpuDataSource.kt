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

        usage?.let { usageVal ->
            synchronized(usageHistory) {
                if (usageHistory.size >= 25) usageHistory.removeFirst()
                usageHistory.addLast(usageVal.toFloat())
            }
        }

        return GpuInfo(
            name = cachedGpuName ?: "N/A",
            vendor = cachedVendor ?: "N/A",
            usagePercentage = usage ?: 0,
            temperature = tempFormatted,
            clockSpeed = clockSpeed,
            vram = "Shared System Memory",
            driverVersion = cachedDriverVersion ?: "N/A",
            apiVersion = cachedApiVersion ?: "OpenGL ES / Vulkan",
            busInfo = "Integrated SoC Interconnect",
            usageHistory = synchronized(usageHistory) { usageHistory.toList() }
        )
    }

    private fun readGpuUtilizationPercentage(): Int? {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
            "/sys/class/kgsl/kgsl-3d0/gpubusy",
            "/sys/devices/platform/mali.0/utilization",
            "/sys/class/devfreq/gpufreq/cur_freq"
        )
        for (p in paths) {
            try {
                val f = File(p)
                if (f.exists() && f.canRead()) {
                    val line = f.readText().trim()
                    val valInt = line.replace("%", "").toIntOrNull()
                    if (valInt != null && valInt in 0..100) {
                        return valInt
                    }
                }
            } catch (_: Exception) {}
        }
        return null
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
        for (i in 0..15) {
            val typeFile = File("/sys/class/thermal/thermal_zone$i/type")
            val tempFile = File("/sys/class/thermal/thermal_zone$i/temp")
            if (tempFile.exists() && tempFile.canRead()) {
                try {
                    val type = if (typeFile.exists() && typeFile.canRead()) typeFile.readText().trim().lowercase() else ""
                    if (type.contains("gpu")) {
                        val raw = tempFile.readText().trim().toFloatOrNull() ?: continue
                        val temp = if (raw > 1000) raw / 1000f else raw
                        if (temp in 10f..115f) return temp
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }

    private fun readGpuClockSpeed(): String? {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
            "/sys/devices/platform/mali.0/clock"
        )
        for (p in paths) {
            try {
                val f = File(p)
                if (f.exists() && f.canRead()) {
                    val hz = f.readText().trim().toLongOrNull() ?: continue
                    if (hz > 1_000_000) {
                        return "${hz / 1_000_000} MHz"
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }
}
