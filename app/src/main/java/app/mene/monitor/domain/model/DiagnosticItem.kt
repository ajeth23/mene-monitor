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
    WARNING,
    NEEDS_CONFIRMATION,
    UNSUPPORTED,
    PERMISSION_REQUIRED
}

data class DiagnosticItem(
    val id: String,
    val title: String,
    val description: String,
    val category: DiagnosticCategory,
    val status: TestStatus = TestStatus.NOT_TESTED,
    val detailMessage: String? = null,
    val nextStep: String? = null,
    val isInteractive: Boolean = false
)

data class DiagnosticsReport(
    val healthScorePercentage: Int = 0,
    val totalTests: Int = 0,
    val passedTests: Int = 0,
    val items: List<DiagnosticItem> = emptyList()
) {
    val completedTests: Int get() = items.count { it.status in setOf(TestStatus.PASSED, TestStatus.FAILED, TestStatus.WARNING) }
    val failedTests: Int get() = items.count { it.status == TestStatus.FAILED }
    val warningTests: Int get() = items.count { it.status == TestStatus.WARNING }
    val pendingConfirmationTests: Int get() = items.count { it.status == TestStatus.NEEDS_CONFIRMATION }
    val unsupportedTests: Int get() = items.count { it.status == TestStatus.UNSUPPORTED }
    val permissionRequiredTests: Int get() = items.count { it.status == TestStatus.PERMISSION_REQUIRED }
    val completionPercentage: Int get() = if (totalTests == 0) 0 else completedTests * 100 / totalTests
}
