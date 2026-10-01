package app.mene.monitor.presentation.diagnostics.tests

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.mene.monitor.presentation.theme.AccentCpu
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkSurface

@Composable
fun TouchTestScreen(
    onComplete: (passed: Boolean) -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            onDispose {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            onDispose {}
        }
    }

    val rows = 14
    val cols = 8
    val touchedGrid = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cellWidth = size.width / cols
                            val cellHeight = size.height / rows
                            val col = (offset.x / cellWidth).toInt().coerceIn(0, cols - 1)
                            val row = (offset.y / cellHeight).toInt().coerceIn(0, rows - 1)
                            touchedGrid[Pair(row, col)] = true
                        },
                        onDrag = { change, _ ->
                            val cellWidth = size.width / cols
                            val cellHeight = size.height / rows
                            val col = (change.position.x / cellWidth).toInt().coerceIn(0, cols - 1)
                            val row = (change.position.y / cellHeight).toInt().coerceIn(0, rows - 1)
                            touchedGrid[Pair(row, col)] = true
                        }
                    )
                }
        ) {
            val cellWidth = this.maxWidth / cols
            val cellHeight = this.maxHeight / rows

            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until cols) {
                            val isTouched = touchedGrid[Pair(r, c)] == true
                            Box(
                                modifier = Modifier
                                    .size(cellWidth, cellHeight)
                                    .background(if (isTouched) AccentPrimary.copy(alpha = 0.85f) else DarkSurface)
                                    .border(0.5.dp, Color(0xFF2A2E3D))
                            )
                        }
                    }
                }
            }
        }

        val totalCells = rows * cols
        val touchedCount = touchedGrid.size
        val progressPct = (touchedCount * 100) / totalCells

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Full Panel Touch Matrix: $progressPct% ($touchedCount/$totalCells)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Drag finger over the entire grid to test 100% touch coverage.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onComplete(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCpu)
                ) {
                    Text("Dead Zones (Fail)", fontSize = 12.sp)
                }

                Button(
                    onClick = { onComplete(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) {
                    Text("Touch OK (Pass)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
