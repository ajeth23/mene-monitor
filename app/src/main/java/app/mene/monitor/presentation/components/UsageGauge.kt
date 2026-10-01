package app.mene.monitor.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun UsageGaugeWithStats(
    percentage: Int,
    temperature: String,
    clockSpeed: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    tempLabel: String = "Temperature",
    clockLabel: String = "Current Clock"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Gauge
        CircularUsageGauge(
            percentage = percentage,
            accentColor = accentColor,
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.width(28.dp))

        // Side details: Temperature and Clock speed
        Column {
            Column {
                Text(
                    text = temperature,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tempLabel,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = clockSpeed,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = clockLabel,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CircularUsageGauge(
    percentage: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 600),
        label = "gauge_anim"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 10.dp.toPx()
            // Track background arc (270 degrees)
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Value arc
            val sweep = (animatedPercentage / 100f) * 270f
            if (sweep > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${percentage}%",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Usage",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
