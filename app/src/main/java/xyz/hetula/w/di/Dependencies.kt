package xyz.hetula.w.di

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import xyz.hetula.w.api.city.CityBackend
import xyz.hetula.w.api.overview.OverviewPresenter
import xyz.hetula.w.api.weather.WeatherBackend
import xyz.hetula.w.backend.city.OpenWeatherCityManager
import xyz.hetula.w.backend.weather.OpenWeatherBackend

val schedulerModule = module {
    single(named("uiScheduler")) { AndroidSchedulers.mainThread() }

    single(named("ioScheduler")) { Schedulers.io() }
}

val uiModule = module {
    factory { OverviewPresenter(get()) }
}

val backendModule = module {
    single<CityBackend> { OpenWeatherCityManager() }
    single<WeatherBackend> { OpenWeatherBackend() }
}