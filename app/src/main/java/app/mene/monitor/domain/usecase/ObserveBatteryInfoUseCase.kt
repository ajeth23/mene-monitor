package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.BatteryInfo
import app.mene.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveBatteryInfoUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<BatteryInfo> {
        return repository.observeBatteryInfo()
    }
}
