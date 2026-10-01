package app.mene.monitor

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import android.graphics.Color as AndroidColor
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mene.monitor.presentation.navigation.MeneAppScaffold
import app.mene.monitor.presentation.theme.AppThemeMode
import app.mene.monitor.presentation.theme.MeneMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.navigationBarColor = AndroidColor.TRANSPARENT
        val app = applicationContext as MeneMonitorApp
        val container = app.container

        setContent {
            val themeMode by container.updateSettingsUseCase.observeThemeMode().collectAsStateWithLifecycle(
                initialValue = AppThemeMode.SYSTEM
            )

            MeneMonitorTheme(themeMode = themeMode) {
                MeneAppScaffold()
            }
        }
    }
}
