package xyz.hetula.w.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_overview.view.*
import xyz.hetula.w.Constants
import xyz.hetula.w.DragonairUpdater
import xyz.hetula.w.R
import xyz.hetula.w.api.util.NameCorrection

class OverviewFragment : DragonairFragment() {
    private lateinit var mAlarmManager: AlarmManager

    private lateinit var mFabSelectCity: FloatingActionButton
    private lateinit var mSelectCityHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_overview, container, false)
        mFabSelectCity = root.fabSelectCity
        mSelectCityHint = root.textSelectCityToStart

        mSelectCityHint.visibility = View.VISIBLE
        mainActivity.startCityLoading {
            mFabSelectCity.show()
            mSelectCityHint.setText(R.string.ui_select_city_to_start)
        }
        mFabSelectCity.setOnClickListener {
            val city = mainActivity.cityManager().getCity(655194) ?: return@setOnClickListener
            scheludeFirst(city.id)
            Snackbar.make(root, NameCorrection.correctName(city.name), Snackbar.LENGTH_SHORT).show()
        }
        return root
    }

    private fun scheludeFirst(cityId: Long) {
        Intent(context!!.applicationContext, DragonairUpdater::class.java).let {
            it.action = Constants.Intents.ACTION_UPDATE_WEATHER_TIMELY
            it.putExtra("id", cityId)
            PendingIntent.getBroadcast(context!!.applicationContext, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }.also {
            mAlarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 100,
                it
            )
        }
    }
}
