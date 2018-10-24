package xyz.hetula.dragonair.weather

/**
 * @param speed Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
 * @param deg Wind direction, degrees (meteorological)
 */
data class Wind(
    val speed: Double,
    val deg: Int
)
