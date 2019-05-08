package xyz.hetula.w.overview

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_overview.view.*
import org.koin.android.ext.android.inject
import xyz.hetula.w.util.Constants
import xyz.hetula.w.weather.WeatherUpdateReceiver
import xyz.hetula.w.R
import xyz.hetula.w.api.city.City
import xyz.hetula.w.api.overview.OverviewPresenter
import xyz.hetula.w.api.overview.OverviewView
import xyz.hetula.w.api.overview.OverviewViewModel
import xyz.hetula.w.util.BaseFragment
import xyz.hetula.w.api.util.NameCorrection
import java.util.concurrent.TimeUnit

class OverviewFragment : BaseFragment(), OverviewView {
    private lateinit var mAlarmManager: AlarmManager

    private lateinit var mFabSelectCity: FloatingActionButton
    private lateinit var mSelectCityHint: TextView

    private val presenter: OverviewPresenter by inject()

    private var mCitiesList: List<City> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_overview, container, false)
        mFabSelectCity = root.fabSelectCity
        mSelectCityHint = root.textSelectCityToStart

        mFabSelectCity.setOnClickListener {
            val citiesByName: Array<CharSequence> =
                mCitiesList.map { it.name }.toTypedArray()
            AlertDialog.Builder(context!!)
                .setTitle(R.string.city_selection_list_title)
                .setItems(citiesByName) { _, i: Int -> clickedOnCity(mCitiesList[i]) }
                .show()
        }

        presenter.attach(this)

        return root
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }

    override fun loadCitiesIntent(): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun render(viewModel: OverviewViewModel) {
        if (viewModel.loading) {
            hideCitySelection()
        } else {
            showCitySelection()
        }
        // TODO: Remove from ViewModel and get from Backend!
        mCitiesList = viewModel.cities
    }

    private fun clickedOnCity(city: City) {
        Log.d("OverView", "Selected: $city")
        scheludeFirst(city.id)
        Snackbar.make(mSelectCityHint, NameCorrection.correctName(city.name), Snackbar.LENGTH_SHORT).show()
    }

    private fun showCitySelection() {
        if (mFabSelectCity.isOrWillBeHidden) {
            mFabSelectCity.show()
            mSelectCityHint.setText(R.string.ui_select_city_to_start)
        }
    }

    private fun hideCitySelection() {
        if (mFabSelectCity.isOrWillBeShown) {
            mFabSelectCity.hide()
            mSelectCityHint.setText(R.string.ui_cities_loading)
        }
    }

    private fun scheludeFirst(cityId: Long) {
        Intent(context!!.applicationContext, WeatherUpdateReceiver::class.java).let {
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
