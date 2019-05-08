package xyz.hetula.w.api.weather

/**
 * @param id Weather condition id
 * @param main Group of weather parameters (Rain, Snow, Extreme etc.)
 * @param description Weather condition within the group
 * @param icon Weather icon id
 */
data class WeatherData(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String
)
