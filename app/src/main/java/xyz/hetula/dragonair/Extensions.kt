package xyz.hetula.dragonair

import xyz.hetula.dragonair.weather.Weather
import xyz.hetula.dragonair.weather.WeatherData

fun Weather?.getTemperatureAsCelsius(): Double {
    val temp: Double = this?.main?.temp ?: return 0.0
    return temp - Constants.Weather.KELVIN_TO_CELSIUS
}

fun Weather?.getConditions(): String {
    val currentConditions: WeatherData = this?.weather?.getOrNull(0) ?: return ""
    return currentConditions.main
}

fun Weather?.getFirstWeatherData(): WeatherData? {
    return this?.weather?.getOrNull(0)
}
