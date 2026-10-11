package app.mene.monitor.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.theme.*

@Composable
fun SettingsScreen(uiState: SettingsUiState, onAction: (SettingsAction) -> Unit, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(DarkBackground)) {
        HardwareHeader(title = "Settings", onBackClick = onBackClick)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SettingsPanel("Refresh interval", "Choose how often device readings update.") {
                Column(Modifier.selectableGroup()) {
                    listOf(1L to "1 second · Fast", 2L to "2 seconds · Balanced", 5L to "5 seconds · Eco").forEach { (seconds, label) ->
                        SettingsChoice(label, uiState.refreshIntervalSeconds == seconds) { onAction(SettingsAction.SetRefreshInterval(seconds)) }
                    }
                }
            }
            SettingsPanel("Appearance", "Choose a theme that feels comfortable.") {
                Column(Modifier.selectableGroup()) {
                    listOf(AppThemeMode.SYSTEM to "Follow system", AppThemeMode.DARK to "Dark", AppThemeMode.LIGHT to "Light", AppThemeMode.DYNAMIC to "Wallpaper colors").forEach { (mode, label) ->
                        SettingsChoice(label, uiState.themeMode == mode) { onAction(SettingsAction.SetThemeMode(mode)) }
                    }
                }
            }
            SettingsPanel("Temperature units", "Applies to temperature readings throughout the app.") {
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).toggleable(value = uiState.useFahrenheit, role = Role.Switch,
                    onValueChange = { onAction(SettingsAction.SetUseFahrenheit(it)) }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text("Use Fahrenheit", color = TextPrimary, fontSize = 16.sp)
                        Text(if (uiState.useFahrenheit) "Fahrenheit (°F)" else "Celsius (°C)", color = TextSecondary, fontSize = 16.sp)
                    }
                    Switch(checked = uiState.useFahrenheit, onCheckedChange = null, modifier = Modifier.testTag("switch_temperature_unit"))
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(title: String, description: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextSecondary, fontSize = 16.sp)
            content()
        }
    }
}

@Composable
private fun SettingsChoice(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary))
        Spacer(Modifier.width(12.dp))
        Text(label, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
}
