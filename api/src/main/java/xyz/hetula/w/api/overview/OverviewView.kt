package xyz.hetula.w.api.overview

import io.reactivex.Observable
import xyz.hetula.w.api.mvi.MviView

interface OverviewView : MviView<OverviewViewModel> {
    fun loadCitiesIntent(): Observable<Unit>
}