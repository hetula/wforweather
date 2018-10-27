package xyz.hetula.dragonair

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import xyz.hetula.dragonair.util.GsonHelper
import xyz.hetula.dragonair.weather.Weather
import xyz.hetula.dragonair.weather.WeatherManager
import java.io.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class DragonairService : Service() {
    private val mWeatherManager: WeatherManager = WeatherManager()

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mAlarmManager: AlarmManager
    private lateinit var mUpdateReceiver: WeatherUpdateReceiver
    private lateinit var mLastWeatherFile: File

    private var mUpdateIntervalMillis: Long = TimeUnit.MINUTES.toMillis(60L)
    private var mUpdateWindowMillis: Long = TimeUnit.MINUTES.toMillis(10L)

    private var mLastWeather: Weather? = null
    private var mLastWeatherFetch: Long = -1L
    private var mCurrentCityId: Long = -1L

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Add real binder to provide access to update schedules etc.
        return NoOpBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setCityIdIfNotPresent(intent?.getLongExtra("id", -1) ?: -1)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        mLastWeatherFile = File(applicationContext.cacheDir, "last_weather")
        mWeatherManager.initialize(applicationContext)
        mUpdateReceiver = WeatherUpdateReceiver(this)
        mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            Constants.Notification.WEATHER_CHANNEL_ID,
            getString(Constants.Notification.WEATHER_CHANNEL_NAME), NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.vibrationPattern = null
        mNotificationManager.createNotificationChannel(notificationChannel)

        readCurrentCityId()
        readLastWeather()

        startForeground(
            Constants.Notification.WEATHER_NOTIFICATION_ID,
            createNotification(applicationContext, mLastWeather)
        )
        mUpdateReceiver.register(applicationContext)
        allocateNewScheduledUpdate()
    }

    override fun onDestroy() {
        mUpdateReceiver.unregister(applicationContext)
        stopForeground(true)
        mNotificationManager.cancelAll()
        mWeatherManager.close()
        super.onDestroy()
    }

    private fun setCityIdIfNotPresent(newCityId: Long) {
        Log.d(TAG, "Trying to set City Id: $newCityId")
        if(mCurrentCityId == -1L) {
            mCurrentCityId = newCityId
            storeCurrentCityId(mCurrentCityId)
            if (mCurrentCityId != -1L && mLastWeather == null) {
                Log.d(TAG, "City Id set to $mCurrentCityId, fetching weather!")
                fetchCurrentCityWeather()
            } else {
                Log.w(TAG, "Not fetching weather! City ID [id: $mCurrentCityId] or Weather [w: $mLastWeather]!")
            }
        } else {
            Log.d(TAG,"City Id already set to $mCurrentCityId, not setting new")
        }
    }

    private fun fetchCurrentCityWeatherAndAllocateNewSchelude() {
        Log.d(TAG, "fetchCurrentCityWeatherAndAllocateNewSchelude called")
        fetchCurrentCityWeather()
        allocateNewScheduledUpdate()
    }

    private fun allocateNewScheduledUpdate() {
        Log.d(TAG, "allocateNewScheduledUpdate called")
        val updateIntent = Intent(Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY).let {
            PendingIntent.getBroadcast(applicationContext, 45, it, 0)
        }
        val alarmTimeStart = SystemClock.elapsedRealtime() + mUpdateIntervalMillis
        mAlarmManager.setWindow(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            alarmTimeStart,
            mUpdateWindowMillis,
            updateIntent
        )
    }

    private fun fetchCurrentCityWeather() {
        Log.d(TAG, "fetchCurrentCityWeather called")
        val city = mCurrentCityId
        if (city == -1L) {
            Log.w(TAG, "No current city id set! Can't fetch data!")
        } else {
            if (isTooSoon()) {
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

    private fun isTooSoon(): Boolean {
        if (mLastWeatherFetch == -1L) {
            Log.w(TAG, "No last fetch time!")
            return false
        }
        mLastWeather ?: return false
        Log.d(TAG, "Got last weather, check fetch time!")
        val now = SystemClock.elapsedRealtime()
        val diff = now - mLastWeatherFetch
        return diff < 10 * 60_000 // 10 mins
    }

    private fun readLastWeather() {
        if (!mLastWeatherFile.exists()) {
            Log.d(TAG, "No last weather data found!")
            return
        }
        BufferedReader(FileReader(mLastWeatherFile)).use {
            mLastWeather = GsonHelper.readWeather(Gson(), it)
            Log.d(TAG, "Last Weather read: $mLastWeather")
        }
    }

    private fun storeLastWeather(weather: Weather) {
        mLastWeather = weather
        BufferedWriter(FileWriter(mLastWeatherFile)).use {
            it.write(Gson().toJson(weather))
            Log.d(TAG, "Stored Last weather")
        }
    }

    private fun readCurrentCityId() {
        mCurrentCityId = getSharedPreferences(Constants.Pref.PREF_NAME, Context.MODE_PRIVATE)
            .getLong(Constants.Pref.KEY_CURRENT_CITY_ID, -1L)
        if(mCurrentCityId == -1L) {
            Log.w(TAG, "No city id set!")
        } else {
            Log.d(TAG, "Read City Id: $mCurrentCityId")
        }
    }

    private fun storeCurrentCityId(cityId: Long) {
        Log.d(TAG, "Storing City ID: $cityId")
        getSharedPreferences(Constants.Pref.PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(Constants.Pref.KEY_CURRENT_CITY_ID, cityId)
            .apply()
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
        val country = weather?.sys?.country ?: "-"
        val temperature = weather.getTemperatureAsCelsius().roundToInt()
        val conditions = weather.getConditions()

        // TODO: Add conditions icon!
        val bitmap = AppCompatResources.getDrawable(applicationContext, R.drawable.ic_conditions_cloudy)!!.toBitmap()

        val pendingContentIntent = PendingIntent.getBroadcast(
            applicationContext, 44,
            Intent(Constants.Intents.ACTION_UPDATE_WEATHER), PendingIntent.FLAG_UPDATE_CURRENT
        )

        // TODO: Replace icon with temp icon!
        notification.setSmallIcon(R.drawable.ic_weather_placeholder)
            .setLargeIcon(bitmap)
            .setShowWhen(true)
            .setContentTitle(getString(R.string.weather_notification_title, temperature, conditions))
            .setContentText(getString(R.string.weather_notification_content, city, country))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(System.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(pendingContentIntent)

        val date = weather?.dt ?: -1L
        if(date != -1L) {
            val formattedTime = DateUtils.formatDateTime(applicationContext, date, DateUtils.FORMAT_SHOW_TIME)
            notification.setSubText(getString(R.string.weather_notification_time, formattedTime))
        }

        if (weather == null) {
            notification.priority = NotificationManager.IMPORTANCE_LOW
        }

        return notification.build()
    }

    class NoOpBinder : Binder()

    private class WeatherUpdateReceiver(private val service: DragonairService) : BroadcastReceiver() {
        private val mIntentFilter = IntentFilter()
        private var mRegistered: Boolean = false

        init {
            mIntentFilter.addAction(Constants.Intents.ACTION_UPDATE_WEATHER)
            mIntentFilter.addAction(Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY)
        }

        fun register(context: Context) {
            if (!mRegistered) {
                Log.d(TAG, "WeatherUpdateReceiver registered")
                context.registerReceiver(this, mIntentFilter)
                mRegistered = true
            }
        }

        fun unregister(context: Context) {
            if (mRegistered) {
                Log.d(TAG, "WeatherUpdateReceiver unregistered")
                context.unregisterReceiver(this)
                mRegistered = false
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                Constants.Intents.ACTION_UPDATE_WEATHER -> {
                    Log.d(TAG, "Received update request!")
                    service.fetchCurrentCityWeather()
                }
                Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY -> {
                    Log.d(TAG, "Received timely update request!")
                    service.fetchCurrentCityWeatherAndAllocateNewSchelude()
                }
            }
        }
    }

    companion object {
        private const val TAG = "DragonairService"
    }
}
