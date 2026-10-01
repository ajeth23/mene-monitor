package app.mene.monitor.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Cpu : Screen("cpu")
    data object Memory : Screen("memory")
    data object Diagnostics : Screen("diagnostics")
    data object More : Screen("more")

    // Sub-screens
    data object Gpu : Screen("gpu")
    data object Storage : Screen("storage")
    data object Battery : Screen("battery")
    data object Sensors : Screen("sensors")
    data object Network : Screen("network")
    data object SystemInfo : Screen("system_info")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
