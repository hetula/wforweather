package xyz.hetula.w

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import xyz.hetula.w.city.CityManager

class CitySelecterActivity : AppCompatActivity() {
    private val mCityManager: CityManager = CityManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        W.initialize(applicationContext)
        setContentView(R.layout.activity_city_selecter)
        Thread {
            val start = SystemClock.elapsedRealtime()
            mCityManager.loadCitiesSync(applicationContext)
            val end = SystemClock.elapsedRealtime()
            Log.d("TestingTimes", "Loaded cities in ${end - start} ms")
            val testCity = mCityManager.getCity(655194) ?: return@Thread
            scheludeFirst(testCity.id)
        }.start()
    }

    private fun scheludeFirst(cityId: Long) {
        Intent(applicationContext, WUpdater::class.java).let {
            it.action = Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY
            it.putExtra("id", cityId)
            PendingIntent.getBroadcast(applicationContext, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }.also {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 100,
                it
            )
        }
    }

}
