package app.mene.monitor.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted

@Composable
fun UsageChart(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    title: String = "Usage History",
    maxValue: Float = 100f
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    // Grid lines (0%, 50%, 100%)
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(0f, 0f),
                        end = Offset(w, 0f),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(0f, h),
                        end = Offset(w, h),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )

                    if (dataPoints.size >= 2) {
                        val points = dataPoints.mapIndexed { index, value ->
                            val x = index * (w / (dataPoints.size - 1))
                            val normalized = (value / maxValue).coerceIn(0f, 1f)
                            val y = h - (normalized * (h - 8f)) - 4f
                            Offset(x, y)
                        }

                        // Path for stroke
                        val strokePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val cur = points[i]
                                val cX = (prev.x + cur.x) / 2f
                                cubicTo(cX, prev.y, cX, cur.y, cur.x, cur.y)
                            }
                        }

                        // Path for filled gradient
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.35f),
                                    lineColor.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )

                        drawPath(
                            path = strokePath,
                            color = lineColor,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Y-axis labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "100%", color = TextMuted, fontSize = 10.sp)
                Text(text = "50%", color = TextMuted, fontSize = 10.sp)
                Text(text = "0%", color = TextMuted, fontSize = 10.sp)
            }
        }

        // X-axis timestamps
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "-60s", color = TextMuted, fontSize = 10.sp)
            Text(text = "-45s", color = TextMuted, fontSize = 10.sp)
            Text(text = "-30s", color = TextMuted, fontSize = 10.sp)
            Text(text = "-15s", color = TextMuted, fontSize = 10.sp)
            Text(text = "Now", color = TextMuted, fontSize = 10.sp)
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
        if (dataPoints.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val points = dataPoints.takeLast(12).let { list ->
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
