package xyz.hetula.w

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import xyz.hetula.w.backend.city.OpenWeatherCityManager
import xyz.hetula.w.overview.OverviewFragment
import xyz.hetula.w.weather.Weather
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private val mCityManager = OpenWeatherCityManager()
    private var mLoadingDone: Boolean = false

    private var mCityLoadTask: CityLoadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weather.initialize(applicationContext)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, OverviewFragment())
            .commit()
    }

    override fun onDestroy() {
        mCityLoadTask?.cancel(true)
        mCityLoadTask = null
        super.onDestroy()
    }

    fun startCityLoading(callback: () -> Unit) {
        mCityLoadTask = CityLoadTask(this, callback)
        mCityLoadTask?.execute()
    }

    fun cityManager() = mCityManager

    internal fun cityLoadReady() {
        mLoadingDone = true

    }

    private class CityLoadTask(main: MainActivity, private val callback: () -> Unit) : AsyncTask<Void, Void, Void?>() {
        private val mMainRef = WeakReference(main)

        override fun doInBackground(vararg params: Void?): Void? {
            withMain {main ->
                main.mCityManager.loadCitiesSync(main.applicationContext)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            withMain {main ->
                main.cityLoadReady()
                callback()
            }
        }

        private inline fun withMain(with: (MainActivity) -> Unit) {
            val main = mMainRef.get()
            if(main != null) {
                with(main)
            } else {
                Log.w(TAG, "MainActivity refenrence already cleaned!")
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
