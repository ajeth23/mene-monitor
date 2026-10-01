package app.mene.monitor.data.datasource.system

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import app.mene.monitor.domain.model.DiagnosticCategory
import app.mene.monitor.domain.model.DiagnosticItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

class AndroidDiagnosticsDataSource(private val context: Context) {

    private var isFlashlightOn = false

    fun toggleFlashlight(enable: Boolean? = null): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return false

            val newState = enable ?: !isFlashlightOn
            cameraManager.setTorchMode(cameraId, newState)
            isFlashlightOn = newState
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun testFlashlightStrobe(): Boolean = withContext(Dispatchers.IO) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return@withContext false

        try {
            // First strobe flash
            cameraManager.setTorchMode(cameraId, true)
            isFlashlightOn = true
            kotlinx.coroutines.delay(220L)
            cameraManager.setTorchMode(cameraId, false)
            isFlashlightOn = false
            kotlinx.coroutines.delay(120L)

            // Second strobe flash
            cameraManager.setTorchMode(cameraId, true)
            isFlashlightOn = true
            kotlinx.coroutines.delay(220L)
            cameraManager.setTorchMode(cameraId, false)
            isFlashlightOn = false
            true
        } catch (e: Exception) {
            false
        } finally {
            // Always guarantee flashlight is turned OFF when testing is done
            try {
                cameraManager.setTorchMode(cameraId, false)
            } catch (_: Exception) {}
            isFlashlightOn = false
        }
    }

    fun runVibrationTest(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(200)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun playAudioTestTone(): Boolean = withContext(Dispatchers.IO) {
        try {
            val sampleRate = 44100
            val durationMs = 500
            val numSamples = durationMs * sampleRate / 1000
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)

            // Fill with a 440 Hz tone
            val freqOfTone = 440.0
            for (i in 0 until numSamples) {
                sample[i] = sin(2.0 * Math.PI * i.toDouble() / (sampleRate / freqOfTone))
            }

            var idx = 0
            for (dVal in sample) {
                val valInt = (dVal * 32767).toInt().coerceIn(-32768, 32767)
                generatedSnd[idx++] = (valInt and 0x00ff).toByte()
                generatedSnd[idx++] = (valInt and 0xff00 ushr 8).toByte()
            }

            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioTrack.play()
            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.stop()
            audioTrack.release()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getInitialDiagnosticItems(): List<DiagnosticItem> {
        return listOf(
            DiagnosticItem(
                id = "display",
                title = "Display & Dead Pixels",
                description = "Full-screen color cycle (Red, Green, Blue, White, Black) to check for dead pixels and screen burn.",
                category = DiagnosticCategory.DISPLAY,
                isInteractive = true
            ),
            DiagnosticItem(
                id = "touchscreen",
                title = "Touchscreen Multi-Touch",
                description = "Grid matrix touch verification to test screen dead zones and touch sensitivity.",
                category = DiagnosticCategory.TOUCHSCREEN,
                isInteractive = true
            ),
            DiagnosticItem(
                id = "vibration",
                title = "Haptics & Vibration",
                description = "Triggers device haptic feedback engine to confirm vibration motor function.",
                category = DiagnosticCategory.VIBRATION,
                isInteractive = false
            ),
            DiagnosticItem(
                id = "flashlight",
                title = "Flashlight & Torch",
                description = "Toggles rear camera LED flash to verify hardware illumination.",
                category = DiagnosticCategory.FLASHLIGHT,
                isInteractive = false
            ),
            DiagnosticItem(
                id = "audio",
                title = "Speaker & Sound Output",
                description = "Plays a 440 Hz audio frequency test tone to verify speaker output.",
                category = DiagnosticCategory.AUDIO,
                isInteractive = false
            ),
            DiagnosticItem(
                id = "sensors",
                title = "Sensors Scan",
                description = "Verifies active hardware sensors (Accelerometer, Gyroscope, Light, Proximity).",
                category = DiagnosticCategory.SENSORS,
                isInteractive = false
            ),
            DiagnosticItem(
                id = "battery",
                title = "Battery & Power Health",
                description = "Checks battery charging system, voltage levels, and thermal conditions.",
                category = DiagnosticCategory.BATTERY,
                isInteractive = false
            )
        )
    }
}
