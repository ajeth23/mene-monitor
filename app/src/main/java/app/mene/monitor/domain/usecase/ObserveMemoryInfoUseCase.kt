package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.MemoryInfo
import app.mene.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveMemoryInfoUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<MemoryInfo> {
        return repository.observeMemoryInfo()
    }
}
