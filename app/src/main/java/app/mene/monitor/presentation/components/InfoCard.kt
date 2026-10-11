package app.mene.monitor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

data class InfoItem(
    val label: String,
    val value: String,
    val valueColor: Color? = null
)

@Composable
fun DetailsCard(
    title: String,
    items: List<InfoItem>,
    modifier: Modifier = Modifier,
    expandable: Boolean = false
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        if (expandable) {
            TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("$title  ${if (expanded) "Hide" else "Show"}", color = TextPrimary, fontSize = 18.sp)
            }
        } else {
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp))
        }
        if (!expandable || expanded) {
            items.forEachIndexed { index, item ->
                InfoRow(label = item.label, value = item.value, valueColor = item.valueColor)
                if (index < items.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val finalColor = valueColor ?: TextPrimary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            color = TextSecondary,
            fontSize = 16.sp
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = finalColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}
