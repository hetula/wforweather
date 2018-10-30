package xyz.hetula.dragonair

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import xyz.hetula.dragonair.util.GsonHelper
import xyz.hetula.dragonair.util.WeatherIconMapper
import xyz.hetula.dragonair.weather.Weather
import xyz.hetula.dragonair.weather.WeatherManager
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object Dragonair {
    private const val TAG = "DragonairInstance"
    private val mWeatherManager: WeatherManager = WeatherManager()
    private var mInitialized: Boolean = false
    private var mDestroyed: Boolean = false

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mAlarmManager: AlarmManager
    private lateinit var mLastWeatherFile: File

    private var mMinApiQueryTime: Long = TimeUnit.MINUTES.toMillis(10L)
    private var mUpdateIntervalMillis: Long = TimeUnit.MINUTES.toMillis(60L)
    private var mUpdateWindowMillis: Long = TimeUnit.MINUTES.toMillis(15L)

    private var mLastWeather: Weather? = null
    private var mLastWeatherFetch: Long = -1L
    private var mCurrentCityId: Long = -1L

    fun initialize(providedContext: Context) {
        if (mDestroyed) {
            throw IllegalStateException("Dragonair instance has died, so should the caller...")
        }
        if (mInitialized) {
            Log.w(TAG, "DragonairInstance up and running")
            return
        }
        Log.d(TAG, "DragonairInstance initialized!")
        val context = providedContext.applicationContext
        mInitialized = true
        mWeatherManager.initialize(context)

        mLastWeatherFile = File(context.applicationContext.cacheDir, "last_weather")
        mWeatherManager.initialize(context.applicationContext)
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            Constants.Notification.WEATHER_CHANNEL_ID,
            context.getString(Constants.Notification.WEATHER_CHANNEL_NAME), NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.vibrationPattern = null
        mNotificationManager.createNotificationChannel(notificationChannel)

        readCurrentCityId(context)
        readLastWeather()
    }

    fun release() {
        if (!mInitialized) {
            Log.d(TAG, "DragonairInstance release without init!")
            return
        }
        Log.d(TAG, "DragonairInstance released!")
        mNotificationManager.cancelAll()
        mWeatherManager.close()
        mDestroyed = true
    }

    fun setCityIdIfNotPresent(providedContext: Context, newCityId: Long) {
        val context = providedContext.applicationContext
        Log.d(TAG, "Trying to set City Id: $newCityId")
        if (mCurrentCityId == -1L) {
            mCurrentCityId = newCityId
            storeCurrentCityId(context, mCurrentCityId)
            if (mCurrentCityId != -1L && mLastWeather == null) {
                Log.d(TAG, "City Id set to $mCurrentCityId, fetching weather!")
                fetchCurrentCityWeather(context)
            } else {
                Log.w(TAG, "Not fetching weather! City ID [id: $mCurrentCityId] or Weather [w: $mLastWeather]!")
            }
        } else {
            Log.d(TAG, "City Id already set to $mCurrentCityId, not setting new")
        }
    }

    fun fetchCurrentCityWeatherAndAllocateNewSchelude(providedContext: Context) {
        val context = providedContext.applicationContext
        Log.d(TAG, "fetchCurrentCityWeatherAndAllocateNewSchelude called")
        fetchCurrentCityWeather(context)
        allocateNewScheduledUpdate(context)
    }

    fun fetchCurrentCityWeather(providedContext: Context) {
        val context = providedContext.applicationContext
        Log.d(TAG, "fetchCurrentCityWeather called")
        val city = mCurrentCityId
        if (city == -1L) {
            Log.w(TAG, "No current city id set! Can't fetch data!")
        } else {
            if (isTooSoon()) {
                Log.w(TAG, "Data request too soon! Refreshing with old data :)")
                updateNotication(context, mLastWeather)
                return
            }
            mWeatherManager.fetchCurrentWeather(city) {
                Log.d(TAG, "Weather: $it")
                mLastWeatherFetch = SystemClock.elapsedRealtime()
                storeLastWeather(it)
                updateNotication(context, it)
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
        return diff < mMinApiQueryTime
    }

    private fun allocateNewScheduledUpdate(context: Context) {
        Log.d(TAG, "allocateNewScheduledUpdate called")
        val updateIntent = Intent(context, DragonairUpdater::class.java).let {
            it.action = Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY
            PendingIntent.getBroadcast(context, 45, it, 0)
        }
        val alarmTimeStart = SystemClock.elapsedRealtime() + mUpdateIntervalMillis
        mAlarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            alarmTimeStart,
            updateIntent
        )
    }

    private fun storeLastWeather(weather: Weather) {
        mLastWeather = weather
        BufferedWriter(FileWriter(mLastWeatherFile)).use {
            it.write(Gson().toJson(weather))
            Log.d(TAG, "Stored Last weather")
        }
    }

    private fun storeCurrentCityId(context: Context, cityId: Long) {
        Log.d(TAG, "Storing City ID: $cityId")
        context.getSharedPreferences(Constants.Pref.PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(Constants.Pref.KEY_CURRENT_CITY_ID, cityId)
            .apply()
    }

    private fun readCurrentCityId(context: Context) {
        mCurrentCityId = context.getSharedPreferences(Constants.Pref.PREF_NAME, Context.MODE_PRIVATE)
            .getLong(Constants.Pref.KEY_CURRENT_CITY_ID, -1L)
        if (mCurrentCityId == -1L) {
            Log.w(TAG, "No city id set!")
        } else {
            Log.d(TAG, "Read City Id: $mCurrentCityId")
        }
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

    private fun updateNotication(context: Context, weather: Weather?) {
        mNotificationManager.notify(
            Constants.Notification.WEATHER_NOTIFICATION_ID,
            createNotification(context, weather)
        )
    }

    private fun createNotification(context: Context, weather: Weather?): Notification {
        val now = Calendar.getInstance()
        val notification = NotificationCompat.Builder(context, Constants.Notification.WEATHER_CHANNEL_ID)

        val city = weather?.getRealName() ?: "-"
        val temperature = weather.getTemperatureAsCelsius().roundToInt()
        val conditions = weather.getConditions()
        val weatherData = weather.getFirstWeatherData()
        val weatherDesc = (weatherData?.description ?: "").capitalize()
        val timeOfMeasurement =
            DateUtils.formatDateTime(context, getMeasurementTimeInMs(weather), DateUtils.FORMAT_SHOW_TIME)

        val night = isNight(now.get(Calendar.HOUR_OF_DAY))
        val weatherConditionsIconRes = WeatherIconMapper.mapWeatherToIconRes(weatherData?.icon, night)
        val bitmap = AppCompatResources.getDrawable(context, weatherConditionsIconRes)!!.toBitmap()

        val pendingContentIntent = Intent(context, DragonairUpdater::class.java).let {
            it.action = Constants.Intents.ACTION_UPDATE_WEATHER
            PendingIntent.getBroadcast(context, 44, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val smallIcon = WeatherIconMapper.mapTemperatureToIconRes(temperature)
        notification.setSmallIcon(smallIcon)
            .setLargeIcon(bitmap)
            .setContentTitle(context.getString(R.string.weather_notification_title, temperature, conditions))
            .setContentText(weatherDesc)
            .setSubText(context.getString(R.string.weather_notification_content, city, timeOfMeasurement))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(System.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(pendingContentIntent)

        if (weather == null) {
            notification.priority = NotificationManager.IMPORTANCE_LOW
        }


        if (isInDoNotDisturbTime(now)) { // Silent night!
            Log.d(TAG, "Night time! Lets be silent :)")
            notification.setSound(null)
            notification.setOnlyAlertOnce(true)
        }

        return notification.build()
    }

    private fun getMeasurementTimeInMs(weather: Weather?): Long {
        val dt = weather?.dt ?: return System.currentTimeMillis()
        return dt * 1000
    }

    private fun isInDoNotDisturbTime(now: Calendar): Boolean {
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY

        val nightStart: Int
        val nightEnd: Int

        if (isWeekend) {
            nightStart = 23
            nightEnd = 9
        } else {
            nightStart = 22
            nightEnd = 7
        }

        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        Log.d(TAG, "Weekend[$isWeekend] Current hour[$currentHour] Night[$nightStart-$nightEnd]")
        return currentHour > nightStart || currentHour < nightEnd
    }

    private fun isNight(hour: Int): Boolean {
        return hour > 19 || hour < 6
    }

}
