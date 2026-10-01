package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.SystemInfo
import app.mene.monitor.domain.repository.DeviceRepository

class GetSystemInfoUseCase(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): SystemInfo {
        return repository.getSystemInfo()
    }
}
