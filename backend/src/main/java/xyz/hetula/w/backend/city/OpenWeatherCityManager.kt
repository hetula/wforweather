package xyz.hetula.w.backend.city

import android.content.Context
import android.util.Log
import android.util.LongSparseArray
import androidx.core.util.isNotEmpty
import androidx.core.util.set
import androidx.core.util.valueIterator
import com.google.gson.Gson
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import xyz.hetula.w.api.city.City
import xyz.hetula.w.api.city.CityBackend
import xyz.hetula.w.api.util.NameCorrection
import xyz.hetula.w.backend.R
import xyz.hetula.w.backend.util.GsonHelper
import java.io.BufferedReader
import java.io.InputStreamReader

class OpenWeatherCityManager : CityBackend, KoinComponent {

    private val context: Context by inject()

    private val uiScheduler: Scheduler by inject(named("uiScheduler"))
    private val ioScheduler: Scheduler by inject(named("ioScheduler"))

    private val mCities = LongSparseArray<City>()

    private val loadingState = BehaviorRelay.createDefault<Boolean>(true)
    private val cities = BehaviorRelay.create<List<City>>()

    override fun loading(): Observable<Boolean> = loadingState.distinctUntilChanged().observeOn(uiScheduler)

    override fun cities(): Observable<List<City>> = cities.observeOn(uiScheduler)

    override fun loadCities(): Completable {
        if (mCities.isNotEmpty()) {
            return Completable.complete()
        }
        return Completable.fromAction {
            loadCitiesSync(context)
            cities.accept(citiesAsList())
            loadingState.accept(false)
        }.subscribeOn(ioScheduler).observeOn(uiScheduler)
    }

    override fun getCity(id: Long): City? = mCities[id]

    private fun loadCitiesSync(context: Context) {
        mCities.clear()
        BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.city_list))).use {
            GsonHelper.readCities(Gson(), it).forEach { city ->
                if(city.country == "FI") {
                    mCities[city.id] = city
                }
            }
        }
    }

    private fun citiesAsList(): List<City> {
        val list = ArrayList<City>()
        mCities.valueIterator().forEach { list.add(it) }
        list.sort()
        return list.distinctBy { NameCorrection.correctName(it.name) }
    }
}
