package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.repository.DeviceRepository

class UpdateSettingsUseCase(
    private val repository: DeviceRepository
) {
    fun setRefreshInterval(seconds: Long) {
        repository.setRefreshInterval(seconds)
    }

    fun getRefreshInterval(): Long {
        return repository.getRefreshInterval()
    }

    fun setUseFahrenheit(enabled: Boolean) {
        repository.setUseFahrenheit(enabled)
    }

    fun isUseFahrenheit(): Boolean {
        return repository.isUseFahrenheit()
    }

    fun setThemeMode(mode: app.mene.monitor.presentation.theme.AppThemeMode) {
        repository.setThemeMode(mode)
    }

    fun getThemeMode(): app.mene.monitor.presentation.theme.AppThemeMode {
        return repository.getThemeMode()
    }

    fun observeThemeMode(): kotlinx.coroutines.flow.Flow<app.mene.monitor.presentation.theme.AppThemeMode> {
        return repository.observeThemeMode()
    }
}
