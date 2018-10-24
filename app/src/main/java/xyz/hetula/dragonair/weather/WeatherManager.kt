package xyz.hetula.dragonair.weather

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import xyz.hetula.dragonair.R
import xyz.hetula.dragonair.Constants
import xyz.hetula.dragonair.city.City
import xyz.hetula.dragonair.util.GsonRequest
import java.util.*

class WeatherManager {
    private lateinit var mApiKey: String
    private lateinit var mReqQueue: RequestQueue

    fun initialize(context: Context) {
        mApiKey = context.getString(R.string.api_key)
        mReqQueue = Volley.newRequestQueue(context.applicationContext)
    }

    fun close() {
        mReqQueue.stop()
    }

    fun fetchCurrentWeather(city: City, callback: (Weather) -> Unit) {
        mReqQueue.add(GsonRequest(
            currentWeatherUrl(mApiKey, city),
            Weather::class.java,
            HashMap(),
            callback,
            Err()
        ))
    }

    fun currentWeatherUrl(apiKey: String , city: City): String {
        return String.format(Locale.ROOT, Constants.Api.CURRENT_WEATHER_API_URL, city.id, apiKey);
    }

    private class Err : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            Log.e("Dragonair", "VolleyErr: $error")
        }
    }
}
