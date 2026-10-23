package com.mtuci.drivenext.presentation.cars

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.presentation.common.UiText
import com.mtuci.drivenext.presentation.common.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class CarListUiState(
    val isLoading: Boolean = false,
    val cars: List<Car> = emptyList(),
    @StringRes val errorText: Int? = null,
    @StringRes val emptyText: Int? = null
)

sealed interface CarListEvent {
    data class OpenSearchResults(val query: String) : CarListEvent
    data class OpenBooking(val car: Car) : CarListEvent
    data class OpenDetails(val car: Car) : CarListEvent
    data object OpenNoConnection : CarListEvent
    data class ShowError(val message: UiText) : CarListEvent
}

/**
 * Общая логика «Главной» и «Результатов поиска»: загрузка списка с лоадером,
 * ошибка с кнопкой «Повторить», переходы по кнопкам карточки.
 * @param errorText текст ошибки из ТЗ: «Не удалось загрузить данные…» или «Не удалось выполнить поиск…»
 */
abstract class CarListViewModel(@StringRes private val errorText: Int) : ViewModel() {

    private val _state = MutableStateFlow(CarListUiState())
    val state: StateFlow<CarListUiState> = _state.asStateFlow()

    private val _events = Channel<CarListEvent>(Channel.BUFFERED)
    val events: Flow<CarListEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    // Не было сети — повторим запрос, когда пользователь вернётся с экрана «Нет подключения»
    private var retryWhenVisible = false

    /** Текст, если список пустой. */
    @get:StringRes
    protected abstract val emptyText: Int

    protected abstract suspend fun fetchCars(): Result<List<Car>>

    fun onRetryClick() = load()

    fun onScreenStarted() {
        if (retryWhenVisible) load()
    }

    fun onBookClick(car: Car) = send(CarListEvent.OpenBooking(car))

    fun onDetailsClick(car: Car) = send(CarListEvent.OpenDetails(car))

    protected fun load() {
        retryWhenVisible = false
        // Новый запрос отменяет старый: например, результаты по прошлому тексту поиска уже не нужны
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = CarListUiState(isLoading = true)
            fetchCars().fold(
                onSuccess = { cars ->
                    _state.value = CarListUiState(cars = cars, emptyText = if (cars.isEmpty()) emptyText else null)
                },
                onFailure = { error ->
                    _state.value = CarListUiState(errorText = errorText)
                    if (error is AppError.NoConnection) {
                        retryWhenVisible = true
                        _events.send(CarListEvent.OpenNoConnection)
                    } else {
                        _events.send(CarListEvent.ShowError(error.toUiText()))
                    }
                }
            )
        }
    }

    protected fun send(event: CarListEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
