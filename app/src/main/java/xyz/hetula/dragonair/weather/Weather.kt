package xyz.hetula.dragonair.weather

import xyz.hetula.dragonair.city.Coordinate
import xyz.hetula.dragonair.util.NameCorrection
import java.util.*

/**
 * @param id City ID
 * @param name City name
 * @param cod Internal parameter
 * @param dt Time of data calculation, unix, UTC
 * @param visibility Visibility, meter
 * @param base Internal parameter
 * @param coord City coordinates
 * @param weather Weather conditions
 * @param clouds Cloud conditions
 * @param wind Wind conditions
 * @param main Temperature conditions
 * @param sys Miscallenous information
 */
data class Weather(
    val id: Long,
    val name: String,
    val cod: Int,
    val dt: Long,
    val visibility: Long,
    val base: String,
    val coord: Coordinate,
    val weather: List<WeatherData>,
    val clouds: Clouds,
    val wind: Wind,
    val main: MainData,
    val sys: MiscInfo
) {
    fun getRealName(): String = NameCorrection.correctName(name)

    fun getTime() = Date(dt)
}
