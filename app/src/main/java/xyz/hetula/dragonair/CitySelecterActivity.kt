package xyz.hetula.dragonair

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
        }.start()
    }
}
