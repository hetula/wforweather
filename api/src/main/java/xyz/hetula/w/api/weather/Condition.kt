package xyz.hetula.w.api.weather

enum class Condition(private val iconId: String) {
    UNKNOWN("--No-ID--"),
    CLEAR_SKY("01"),
    FEW_CLOUDS("02"),
    SCATTERED_CLOUDS("03"),
    BROKEN_CLOUDS("04"),
    SHOWER_RAIN("09"),
    RAIN("10"),
    THUNDERSTORM("11"),
    SNOW("13"),
    MIST("50");

    fun matches(weatherCondition: String): Boolean {
        return weatherCondition.startsWith(iconId)
    }
}
