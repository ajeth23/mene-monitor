package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.DeviceInfo
import app.mene.monitor.domain.repository.DeviceRepository

class GetDeviceInfoUseCase(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): DeviceInfo {
        return repository.getDeviceInfo()
    }
}
