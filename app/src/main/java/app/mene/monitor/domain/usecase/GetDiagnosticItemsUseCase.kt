package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.repository.DeviceRepository

class GetDiagnosticItemsUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): List<DiagnosticItem> {
        return repository.getDiagnosticItems()
    }
}
