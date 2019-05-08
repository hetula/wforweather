package xyz.hetula.w.api.weather

/**
 * @param type Internal parameter
 * @param id Internal parameter
 * @param message Internal parameter
 * @param country Country code (GB, JP etc.)
 * @param sunrise Sunrise time, unix, UTC
 * @param sunset Sunset time, unix, UTC
 */
data class MiscInfo(
    val type: Int,
    val id: Long,
    val message: Double,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)
