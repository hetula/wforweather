package xyz.hetula.dragonair.weather

/**
 * @param temp Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
 * @param pressure Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
 * @param humidity Humidity, %
 * @param temp_min Minimum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
 * @param temp_max Maximum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
 */
data class MainData(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double
)
