package app.mene.monitor.presentation.diagnostics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.model.TestStatus
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.diagnostics.tests.DisplayTestScreen
import app.mene.monitor.presentation.diagnostics.tests.TouchTestScreen
import app.mene.monitor.presentation.theme.AccentBattery
import app.mene.monitor.presentation.theme.AccentCpu
import app.mene.monitor.presentation.theme.AccentGpu
import app.mene.monitor.presentation.theme.AccentMemory
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.AccentSensors
import app.mene.monitor.presentation.theme.AccentStorage
import app.mene.monitor.presentation.theme.AccentWarning
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.DarkSurfaceVariant
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun DiagnosticsScreen(
    uiState: DiagnosticsUiState,
    onAction: (DiagnosticsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if an interactive test full-screen mode is active
    when (uiState.activeInteractiveTest) {
        "display" -> {
            DisplayTestScreen(
                onComplete = { passed ->
                    onAction(DiagnosticsAction.CompleteInteractiveTest("display", passed))
                }
            )
            return
        }
        "touchscreen" -> {
            TouchTestScreen(
                onComplete = { passed ->
                    onAction(DiagnosticsAction.CompleteInteractiveTest("touchscreen", passed))
                }
            )
            return
        }
    }

    val scrollState = rememberScrollState()

    // Breathing pulse for real-time status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "diag_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diag_pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HardwareHeader(title = "Hardware Diagnostics")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Card: Overall Health Score & Full Scan Trigger with Tactile Feedback
            val heroInteractionSource = remember { MutableInteractionSource() }
            val isHeroPressed by heroInteractionSource.collectIsPressedAsState()
            val heroScale by animateFloatAsState(
                targetValue = if (isHeroPressed) 0.985f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "hero_scale"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = heroScale, scaleY = heroScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated Circular Health Gauge
                    AnimatedHealthGauge(healthScore = uiState.report.healthScorePercentage)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Device Health",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Live breathing status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                (if (uiState.isRunningFullScan) AccentPrimary else AccentMemory)
                                                    .copy(alpha = pulseAlpha)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (uiState.isRunningFullScan) "Testing" else "Ready",
                                        color = if (uiState.isRunningFullScan) AccentPrimary else AccentMemory,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Passed ${uiState.report.passedTests} of ${uiState.report.totalTests} hardware checks",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time scan progress indicator if running
                if (uiState.isRunningFullScan) {
                    val currentItem = uiState.items.firstOrNull { it.id == uiState.currentRunningTestId }
                    val progressValue = if (uiState.totalAutomatedTests > 0) {
                        (uiState.currentProgressIndex.toFloat() / uiState.totalAutomatedTests).coerceIn(0f, 1f)
                    } else 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressValue,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "scan_progress_bar"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scanning ${currentItem?.title ?: "Hardware"}...",
                                color = AccentPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.currentProgressIndex}/${uiState.totalAutomatedTests}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(DarkBackground.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(AccentPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Scan Button with tactile bounce
                val buttonInteractionSource = remember { MutableInteractionSource() }
                val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    targetValue = if (isButtonPressed) 0.97f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "btn_scale"
                )

                Button(
                    onClick = { onAction(DiagnosticsAction.RunFullScan) },
                    enabled = !uiState.isRunningFullScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    interactionSource = buttonInteractionSource
                ) {
                    if (uiState.isRunningFullScan) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Running Diagnostics...",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run Automated Hardware Scan",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Manual Test Notice Banner (Prompting User for Display & Touch Tests)
            AnimatedVisibility(
                visible = uiState.showManualTestNotice,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Manual Verification Required",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { onAction(DiagnosticsAction.DismissManualTestNotice) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Automated scan does not test Display & Touchscreen. Please run these tests manually to inspect dead pixels and touch responsiveness.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAction(DiagnosticsAction.StartInteractiveTest("display")) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Test Display",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onAction(DiagnosticsAction.StartInteractiveTest("touchscreen")) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMemory),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Test Touch",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = "Diagnostic Checklists",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Individual Diagnostic Items with Tactile Animations
            uiState.items.forEach { item ->
                DiagnosticCard(
                    item = item,
                    onRunTest = {
                        if (item.isInteractive) {
                            onAction(DiagnosticsAction.StartInteractiveTest(item.id))
                        } else {
                            onAction(DiagnosticsAction.RunSingleTest(item.id))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnimatedHealthGauge(
    healthScore: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = healthScore,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "diag_health_num"
    )

    val sweepAngleProgress by animateFloatAsState(
        targetValue = (healthScore / 100f).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "diag_health_sweep"
    )

    val scoreColor = when {
        healthScore >= 80 -> AccentMemory
        healthScore >= 50 -> AccentWarning
        else -> Color(0xFFEF4444)
    }

    val trackColor = DarkSurfaceVariant

    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeftOffset = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Animated Foreground Arc
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = sweepAngleProgress * 360f,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore}%",
                color = scoreColor,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Health",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DiagnosticCard(
    item: DiagnosticItem,
    onRunTest: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "diag_item_scale_${item.id}"
    )

    val (categoryColor, categoryIcon) = when (item.id) {
        "display" -> Pair(AccentPrimary, Icons.Default.Tv)
        "touchscreen" -> Pair(AccentMemory, Icons.Default.TouchApp)
        "vibration" -> Pair(AccentSensors, Icons.Default.Vibration)
        "flashlight" -> Pair(AccentWarning, Icons.Default.FlashlightOn)
        "audio" -> Pair(AccentGpu, Icons.Default.VolumeUp)
        "sensors" -> Pair(AccentStorage, Icons.Default.Sensors)
        "battery" -> Pair(AccentBattery, Icons.Default.BatteryChargingFull)
        else -> Pair(AccentPrimary, Icons.Default.Build)
    }

    val (statusColor, statusText) = when (item.status) {
        TestStatus.PASSED -> Pair(AccentMemory, "Passed")
        TestStatus.FAILED -> Pair(Color(0xFFEF4444), "Failed")
        TestStatus.WARNING -> Pair(AccentWarning, "Warning")
        TestStatus.RUNNING -> Pair(AccentPrimary, "Testing...")
        TestStatus.NOT_TESTED -> Pair(TextMuted, if (item.isInteractive) "Manual Test" else "Not Tested")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onRunTest
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean Colored Category Icon (no box, no stroke)
            Icon(
                imageVector = categoryIcon,
                contentDescription = item.title,
                tint = categoryColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        if (item.status == TestStatus.RUNNING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                color = AccentPrimary,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (item.status == TestStatus.PASSED) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentMemory,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (item.status == TestStatus.FAILED) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        AnimatedVisibility(visible = item.detailMessage != null) {
            item.detailMessage?.let { detail ->
                Text(
                    text = detail,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 10.dp, start = 42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isInteractive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Interactive Screen Test",
                            color = AccentPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            OutlinedButton(
                onClick = onRunTest,
                enabled = item.status != TestStatus.RUNNING,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (item.isInteractive) AccentPrimary else AccentCpu
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = when {
                        item.status == TestStatus.PASSED || item.status == TestStatus.FAILED -> Icons.Default.Refresh
                        item.isInteractive -> Icons.Default.TouchApp
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        item.isInteractive -> "Start Test ➔"
                        item.status == TestStatus.PASSED || item.status == TestStatus.FAILED -> "Retest"
                        else -> "Test Now"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
