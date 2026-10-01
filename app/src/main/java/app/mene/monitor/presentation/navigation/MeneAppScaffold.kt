package app.mene.monitor.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.mene.monitor.MeneMonitorApp
import app.mene.monitor.presentation.battery.BatteryAction
import app.mene.monitor.presentation.battery.BatteryScreen
import app.mene.monitor.presentation.battery.BatteryViewModel
import app.mene.monitor.presentation.cpu.CpuAction
import app.mene.monitor.presentation.cpu.CpuScreen
import app.mene.monitor.presentation.cpu.CpuViewModel
import app.mene.monitor.presentation.diagnostics.DiagnosticsScreen
import app.mene.monitor.presentation.diagnostics.DiagnosticsViewModel
import app.mene.monitor.presentation.gpu.GpuAction
import app.mene.monitor.presentation.gpu.GpuScreen
import app.mene.monitor.presentation.gpu.GpuViewModel
import app.mene.monitor.presentation.home.HomeScreen
import app.mene.monitor.presentation.home.HomeViewModel
import app.mene.monitor.presentation.memory.MemoryAction
import app.mene.monitor.presentation.memory.MemoryScreen
import app.mene.monitor.presentation.memory.MemoryViewModel
import app.mene.monitor.presentation.more.AboutScreen
import app.mene.monitor.presentation.more.MoreScreen
import app.mene.monitor.presentation.network.NetworkAction
import app.mene.monitor.presentation.network.NetworkScreen
import app.mene.monitor.presentation.network.NetworkViewModel
import app.mene.monitor.presentation.sensors.SensorsAction
import app.mene.monitor.presentation.sensors.SensorsScreen
import app.mene.monitor.presentation.sensors.SensorsViewModel
import app.mene.monitor.presentation.settings.SettingsScreen
import app.mene.monitor.presentation.settings.SettingsViewModel
import app.mene.monitor.presentation.storage.StorageAction
import app.mene.monitor.presentation.storage.StorageScreen
import app.mene.monitor.presentation.storage.StorageViewModel
import app.mene.monitor.presentation.system.SystemAction
import app.mene.monitor.presentation.system.SystemScreen
import app.mene.monitor.presentation.system.SystemViewModel
import app.mene.monitor.presentation.theme.DarkBackground

