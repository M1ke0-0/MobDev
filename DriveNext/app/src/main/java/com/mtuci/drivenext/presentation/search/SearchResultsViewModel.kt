package com.mtuci.drivenext.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.usecase.SearchCarsUseCase
import com.mtuci.drivenext.presentation.cars.CarListViewModel
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
class SearchResultsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val searchCars: SearchCarsUseCase
) : CarListViewModel(errorText = R.string.error_search) {

    // Запрос хранится в SavedStateHandle (туда же попадают аргументы фрагмента):
    // переживает поворот и перезапуск приложения — после перезапуска результаты загрузятся заново
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val currentQuery: String
        get() = query.value

    override val emptyText: Int
        get() = if (currentQuery.isBlank()) R.string.search_enter_query else R.string.search_empty

    init {
        load()
        // Запрос изменили — ищем заново, но не на каждую букву, а через паузу 400 мс после ввода
        query.drop(1)
            .debounce(QUERY_DEBOUNCE_MS)
            .onEach { load() }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(text: String) {
        savedStateHandle[KEY_QUERY] = text
    }

    override suspend fun fetchCars(): Result<List<Car>> =
        if (currentQuery.isBlank()) Result.success(emptyList()) else searchCars(currentQuery)

    companion object {
        const val KEY_QUERY = "query"
        private const val QUERY_DEBOUNCE_MS = 400L

        val Factory = viewModelFactory {
            initializer {
                SearchResultsViewModel(createSavedStateHandle(), appContainer().searchCarsUseCase)
            }
        }
    }
}
