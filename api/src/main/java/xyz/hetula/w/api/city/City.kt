package xyz.hetula.w.api.city

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
) : Comparable<City> {

    override fun compareTo(other: City): Int {
        return name.compareTo(other.name)
    }

}
