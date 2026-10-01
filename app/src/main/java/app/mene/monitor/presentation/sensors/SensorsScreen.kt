package app.mene.monitor.presentation.sensors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.domain.model.SensorCategory
import app.mene.monitor.domain.model.SensorInfo
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.AccentSensors
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkBorderSubtle
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.DarkSurfaceVariant
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun SensorsScreen(
    uiState: SensorsUiState,
    onCategorySelected: (SensorCategory?) -> Unit,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HardwareHeader(
            title = "Sensors & Cooling",
            onBackClick = onBackClick,
            onRefreshClick = onRefreshClick
        )

        // Filter chips horizontal row
        CategoryFilterRow(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading && uiState.allSensors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentSensors)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "sensors_top_spacer") {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                itemsIndexed(
                    items = uiState.filteredSensors,
                    key = { index, sensor -> "sensor_${sensor.id}_${sensor.name}_${sensor.category.name}_$index" }
                ) { _, sensor ->
                    SensorCard(sensor = sensor)
                }

                item(key = "sensors_bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: SensorCategory?,
    onCategorySelected: (SensorCategory?) -> Unit
) {
    val scrollState = rememberScrollState()
    val categories = listOf(
        null to "All",
        SensorCategory.TEMPERATURE to "Temperature",
        SensorCategory.FAN to "Fans & Cooling",
        SensorCategory.MOTION to "Motion",
        SensorCategory.ENVIRONMENT to "Environment",
        SensorCategory.POSITION to "Position"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (cat, label) ->
            val isSelected = selectedCategory == cat
            val bg = if (isSelected) AccentSensors else DarkSurfaceVariant
            val textCol = if (isSelected) Color.White else TextSecondary

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onCategorySelected(cat) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = label,
                    color = textCol,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SensorCard(sensor: SensorInfo) {
    val (icon, iconColor) = getCategoryIconAndColor(sensor.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sensor.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${sensor.vendor} • ${sensor.typeName}",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (sensor.isAvailable) "${sensor.valueFormatted} ${sensor.unit}".trim() else "Not available",
                color = if (sensor.isAvailable) AccentPrimary else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sensor.category.name.lowercase().replaceFirstChar { it.uppercase() },
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

private fun getCategoryIconAndColor(category: SensorCategory): Pair<ImageVector, Color> {
    return when (category) {
        SensorCategory.TEMPERATURE -> Pair(Icons.Default.DeviceThermostat, Color(0xFFEF4444))
        SensorCategory.FAN -> Pair(Icons.Default.Air, Color(0xFF06B6D4))
        SensorCategory.MOTION -> Pair(Icons.Default.DirectionsRun, Color(0xFF3B82F6))
        SensorCategory.ENVIRONMENT -> Pair(Icons.Default.WbSunny, Color(0xFFF59E0B))
        SensorCategory.POSITION -> Pair(Icons.Default.Explore, Color(0xFF10B981))
        SensorCategory.OTHER -> Pair(Icons.Default.Sensors, Color(0xFF8B5CF6))
    }
}
