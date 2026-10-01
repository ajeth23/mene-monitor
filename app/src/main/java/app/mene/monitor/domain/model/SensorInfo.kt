package app.mene.monitor.domain.model

enum class SensorCategory {
    TEMPERATURE,
    FAN,
    MOTION,
    ENVIRONMENT,
    POSITION,
    OTHER
}

data class SensorInfo(
    val id: Int,
    val name: String,
    val typeName: String,
    val vendor: String,
    val category: SensorCategory,
    val valueFormatted: String,
    val unit: String,
    val isAvailable: Boolean = true,
    val powerMa: Float = 0f,
    val maxRange: Float = 0f,
    val resolution: Float = 0f
)
