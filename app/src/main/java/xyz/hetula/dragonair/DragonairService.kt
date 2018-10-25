package xyz.hetula.dragonair

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
import com.google.gson.Gson
import xyz.hetula.dragonair.city.City
import xyz.hetula.dragonair.city.Coordinate
import xyz.hetula.dragonair.util.GsonHelper
import xyz.hetula.dragonair.weather.Weather
import xyz.hetula.dragonair.weather.WeatherManager
import java.io.*
import kotlin.math.roundToInt

class DragonairService : Service() {
    private val mWeatherManager: WeatherManager = WeatherManager()

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mLastWeatherFile: File

    private var mCurrentCity: City? = null
    private var mLastWeather: Weather? = null
    private var mLastWeatherFetch = -1L

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Add real binder to provide access to update schedules etc.
        return NoOpBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_STICKY
        if(intent.action == Constants.Intents.ACTION_UPDATE_WEATHER) {
            fetchCurrentCityWeather()
            return START_STICKY
        }
        val cityId = intent.getLongExtra("id", -1)
        if (cityId != -1L) {
            val cityName = intent.getStringExtra("name") ?: "-"
            val country = intent.getStringExtra("country") ?: "-"
            mCurrentCity = City(cityId, cityName, country, Coordinate(0.0, 0.0))
            fetchCurrentCityWeather()
        } else {
            Log.w(TAG, "No city id provided!")
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        mLastWeatherFile = File(applicationContext.cacheDir, "last_weather")
        mWeatherManager.initialize(applicationContext)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            Constants.Notification.WEATHER_CHANNEL_ID,
            getString(Constants.Notification.WEATHER_CHANNEL_NAME), NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.vibrationPattern = null
        mNotificationManager.createNotificationChannel(notificationChannel)

        readLastWeather()

        startForeground(
            Constants.Notification.WEATHER_NOTIFICATION_ID,
            createNotification(applicationContext, mLastWeather)
        )

        // TODO: Add alarm manager to fetch timely and also check for timestamp!
    }

    override fun onDestroy() {
        stopForeground(true)
        mNotificationManager.cancelAll()
        mWeatherManager.close()
        super.onDestroy()
    }

    private fun fetchCurrentCityWeather() {
        val city = mCurrentCity
        if (city == null) {
            Log.w(TAG, "No current city set! Can't fetch data!")
        } else {
            if(isTooSoon()) {
                Log.w(TAG, "Data request too soon! Skipping :)")
                return
            }
            mWeatherManager.fetchCurrentWeather(city) {
                Log.d(TAG, "Weather: $it")
                mLastWeatherFetch = SystemClock.elapsedRealtime()
                storeLastWeather(it)
                updateNotication(applicationContext, it)
            }
        }
    }

    private fun isTooSoon() : Boolean {
        if(mLastWeatherFetch == -1L) {
            Log.w(TAG, "No last fetch time!")
            return false
        }
        val weather = mLastWeather ?: return false
        Log.d(TAG, "Got last weather, check fetch time!")
        val now = SystemClock.elapsedRealtime()
        val diff = now - mLastWeatherFetch
        return diff < 10 * 60_000 // 10 mins
    }

    private fun readLastWeather() {
        if (!mLastWeatherFile.exists()) {
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

    private fun updateNotication(context: Context, weather: Weather) {
        mNotificationManager.notify(
            Constants.Notification.WEATHER_NOTIFICATION_ID,
            createNotification(context, weather)
        )
    }

    private fun createNotification(context: Context, weather: Weather?): Notification {
        val notification = NotificationCompat.Builder(context, Constants.Notification.WEATHER_CHANNEL_ID)

        val city = weather?.getRealName() ?: "-"
        val country = mCurrentCity?.country ?: "-"
        val temperature = weather.getTemperatureAsCelsius().roundToInt()
        val conditions = weather.getConditions()

        val content = RemoteViews(packageName, R.layout.view_weather)
        content.setTextViewText(R.id.weatherTitle, getString(R.string.weather_notification_content, city, country))
        content.setTextViewText(
            R.id.weatherTemperature,
            getString(R.string.weather_notification_title, temperature, conditions)
        )
        // TODO: Add conditions icon!
        content.setImageViewResource(R.id.weatherIcon, R.drawable.ic_conditions_cloudy)

        val contentIntent = Intent(applicationContext, DragonairService::class.java)
        contentIntent.action = Constants.Intents.ACTION_UPDATE_WEATHER

        val pendingContentIntent = PendingIntent.getForegroundService(applicationContext, 44,
            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // TODO: Replace icon with temp icon!
        notification.setSmallIcon(R.drawable.ic_weather_placeholder)
            .setCustomContentView(content)
            .setOngoing(true)
            .setContentIntent(pendingContentIntent)

        if(weather == null) {
            notification.priority = NotificationManager.IMPORTANCE_LOW
        }

        return notification.build()
    }

    class NoOpBinder : Binder()

    companion object {
        private const val TAG = "DragonairService"
    }
}
