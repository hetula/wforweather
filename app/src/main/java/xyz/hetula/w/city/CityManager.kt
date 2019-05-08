package xyz.hetula.w.city

import android.content.Context
import android.util.LongSparseArray
import androidx.core.util.set
import com.google.gson.Gson
import xyz.hetula.w.R
import xyz.hetula.w.util.GsonHelper
import java.io.BufferedReader
import java.io.InputStreamReader

class CityManager {
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
