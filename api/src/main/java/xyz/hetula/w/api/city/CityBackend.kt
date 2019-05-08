package xyz.hetula.w.api.city

import io.reactivex.Completable
import io.reactivex.Observable

interface CityBackend {
    fun loading(): Observable<Boolean>

    fun cities(): Observable<List<City>>

    fun loadCities(): Completable

    fun getCity(id: Long): City?
}