package xyz.hetula.w.api.overview

import io.reactivex.Observable
import xyz.hetula.w.api.city.City
import xyz.hetula.w.api.city.CityBackend
import xyz.hetula.w.api.mvi.Change
import xyz.hetula.w.api.mvi.MviPresenter

class OverviewPresenter(private val cityBackend: CityBackend) : MviPresenter<OverviewViewModel, OverviewView>() {

    override fun initialState() = OverviewViewModel()

    override fun changes(view: OverviewView): List<Observable<Change>> {
        return arrayListOf<Observable<Change>>(
            view.loadCitiesIntent().map {
                // TODO: Actually follow completable
                dispose {
                    cityBackend.loadCities()
                        .onErrorComplete()
                        .subscribe()
                }
                LoadingChange(true)
            },
            cityBackend.loading().map(::LoadingChange),
            cityBackend.cities().map(::CitiesChange)
        )
    }

    override fun reduce(previousModel: OverviewViewModel, change: Change): OverviewViewModel {
        return when (change) {
            is LoadingChange -> previousModel.copy(loading = change.loading)
            is CitiesChange -> previousModel.copy(cities = change.cities)
            else -> previousModel
        }
    }

    data class LoadingChange(val loading: Boolean) : Change

    data class CitiesChange(val cities: List<City>) : Change
}