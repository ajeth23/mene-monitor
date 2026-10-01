package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.GpuInfo
import app.mene.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveGpuInfoUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<GpuInfo> {
        return repository.observeGpuInfo()
    }
}
