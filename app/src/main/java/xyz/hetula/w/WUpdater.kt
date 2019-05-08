package xyz.hetula.w

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class WUpdater : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        Log.d(TAG, "Intent: $intent")
        val action = intent?.action ?: return

        if (W.isNotReady()) {
            Log.w(TAG, "W instance not ready! Initializing!")
            W.initialize(context)
        }

        val cityId = intent.getLongExtra("id", -1)
        if (cityId != -1L) {
            W.setCityIdIfNotPresent(context, cityId)
        }
        when (action) {
            Constants.Intents.ACTION_UPDATE_WEATHER -> {
                Log.d(TAG, "Received update request!")
                W.fetchCurrentCityWeather(context)
            }
            Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY -> {
                Log.d(TAG, "Received timely update request!")
                W.fetchCurrentCityWeatherAndAllocateNewSchelude(context)
            }
        }
    }

    companion object {
        private const val TAG = "WUpdater"
    }
}
