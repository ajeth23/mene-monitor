package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.CpuInfo
import app.mene.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveCpuInfoUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<CpuInfo> {
        return repository.observeCpuInfo()
    }
}
