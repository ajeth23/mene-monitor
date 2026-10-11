package app.mene.monitor.presentation.memory

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.mene.monitor.presentation.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class ManageableApp(val packageName: String, val label: String, val icon: ImageBitmap?)

@Composable
fun MemoryAppsCard() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var apps by remember { mutableStateOf<List<ManageableApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var revision by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ManageableApp?>(null) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(revision) {
        loading = true
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                context.packageManager.queryIntentActivities(intent, 0)
                    .map { ManageableApp(it.activityInfo.packageName, it.loadLabel(context.packageManager).toString(),
                        runCatching { it.loadIcon(context.packageManager).toBitmap(width = 96, height = 96).asImageBitmap() }.getOrNull()) }
                    .distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
            }
        }
        apps = result.getOrDefault(emptyList())
        loadFailed = result.isFailure
        loading = false
    }
    fun openSettings(packageName: String?) {
        val intent = if (packageName == null) Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        try { context.startActivity(intent) }
        catch (_: ActivityNotFoundException) { Toast.makeText(context, "App settings are unavailable on this device", Toast.LENGTH_SHORT).show() }
    }
    Surface(color = DarkSurface, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Apps & memory", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("Android manages RAM automatically. To address a slow or unresponsive app, review it in Android settings.", color = TextSecondary, fontSize = 16.sp)
            OutlinedTextField(value = search, onValueChange = { search = it; expanded = false }, label = { Text("Search apps") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Apps with a launcher icon · ${apps.size}", color = TextSecondary, fontSize = 16.sp)
            Text("This is an app list, not a list of running processes or per-app RAM usage.", color = TextSecondary, fontSize = 14.sp)
            val filtered = apps.filter { it.label.contains(search, ignoreCase = true) || it.packageName.contains(search, ignoreCase = true) }
            when {
                loading -> Text("Loading apps…", color = TextSecondary, fontSize = 16.sp)
                loadFailed -> Text("Could not load apps. You can still use Android app settings below.", color = TextSecondary, fontSize = 16.sp)
                filtered.isEmpty() -> Text("No apps found", color = TextSecondary, fontSize = 16.sp)
                else -> {
                    filtered.take(if (expanded) filtered.size else 8).forEach { app ->
                        OutlinedButton(onClick = { selected = app }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (app.icon != null) Image(app.icon, contentDescription = null, modifier = Modifier.size(36.dp))
                                else Icon(Icons.Outlined.Apps, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.label, color = TextPrimary, fontSize = 16.sp)
                                    Text(app.packageName, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    if (filtered.size > 8) TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(if (expanded) "Show fewer apps" else "Show all ${filtered.size} apps", fontSize = 16.sp)
                    }
                }
            }
            OutlinedButton(onClick = { openSettings(null) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("All apps in Android settings", fontSize = 16.sp) }
        }
    }
    if (selected != null) AlertDialog(
        onDismissRequest = { selected = null },
        title = { Text("Review ${selected?.label}") },
        text = { Text("Save your work first. In Android app settings, choose an app and use Force stop if needed. This can interrupt unsaved work, downloads, and notifications until you open the app again.\n\nForce stop does not uninstall the app or clear its stored data. Avoid Clear storage unless you intend to reset the app. Android may reclaim RAM or restart processes later; freed memory is not guaranteed.") },
        confirmButton = { TextButton(onClick = {
            val packageName = selected?.packageName
            selected = null
            openSettings(packageName)
        }) { Text("Open app settings") } },
        dismissButton = { TextButton(onClick = { selected = null }) { Text("Cancel") } }
    )
}