@Composable
fun MeneAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val app = LocalContext.current.applicationContext as MeneMonitorApp
    val container = app.container

    val mainRoutes = setOf(
        Screen.Home.route,
        Screen.Cpu.route,
        Screen.Memory.route,
        Screen.Diagnostics.route,
        Screen.More.route
    )
    val shouldShowBottomBar = currentRoute in mainRoutes

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (shouldShowBottomBar) {
                MeneBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                // 1. HOME SCREEN
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.provideFactory(
                            container.getDeviceInfoUseCase,
                            container.observeCpuInfoUseCase,
                            container.observeGpuInfoUseCase,
                            container.observeMemoryInfoUseCase,
                            container.observeStorageInfoUseCase,
                            container.observeBatteryInfoUseCase,
                            container.observeNetworkInfoUseCase
                        )
                    )
                    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                    HomeScreen(
                        uiState = uiState,
                        onNavigateCpu = { navController.navigate(Screen.Cpu.route) },
                        onNavigateGpu = { navController.navigate(Screen.Gpu.route) },
                        onNavigateMemory = { navController.navigate(Screen.Memory.route) },
                        onNavigateStorage = { navController.navigate(Screen.Storage.route) },
                        onNavigateBattery = { navController.navigate(Screen.Battery.route) },
                        onNavigateNetwork = { navController.navigate(Screen.Network.route) },
                        onNavigateDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                        onNavigateSystemInfo = { navController.navigate(Screen.SystemInfo.route) },
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                // 2. CPU SCREEN
                composable(Screen.Cpu.route) {
                    val cpuViewModel: CpuViewModel = viewModel(
                        factory = CpuViewModel.provideFactory(container.observeCpuInfoUseCase)
                    )
                    val uiState by cpuViewModel.uiState.collectAsStateWithLifecycle()

                    CpuScreen(
                        uiState = uiState,
                        onRefreshClick = { cpuViewModel.onAction(CpuAction.Refresh) }
                    )
                }

                // 3. GPU SCREEN
                composable(Screen.Gpu.route) {
                    val gpuViewModel: GpuViewModel = viewModel(
                        factory = GpuViewModel.provideFactory(container.observeGpuInfoUseCase)
                    )
                    val uiState by gpuViewModel.uiState.collectAsStateWithLifecycle()

                    GpuScreen(
                        uiState = uiState,
                        onBackClick = { navController.navigateUp() },
                        onRefreshClick = { gpuViewModel.onAction(GpuAction.Refresh) }
                    )
                }

                // 4. MEMORY SCREEN
                composable(Screen.Memory.route) {
                    val memoryViewModel: MemoryViewModel = viewModel(
                        factory = MemoryViewModel.provideFactory(container.observeMemoryInfoUseCase)
                    )
                    val uiState by memoryViewModel.uiState.collectAsStateWithLifecycle()

                    MemoryScreen(
                        uiState = uiState,
                        onRefreshClick = { memoryViewModel.onAction(MemoryAction.Refresh) }
                    )
                }

                // 5. DIAGNOSTICS SCREEN
                composable(Screen.Diagnostics.route) {
                    val diagnosticsViewModel: DiagnosticsViewModel = viewModel(
                        factory = DiagnosticsViewModel.provideFactory(
                            container.getDiagnosticItemsUseCase,
                            container.runDiagnosticTestUseCase
                        )
                    )
                    val uiState by diagnosticsViewModel.uiState.collectAsStateWithLifecycle()

                    DiagnosticsScreen(
                        uiState = uiState,
                        onAction = { action -> diagnosticsViewModel.onAction(action) }
                    )
                }

                // 5. MORE HUB SCREEN
                composable(Screen.More.route) {
                    MoreScreen(
                        onNavigateStorage = { navController.navigate(Screen.Storage.route) },
                        onNavigateBattery = { navController.navigate(Screen.Battery.route) },
                        onNavigateSensors = { navController.navigate(Screen.Sensors.route) },
                        onNavigateNetwork = { navController.navigate(Screen.Network.route) },
                        onNavigateSystemInfo = { navController.navigate(Screen.SystemInfo.route) },
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateAbout = { navController.navigate(Screen.About.route) }
                    )
                }

                // 6. STORAGE SCREEN
                composable(Screen.Storage.route) {
                    val storageViewModel: StorageViewModel = viewModel(
                        factory = StorageViewModel.provideFactory(container.observeStorageInfoUseCase)
                    )
                    val uiState by storageViewModel.uiState.collectAsStateWithLifecycle()

                    StorageScreen(
                        uiState = uiState,
                        onBackClick = { navController.navigateUp() },
                        onRefreshClick = { storageViewModel.onAction(StorageAction.Refresh) }
                    )
                }

                // 7. BATTERY SCREEN
                composable(Screen.Battery.route) {
                    val batteryViewModel: BatteryViewModel = viewModel(
                        factory = BatteryViewModel.provideFactory(container.observeBatteryInfoUseCase)
                    )
                    val uiState by batteryViewModel.uiState.collectAsStateWithLifecycle()

                    BatteryScreen(
                        uiState = uiState,
                        onBackClick = { navController.navigateUp() },
                        onRefreshClick = { batteryViewModel.onAction(BatteryAction.Refresh) }
                    )
                }

                // 8. SENSORS SCREEN
                composable(Screen.Sensors.route) {
                    val sensorsViewModel: SensorsViewModel = viewModel(
                        factory = SensorsViewModel.provideFactory(container.observeSensorsUseCase)
                    )
                    val uiState by sensorsViewModel.uiState.collectAsStateWithLifecycle()

                    SensorsScreen(
                        uiState = uiState,
                        onCategorySelected = { cat -> sensorsViewModel.onAction(SensorsAction.SelectCategory(cat)) },
                        onBackClick = { navController.navigateUp() },
                        onRefreshClick = { sensorsViewModel.onAction(SensorsAction.Refresh) }
                    )
                }

                // 9. NETWORK SCREEN
                composable(Screen.Network.route) {
                    val networkViewModel: NetworkViewModel = viewModel(
                        factory = NetworkViewModel.provideFactory(container.observeNetworkInfoUseCase)
                    )
                    val uiState by networkViewModel.uiState.collectAsStateWithLifecycle()

                    NetworkScreen(
                        uiState = uiState,
                        onBackClick = { navController.navigateUp() },
                        onRefreshClick = { networkViewModel.onAction(NetworkAction.Refresh) }
                    )
                }

                // 10. SYSTEM INFO SCREEN
                composable(Screen.SystemInfo.route) {
                    val systemViewModel: SystemViewModel = viewModel(
                        factory = SystemViewModel.provideFactory(container.getSystemInfoUseCase)
                    )
                    val uiState by systemViewModel.uiState.collectAsStateWithLifecycle()

                    SystemScreen(
                        uiState = uiState,
                        onBackClick = { navController.navigateUp() },
                        onRefreshClick = { systemViewModel.onAction(SystemAction.Refresh) }
                    )
                }

                // 11. SETTINGS SCREEN
                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.provideFactory(container.updateSettingsUseCase)
                    )
                    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                    SettingsScreen(
                        uiState = uiState,
                        onAction = { action -> settingsViewModel.onAction(action) },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                // 12. ABOUT SCREEN
                composable(Screen.About.route) {
                    AboutScreen(onBackClick = { navController.navigateUp() })
                }
            }
        }
    }
}
