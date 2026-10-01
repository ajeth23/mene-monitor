package app.mene.monitor.domain.model

enum class DiagnosticCategory {
    AUTOMATED,
    DISPLAY,
    TOUCHSCREEN,
    VIBRATION,
    FLASHLIGHT,
    AUDIO,
    SENSORS,
    BATTERY
}

enum class TestStatus {
    NOT_TESTED,
    RUNNING,
    PASSED,
    FAILED,
    WARNING
}

data class DiagnosticItem(
    val id: String,
    val title: String,
    val description: String,
    val category: DiagnosticCategory,
    val status: TestStatus = TestStatus.NOT_TESTED,
    val detailMessage: String? = null,
    val isInteractive: Boolean = false
)

data class DiagnosticsReport(
    val healthScorePercentage: Int = 100,
    val totalTests: Int = 0,
    val passedTests: Int = 0,
    val items: List<DiagnosticItem> = emptyList()
)
