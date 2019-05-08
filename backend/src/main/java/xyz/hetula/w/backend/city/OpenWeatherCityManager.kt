package xyz.hetula.w.backend.city

import android.content.Context
import android.util.LongSparseArray
import androidx.core.util.set
import com.google.gson.Gson
import xyz.hetula.w.api.city.CityBackend
import xyz.hetula.w.backend.R
import xyz.hetula.w.backend.util.GsonHelper
import java.io.BufferedReader
import java.io.InputStreamReader

class OpenWeatherCityManager : CityBackend {
    private val mCities = LongSparseArray<xyz.hetula.w.api.city.City>()

    fun loadCitiesSync(context: Context) {
        mCities.clear()
        BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.city_list))).use {
            GsonHelper.readCities(Gson(), it).forEach { city ->
                mCities[city.id] = city
            }
        }
    }

    fun getCity(id: Long): xyz.hetula.w.api.city.City? = mCities[id]
}
