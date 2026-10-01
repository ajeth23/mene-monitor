package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.StorageInfo
import app.mene.monitor.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveStorageInfoUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<StorageInfo> {
        return repository.observeStorageInfo()
    }
}
