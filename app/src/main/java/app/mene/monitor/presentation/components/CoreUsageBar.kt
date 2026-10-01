package app.mene.monitor.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.domain.model.CoreInfo
import app.mene.monitor.presentation.theme.AccentCpu
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary

@Composable
fun CoreUsageRow(
    core: CoreInfo,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentCpu
) {
    val progress by animateFloatAsState(
        targetValue = (core.usagePercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "core_bar_${core.index}"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Core ${core.index}",
            color = TextPrimary,
            fontSize = 13.sp,
            modifier = Modifier.width(60.dp)
        )

        // Progress bar track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${core.usagePercentage}%",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = core.formattedFrequency,
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(56.dp)
        )
    }
}
