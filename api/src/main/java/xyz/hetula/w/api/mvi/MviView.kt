package xyz.hetula.w.api.mvi

interface MviView<T : MviViewModel> {
    fun render(viewModel: T)
}