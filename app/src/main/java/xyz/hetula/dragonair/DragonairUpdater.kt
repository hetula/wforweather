package xyz.hetula.dragonair

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class DragonairUpdater : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        Log.d(TAG, "Intent: $intent")
        val action = intent?.action ?: return

        val cityId = intent.getLongExtra("id", -1)
        if(cityId != -1L) {
            Dragonair.setCityIdIfNotPresent(context, cityId)
        }
        when (action) {
            Constants.Intents.ACTION_UPDATE_WEATHER -> {
                Log.d(TAG, "Received update request!")
                Dragonair.fetchCurrentCityWeather(context)
            }
            Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY -> {
                Log.d(TAG, "Received timely update request!")
                Dragonair.fetchCurrentCityWeatherAndAllocateNewSchelude(context)
            }
        }
    }

    companion object {
        private const val TAG = "DragonairUpdater"
    }
}
