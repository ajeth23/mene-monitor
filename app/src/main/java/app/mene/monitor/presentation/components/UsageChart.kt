package app.mene.monitor.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary

@Composable
fun UsageChart(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    title: String = "Usage History",
    maxValue: Float = 100f,
    minValue: Float = 0f,
    unit: String = "%",
    timestamps: List<Long> = emptyList(),
    unavailableMessage: String = "Reading unavailable",
    currentValue: Float? = dataPoints.lastOrNull(),
    decimalPlaces: Int = 0
) {
    val valid = dataPoints.filter { it.isFinite() }
    val timed = timestamps.size == dataPoints.size && timestamps.size > 1 && timestamps.last() > timestamps.first()
    val lower = minOf(minValue, valid.minOrNull() ?: minValue)
    val upper = maxOf(maxValue, valid.maxOrNull() ?: maxValue, lower + 1f)
    fun label(value: Float) = String.format(java.util.Locale.getDefault(), "%.${decimalPlaces.coerceIn(0, 2)}f%s", value, unit)
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Text(
            currentValue?.takeIf { it.isFinite() }?.let { label(it) } ?: unavailableMessage,
            color = if (currentValue?.isFinite() == true) lineColor else TextMuted,
            fontSize = if (currentValue?.isFinite() == true) 32.sp else 16.sp
        )
        if (valid.isEmpty()) {
            Text("Waiting for accessible device readings.", color = TextMuted, fontSize = 14.sp)
        } else {
            val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
            Row(Modifier.fillMaxWidth().height(180.dp)) {
                Canvas(Modifier.weight(1f).fillMaxHeight()) {
                    val w = size.width
                    val h = size.height
                    listOf(0f, 0.5f, 1f).forEach { fraction ->
                        drawLine(gridColor, Offset(0f, h * fraction), Offset(w, h * fraction))
                    }
                    val path = Path()
                    var connected = false
                    dataPoints.forEachIndexed { index, value ->
                        if (!value.isFinite()) {
                            connected = false
                        } else {
                            val x = if (timed) ((timestamps[index] - timestamps.first()).toFloat() /
                                (timestamps.last() - timestamps.first())) * w
                                else if (dataPoints.size > 1) index.toFloat() / (dataPoints.size - 1) * w else w
                            val y = h - 4f - ((value - lower) / (upper - lower)) * (h - 8f)
                            if (connected) path.lineTo(x, y) else path.moveTo(x, y)
                            connected = true
                            drawCircle(lineColor, 2.dp.toPx(), Offset(x, y))
                        }
                    }
                    drawPath(path, lineColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                Column(Modifier.fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    listOf(upper, (upper + lower) / 2, lower).forEach {
                        Text(label(it), color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (timed) "${(timestamps.last() - timestamps.first()) / 1000}s ago" else "Earlier samples",
                    color = TextMuted, fontSize = 12.sp)
                Text("Latest sample", color = TextMuted, fontSize = 12.sp)
            }
            Text("Min ${label(valid.min())}  ·  Max ${label(valid.max())}", color = TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
fun Sparkline(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    maxValue: Float = 100f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkline_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkline_alpha"
    )

    Canvas(modifier = modifier) {
        val recentReadings = dataPoints.takeLastWhile { it.isFinite() }.takeLast(12)
        if (recentReadings.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val points = recentReadings.let { list ->
            list.mapIndexed { index, value ->
                val x = index * (w / (list.size - 1))
                val normalized = (value / maxValue).coerceIn(0f, 1f)
                val y = h - (normalized * (h - 4f)) - 2f
                Offset(x, y)
            }
        }

        val strokePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val cur = points[i]
                val cX = (prev.x + cur.x) / 2f
                cubicTo(cX, prev.y, cX, cur.y, cur.x, cur.y)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.22f), Color.Transparent)
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Animated live leading pulse dot
        val lastPoint = points.last()
        drawCircle(
            color = lineColor.copy(alpha = pulseAlpha * 0.45f),
            radius = 5.dp.toPx(),
            center = lastPoint
        )
        drawCircle(
            color = lineColor,
            radius = 2.5.dp.toPx(),
            center = lastPoint
        )
    }
}
