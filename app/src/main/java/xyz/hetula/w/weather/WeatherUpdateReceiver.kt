package xyz.hetula.w.weather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import xyz.hetula.w.util.Constants


class WeatherUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        Log.d(TAG, "Intent: $intent")
        val action = intent?.action ?: return

        if (Weather.isNotReady()) {
            Log.w(TAG, "Weather instance not ready! Initializing!")
            Weather.initialize(context)
        }

        val cityId = intent.getLongExtra("id", -1)
        if (cityId != -1L) {
            Weather.setCityIdIfNotPresent(context, cityId)
        }
        when (action) {
            Constants.Intents.ACTION_UPDATE_WEATHER -> {
                Log.d(TAG, "Received update request!")
                Weather.fetchCurrentCityWeather(context)
            }
            Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY -> {
                Log.d(TAG, "Received timely update request!")
                Weather.fetchCurrentCityWeatherAndAllocateNewSchelude(context)
            }
        }
    }

    companion object {
        private const val TAG = "WeatherUpdateReceiver"
    }
}
