package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.SensorInfo
import app.mene.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveSensorsUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<List<SensorInfo>> {
        return repository.observeSensors()
    }
}
