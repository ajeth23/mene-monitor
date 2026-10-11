package app.mene.monitor.presentation.more

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.BuildConfig
import app.mene.monitor.R
import app.mene.monitor.presentation.components.HardwareHeader
import app.mene.monitor.presentation.theme.AccentPrimary
import app.mene.monitor.presentation.theme.DarkBackground
import app.mene.monitor.presentation.theme.DarkSurface
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextPrimary
import app.mene.monitor.presentation.theme.TextSecondary

@Composable
fun AboutScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    fun openLink(url: String) {
        try {
            uriHandler.openUri(url)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No browser found to open link", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier.fillMaxSize().background(DarkBackground)) {
        HardwareHeader(title = "About", onBackClick = onBackClick)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(DarkSurface).padding(14.dp), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_menemonitor), contentDescription = "Mene Monitor app icon", tint = Color.Unspecified, modifier = Modifier.fillMaxSize())
                }
                Text("Mene Monitor", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Version ${BuildConfig.VERSION_NAME}", color = TextMuted, fontSize = 14.sp)
                Text("Understand your device. Monitor its performance. Test its hardware.",
                    color = TextSecondary, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center)
            }
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DarkSurface).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White).padding(10.dp),
                        contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_mene), contentDescription = "Mene company logo",
                            tint = Color.Unspecified, modifier = Modifier.fillMaxSize())
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Created by", color = TextMuted, fontSize = 14.sp)
                        Text("Mene", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Software, AI, and digital products.", color = TextSecondary, fontSize = 16.sp)
                Button(
                    onClick = { openLink("https://mene.app") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("about_website"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = if (isDark) Color.Black else Color.White
                    )
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Visit mene.app", modifier = Modifier.padding(start = 10.dp), fontSize = 16.sp)
                }
            }
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DarkSurface).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Built in the open", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("Mene Monitor is open source under the Apache License 2.0. Explore the code, report an issue, or contribute on GitHub.",
                    color = TextSecondary, fontSize = 16.sp, lineHeight = 24.sp)
                OutlinedButton(
                    onClick = { openLink("https://github.com/ajeth23/mene-monitor") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("about_source"),
                    shape = RoundedCornerShape(14.dp),
                    border = null
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("View source on GitHub", modifier = Modifier.padding(start = 10.dp), fontSize = 16.sp, color = TextPrimary)
                }
            }
        }
    }
}
