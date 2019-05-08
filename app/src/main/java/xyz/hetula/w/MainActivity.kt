package xyz.hetula.w

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xyz.hetula.w.overview.OverviewFragment
import xyz.hetula.w.weather.Weather

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weather.initialize(applicationContext)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, OverviewFragment())
            .commit()
    }
}
