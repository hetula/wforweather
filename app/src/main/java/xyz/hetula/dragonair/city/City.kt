package xyz.hetula.dragonair.city

import xyz.hetula.dragonair.util.NameCorrection

/**
 * @param id City id
 * @param name City name
 * @param country Country code (GB, JP etc.)
 * @param coord City Coordinate
 */
data class City(
    val id: Long,
    val name: String,
    val country: String,
    val coord: Coordinate
) {
    fun getRealName() = NameCorrection.correctName(name)
}
