package xyz.hetula.dragonair

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import xyz.hetula.dragonair.city.City
import xyz.hetula.dragonair.util.GsonHelper
import xyz.hetula.dragonair.weather.Weather
import xyz.hetula.dragonair.weather.WeatherManager
import java.io.*

class DragonairService : Service() {
    private val mWeatherManager: WeatherManager = WeatherManager()

    private lateinit var mLastWeatherFile: File

    private var mCurrentCity: City? = null
    private var mLastWeather: Weather? = null

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Actually create the binder!
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mLastWeatherFile = File(applicationContext.cacheDir, "last_weather")
        mWeatherManager.initialize(applicationContext)
        readLastWeather()
    }

    override fun onDestroy() {
        mWeatherManager.close()
        super.onDestroy()
    }

    private fun fetchCurrentCityWeather() {
        val city = mCurrentCity
        if (city == null) {
            Log.w(TAG, "No current city set! Can't fetch data!")
        } else {
            mWeatherManager.fetchCurrentWeather(city) {
                Log.d(TAG, "Weather: $it")
                storeLastWeather(it)
            }
        }
    }

    private fun readLastWeather() {
        if(!mLastWeatherFile.exists()) {
            return
        }
        BufferedReader(FileReader(mLastWeatherFile)).use {
            mLastWeather = GsonHelper.readWeather(Gson(), it)
        }
    }

    private fun storeLastWeather(weather: Weather) {
        mLastWeather = weather
        BufferedWriter(FileWriter(mLastWeatherFile)).use {
            it.write(Gson().toJson(weather))
        }
    }

    companion object {
        private const val TAG = "DragonairService"
    }
}
