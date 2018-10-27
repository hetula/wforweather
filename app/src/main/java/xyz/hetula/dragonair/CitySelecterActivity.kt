package xyz.hetula.dragonair

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import xyz.hetula.dragonair.city.CityManager

class CitySelecterActivity : AppCompatActivity() {
    private val mCityManager: CityManager = CityManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_selecter)
        Thread {
            val start = SystemClock.elapsedRealtime()
            mCityManager.loadCitiesSync(applicationContext)
            val end = SystemClock.elapsedRealtime()
            Log.d("TestingTimes", "Loaded cities in ${end - start} ms")
            val testCity = mCityManager.getCity(655194) ?: return@Thread

            Intent(applicationContext, DragonairService::class.java).also {
                it.putExtra("id", testCity.id)
                startForegroundService(it)
            }
        }.start()
    }
}
