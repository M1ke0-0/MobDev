package com.mtuci.drivenext.presentation.cars

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ViewListStateBinding
import com.mtuci.drivenext.presentation.common.openNoConnection
import com.mtuci.drivenext.presentation.common.showErrorDialog
import com.mtuci.drivenext.presentation.main.MainNavigator

/** Телефон — одна карточка в ряд, планшет — две-три (res/values-w600dp, values-w900dp). */
fun RecyclerView.setupCarList(carAdapter: CarAdapter) {
    layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.car_grid_columns))
    adapter = carAdapter
}

/** Показывает список, лоадер, ошибку с кнопкой «Повторить» или текст «ничего нет». */
fun ViewListStateBinding.render(state: CarListUiState, list: RecyclerView) {
    progress.isVisible = state.isLoading
    errorContainer.isVisible = state.errorText != null
    state.errorText?.let(tvError::setText)
    tvEmpty.isVisible = state.emptyText != null
    state.emptyText?.let(tvEmpty::setText)
    list.isVisible = !state.isLoading && state.errorText == null
}

fun Fragment.handleCarListEvent(event: CarListEvent) {
    val navigator = requireActivity() as MainNavigator
    when (event) {
        is CarListEvent.OpenSearchResults -> navigator.openSearchResults(event.query)
        is CarListEvent.OpenBooking -> navigator.openBooking(event.car)
        is CarListEvent.OpenDetails -> navigator.openCarDetails(event.car.id)
        CarListEvent.OpenNoConnection -> requireActivity().openNoConnection()
        is CarListEvent.ShowError -> requireContext().showErrorDialog(event.message)
    }
}
