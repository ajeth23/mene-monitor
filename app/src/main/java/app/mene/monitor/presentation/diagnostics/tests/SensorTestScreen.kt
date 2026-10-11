package app.mene.monitor.presentation.diagnostics.tests

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.mene.monitor.domain.model.TestStatus
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.theme.*
import java.util.Locale

private data class SensorTestSpec(val type: Int, val title: String, val instruction: String, val threshold: Float, val unit: String)
private data class SensorTestReading(val supported: Boolean, val value: String = "Waiting for a reading", val responded: Boolean = false, val blocked: String? = null)

@Composable
fun SensorTestScreen(onCancel: () -> Unit, onComplete: (TestStatus, String) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val manager = remember(context) { context.getSystemService(android.content.Context.SENSOR_SERVICE) as? SensorManager }
    val specs = remember { listOf(
        SensorTestSpec(Sensor.TYPE_ACCELEROMETER, "Accelerometer", "Slowly tilt the phone in different directions.", 0.7f, "m/s²"),
        SensorTestSpec(Sensor.TYPE_GYROSCOPE, "Gyroscope", "Rotate the phone gently, then hold it still.", 0.1f, "rad/s"),
        SensorTestSpec(Sensor.TYPE_LIGHT, "Light", "Cover the light sensor, then uncover it in a brighter area.", 1f, "lx"),
        SensorTestSpec(Sensor.TYPE_PROXIMITY, "Proximity", "Cover and uncover the proximity sensor near the top of the screen.", 0.1f, "cm")
    ) }
    val sensors = remember(manager) { specs.associate { it.type to manager?.getDefaultSensor(it.type) } }
    val readings = remember { mutableStateMapOf<Int, SensorTestReading>().apply {
        specs.forEach { put(it.type, SensorTestReading(sensors[it.type] != null)) }
    } }
    var attempt by remember { mutableIntStateOf(0) }
    DisposableEffect(manager, owner, attempt) {
        val trackers = specs.associate { it.type to SensorResponseTracker(it.threshold) }
        specs.forEach { readings[it.type] = SensorTestReading(sensors[it.type] != null) }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val spec = specs.firstOrNull { it.type == event.sensor.type } ?: return
                val tracker = trackers.getValue(spec.type)
                tracker.record(event.values)
                if (event.values.isEmpty() || event.values.any { !it.isFinite() }) return
                val value = if (spec.type == Sensor.TYPE_ACCELEROMETER || spec.type == Sensor.TYPE_GYROSCOPE) {
                    event.values.take(3).mapIndexed { index, value ->
                        "${listOf("X", "Y", "Z")[index]} ${String.format(Locale.getDefault(), "%.2f", value)}"
                    }.joinToString("  ") + " ${spec.unit}"
                } else String.format(Locale.getDefault(), "%.1f %s", event.values[0], spec.unit)
                readings[spec.type] = SensorTestReading(true, value, tracker.responded)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        var registered = false
        fun start() {
            if (registered) return
            registered = true
            specs.forEach { spec ->
                sensors[spec.type]?.let { sensor ->
                    try {
                        if (manager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL, Handler(Looper.getMainLooper())) != true) {
                            readings[spec.type] = SensorTestReading(true, blocked = "Could not start sensor readings")
                        }
                    } catch (_: SecurityException) {
                        readings[spec.type] = SensorTestReading(true, blocked = "Sensor access is blocked by this device")
                    }
                }
            }
        }
        fun stop() { manager?.unregisterListener(listener); registered = false }
        val observer = LifecycleEventObserver { _, event ->
            when (event) { Lifecycle.Event.ON_START -> start(); Lifecycle.Event.ON_STOP -> stop(); else -> Unit }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
        onDispose { owner.lifecycle.removeObserver(observer); stop() }
    }
    val supported = readings.values.count { it.supported }
    val responded = readings.values.count { it.supported && it.responded }
    Column(Modifier.fillMaxSize().background(DarkBackground).safeDrawingPadding()) {
        HardwareHeader("Sensor test", onBackClick = onCancel)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("$responded of $supported available sensors responded", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Follow each instruction and watch the live readings. A response checks basic function, not calibration or accuracy.",
                color = TextSecondary, fontSize = 16.sp, lineHeight = 24.sp)
            specs.forEach { spec ->
                val reading = readings.getValue(spec.type)
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DarkSurface).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(spec.title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(when {
                        !reading.supported -> "Unsupported on this device"
                        reading.blocked != null -> reading.blocked
                        reading.responded -> "Response detected"
                        else -> "Follow the instruction to check response"
                    }, color = if (reading.responded) AccentMemory else TextSecondary, fontSize = 14.sp)
                    if (reading.supported) {
                        Text(spec.instruction, color = TextSecondary, fontSize = 16.sp)
                        Text(reading.value, color = TextPrimary, fontSize = 16.sp)
                    }
                }
            }
            Text("If values stay unchanged, keep this page open, check whether Sensors Off is enabled in Android developer settings, and retry. Missing sensors are skipped.",
                color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
            Button(
                onClick = {
                    val status = when { supported == 0 -> TestStatus.UNSUPPORTED; responded == supported -> TestStatus.PASSED; else -> TestStatus.WARNING }
                    val detail = when (status) {
                        TestStatus.UNSUPPORTED -> "None of the four test sensors are available"
                        TestStatus.PASSED -> "$responded available sensors responded; calibration was not tested"
                        else -> "$responded of $supported available sensors responded; remaining checks need review"
                    }
                    onComplete(status, detail)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = androidx.compose.ui.graphics.Color.Black)
            ) { Text(if (supported == 0) "Finish — unsupported" else if (responded == supported) "Finish test" else "Finish with checks to review", fontSize = 16.sp) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { attempt++ }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Retry", fontSize = 16.sp) }
                OutlinedButton(onClick = { onComplete(TestStatus.FAILED, "User reported a sensor issue after guided testing") },
                    enabled = supported > 0, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Issue found", fontSize = 16.sp) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
