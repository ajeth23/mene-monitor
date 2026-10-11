package app.mene.monitor.presentation.diagnostics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.domain.model.DiagnosticCategory
import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.model.TestStatus
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.diagnostics.tests.DisplayTestScreen
import app.mene.monitor.presentation.diagnostics.tests.SensorTestScreen
import app.mene.monitor.presentation.diagnostics.tests.TouchTestScreen
import app.mene.monitor.presentation.theme.*

@Composable
fun DiagnosticsScreen(uiState: DiagnosticsUiState, onAction: (DiagnosticsAction) -> Unit, modifier: Modifier = Modifier) {
    BackHandler(enabled = uiState.activeInteractiveTest != null) { onAction(DiagnosticsAction.CancelInteractiveTest) }
    when (uiState.activeInteractiveTest) {
        "display" -> { DisplayTestScreen { onAction(DiagnosticsAction.CompleteInteractiveTest("display", it)) }; return }
        "touchscreen" -> { TouchTestScreen { onAction(DiagnosticsAction.CompleteInteractiveTest("touchscreen", it)) }; return }
        "sensors" -> {
            SensorTestScreen(onCancel = { onAction(DiagnosticsAction.CancelInteractiveTest) },
                onComplete = { status, detail -> onAction(DiagnosticsAction.CompleteSensorTest(status, detail)) })
            return
        }
    }
    val busy = uiState.isRunningFullScan || uiState.currentRunningTestId != null
    Column(modifier.fillMaxSize().background(DarkBackground)) {
        HardwareHeader(title = "Hardware Diagnostics")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DiagnosticPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Comprehensive hardware check", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            when {
                                busy -> "Running diagnostic checks…"
                                uiState.report.failedTests > 0 -> "${uiState.report.passedTests} passed · ${uiState.report.failedTests} failed"
                                uiState.report.passedTests > 0 -> "All ${uiState.report.passedTests} completed tests passed"
                                else -> "Run all automated tests in one tap."
                            },
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                    Button(
                        onClick = { onAction(DiagnosticsAction.RunFullScan) },
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                    ) {
                        Text(if (busy) "Running…" else "Run All", fontSize = 16.sp)
                    }
                }
            }
            if (uiState.showManualTestNotice && uiState.pendingManualItems.isNotEmpty()) DiagnosticPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Finish remaining checks", Modifier.weight(1f), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { onAction(DiagnosticsAction.DismissManualTestNotice) }) { Icon(Icons.Default.Close, "Dismiss notice", tint = TextSecondary) }
                }
                Text(uiState.pendingManualItems.joinToString { it.title }, color = TextSecondary, fontSize = 14.sp)
                Text("Use the checks below to complete your report.", color = TextSecondary, fontSize = 13.sp)
            }
            listOf(false to "Automatic checks", true to "Guided tests").forEach { (interactive, title) ->
                val items = uiState.items.filter { it.isInteractive == interactive }
                if (items.isNotEmpty()) {
                    Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    items.forEach { item -> DiagnosticCheckCard(item, busy, onAction) }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun DiagnosticCheckCard(item: DiagnosticItem, busy: Boolean, onAction: (DiagnosticsAction) -> Unit) {
    DiagnosticPanel {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = when (item.category) {
                    DiagnosticCategory.AUTOMATED -> Icons.Outlined.CheckCircle
                    DiagnosticCategory.DISPLAY -> Icons.Outlined.Tv
                    DiagnosticCategory.TOUCHSCREEN -> Icons.Outlined.TouchApp
                    DiagnosticCategory.VIBRATION -> Icons.Outlined.Vibration
                    DiagnosticCategory.FLASHLIGHT -> Icons.Outlined.FlashlightOn
                    DiagnosticCategory.AUDIO -> Icons.Outlined.VolumeUp
                    DiagnosticCategory.SENSORS -> Icons.Outlined.Sensors
                    DiagnosticCategory.BATTERY -> Icons.Outlined.BatteryStd
                },
                contentDescription = null,
                tint = when (item.status) {
                    TestStatus.PASSED -> Color(0xFF10B981)
                    TestStatus.RUNNING -> AccentPrimary
                    TestStatus.FAILED, TestStatus.WARNING -> AccentWarning
                    else -> TextSecondary
                },
                modifier = Modifier.size(24.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(item.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            when (item.status) {
                TestStatus.PASSED -> {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Complete",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                TestStatus.RUNNING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AccentPrimary
                    )
                }
                TestStatus.FAILED, TestStatus.WARNING -> {
                    Text(
                        "Issue",
                        color = AccentWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                TestStatus.NEEDS_CONFIRMATION -> {
                    Text(
                        "Confirm",
                        color = AccentPrimary,
                        fontSize = 13.sp
                    )
                }
                TestStatus.UNSUPPORTED -> {
                    Text(
                        "N/A",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
                TestStatus.PERMISSION_REQUIRED -> {
                    Text(
                        "Permission",
                        color = AccentWarning,
                        fontSize = 13.sp
                    )
                }
                TestStatus.NOT_TESTED -> {
                    Text(
                        "Ready to test",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
        Text(item.description, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        item.detailMessage?.let { Text(it, color = TextSecondary, fontSize = 13.sp) }
        item.nextStep?.let { Text(it, color = TextMuted, fontSize = 12.sp) }
        if (item.status == TestStatus.NEEDS_CONFIRMATION) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(DiagnosticsAction.CompleteInteractiveTest(item.id, true)) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                ) {
                    Text("Yes, it worked", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = { onAction(DiagnosticsAction.CompleteInteractiveTest(item.id, false)) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                ) {
                    Text("Issue found", fontSize = 14.sp)
                }
            }
        }
        OutlinedButton(
            onClick = {
                onAction(
                    if (item.isInteractive) DiagnosticsAction.StartInteractiveTest(item.id)
                    else DiagnosticsAction.RunSingleTest(item.id)
                )
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp)
        ) {
            Text(
                if (item.status == TestStatus.NOT_TESTED) {
                    if (item.isInteractive) "Start guided test" else "Run check"
                } else "Test again",
                fontSize = 14.sp
            )
        }
    }
}
