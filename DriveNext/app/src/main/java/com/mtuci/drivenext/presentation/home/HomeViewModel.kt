package com.mtuci.drivenext.presentation.home

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.usecase.GetCarsUseCase
import com.mtuci.drivenext.presentation.cars.CarListEvent
import com.mtuci.drivenext.presentation.cars.CarListViewModel
import com.mtuci.drivenext.presentation.common.appContainer

class HomeViewModel(
    private val getCars: GetCarsUseCase
) : CarListViewModel(errorText = R.string.error_load_data) {

    override val emptyText: Int
        get() = R.string.home_empty

    // Список загружается с сервера при открытии экрана
    init {
        load()
    }

    override suspend fun fetchCars(): Result<List<Car>> = getCars()

    /** Поиск по кнопке «Найти» на клавиатуре: переходим на «Результаты поиска», там идёт загрузка. */
    fun onSearchSubmit(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) send(CarListEvent.OpenSearchResults(trimmed))
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(appContainer().getCarsUseCase) }
        }
    }
}
