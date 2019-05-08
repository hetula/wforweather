package xyz.hetula.w.api.mvi

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

abstract class MviPresenter<T : MviViewModel, S : MviView<T>> {
    private var viewRef: WeakReference<S>? = null

    private val disposeBag = CompositeDisposable()

    protected abstract fun initialState(): T

    protected abstract fun changes(view: S): List<Observable<Change>>

    protected abstract fun reduce(previousModel: T, change: Change): T

    fun attach(view: S) {
        viewRef = WeakReference(view)

        val changeIntents = Observable.merge(changes(view))

        val initialState = initialState()

        dispose {
            changeIntents.scan(initialState, ::reduce)
                .distinctUntilChanged()
                .subscribe(::renderWithView)
        }
    }

    fun detach() {
        disposeBag.clear()
        viewRef = null
    }

    private fun renderWithView(viewModel: T) {
        viewRef?.get()?.render(viewModel)
    }

    private fun dispose(disposable: () -> Disposable) {
        disposeBag.add(disposable())
    }
}