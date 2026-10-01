package app.mene.monitor.domain.usecase

import app.mene.monitor.domain.model.DiagnosticItem
import app.mene.monitor.domain.repository.DeviceRepository

class RunDiagnosticTestUseCase(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(id: String): DiagnosticItem {
        return repository.runDiagnosticTest(id)
    }
}
