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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
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
    clockLabel: String = "Current Clock",
    hasReading: Boolean = true
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
            hasReading = hasReading,
            accentColor = accentColor,
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Side details: Temperature and Clock speed
        Column(modifier = Modifier.weight(1f)) {
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
                    fontSize = 16.sp
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
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CircularUsageGauge(
    percentage: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    hasReading: Boolean = true
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = if (hasReading) percentage.coerceIn(0, 100).toFloat() else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "gauge_anim"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val trackColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 10.dp.toPx()
            // Track background arc (270 degrees)
            drawArc(
                color = trackColor,
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
                text = if (hasReading) "${percentage}%" else "N/A",
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
