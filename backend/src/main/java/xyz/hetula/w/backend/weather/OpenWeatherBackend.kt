package xyz.hetula.w.backend.weather

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import xyz.hetula.w.api.weather.Weather
import xyz.hetula.w.api.weather.WeatherBackend
import xyz.hetula.w.backend.R
import xyz.hetula.w.backend.util.GsonRequest
import java.util.*

class OpenWeatherBackend : WeatherBackend {
    private var mInitialized = false
    private lateinit var mApiKey: String
    private lateinit var mReqQueue: RequestQueue

    fun initialize(context: Context) {
        if (!mInitialized) {
            mApiKey = context.getString(R.string.api_key)
            mReqQueue = Volley.newRequestQueue(context.applicationContext)
            mInitialized = true
        }
    }

    fun close() {
        if (mInitialized) {
            mReqQueue.stop()
            mInitialized = false
        }
    }

    fun fetchCurrentWeather(cityId: Long, callback: (Weather) -> Unit) {
        if (!mInitialized) {
            Log.e(TAG, "Not initialized, no weather")
            return
        }
        mReqQueue.add(
            GsonRequest(
                currentWeatherUrl(mApiKey, cityId),
                Weather::class.java,
                HashMap(),
                callback,
                Err()
            )
        )
    }

    private fun currentWeatherUrl(apiKey: String, cityId: Long): String {
        return String.format(Locale.ROOT, CURRENT_WEATHER_API_URL, cityId, apiKey)
    }

    private class Err : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            Log.e("W", "VolleyErr: $error")
        }
    }

    companion object {
        private const val TAG = "OpenWeatherBackend"
        private const val CURRENT_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=%d&APPID=%s"
    }
}
