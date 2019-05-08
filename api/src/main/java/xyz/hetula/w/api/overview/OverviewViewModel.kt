package xyz.hetula.w.api.overview

import xyz.hetula.w.api.city.City
import xyz.hetula.w.api.mvi.MviViewModel

data class OverviewViewModel(val loading: Boolean = true, val cities: List<City> = emptyList()) : MviViewModel