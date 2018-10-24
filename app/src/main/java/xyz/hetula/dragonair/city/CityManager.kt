package xyz.hetula.dragonair.city

import android.content.Context
import com.google.gson.Gson
import xyz.hetula.dragonair.util.GsonHelper
import xyz.hetula.dragonair.R
import java.io.BufferedReader
import java.io.InputStreamReader

class CityManager {
    private val mCities = HashMap<Long, City>()

    fun loadCitiesSync(context: Context) {
        mCities.clear()
        BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.city_list))).use {
            GsonHelper.readCities(Gson(), it).forEach { city ->
                mCities[city.id] = city
            }
        }
    }

    fun getCity(id: Long) = mCities[id]
}
