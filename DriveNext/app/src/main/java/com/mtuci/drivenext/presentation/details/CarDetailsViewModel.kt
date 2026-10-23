package com.mtuci.drivenext.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.repository.FavoritesRepository
import com.mtuci.drivenext.domain.usecase.GetCarDetailsUseCase
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class CarDetailsUiState(
    val isLoading: Boolean = false,
    val car: Car? = null,
    val isError: Boolean = false,
    val isFavorite: Boolean = false
)

sealed interface CarDetailsEvent {
    data class OpenBooking(val car: Car) : CarDetailsEvent
    data object OpenNoConnection : CarDetailsEvent
    data object FavoriteFailed : CarDetailsEvent
}

class CarDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCarDetails: GetCarDetailsUseCase,
    private val favorites: FavoritesRepository
) : ViewModel() {

    private val carId: String = checkNotNull(savedStateHandle[KEY_CAR_ID])

    private val _state = MutableStateFlow(CarDetailsUiState())
    val state: StateFlow<CarDetailsUiState> = _state.asStateFlow()

    private val _events = Channel<CarDetailsEvent>(Channel.BUFFERED)
    val events: Flow<CarDetailsEvent> = _events.receiveAsFlow()

    private var retryWhenVisible = false

    init {
        // Цвет «сердца» меняется сам, когда запись в таблице favorites появилась или исчезла
        viewModelScope.launch {
            favorites.isFavorite(carId).collect { favorite -> _state.update { it.copy(isFavorite = favorite) } }
        }
        load()
    }

    fun onScreenStarted() {
        if (retryWhenVisible) load()
    }

    fun onRetryClick() = load()

    fun onBookClick() {
        val car = _state.value.car ?: return
        viewModelScope.launch { _events.send(CarDetailsEvent.OpenBooking(car)) }
    }

    fun onFavoriteClick() {
        val current = _state.value
        val car = current.car ?: return
        viewModelScope.launch {
            try {
                if (current.isFavorite) favorites.remove(car.id) else favorites.add(car)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(CarDetailsEvent.FavoriteFailed)
            }
        }
    }

    private fun load() {
        retryWhenVisible = false
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isError = false) }
            getCarDetails(carId).fold(
                onSuccess = { car -> _state.update { it.copy(isLoading = false, car = car) } },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, isError = true) }
                    if (error is AppError.NoConnection) {
                        retryWhenVisible = true
                        _events.send(CarDetailsEvent.OpenNoConnection)
                    }
                }
            )
        }
    }

    companion object {
        const val KEY_CAR_ID = "car_id"

        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                CarDetailsViewModel(createSavedStateHandle(), container.getCarDetailsUseCase, container.favoritesRepository)
            }
        }
    }
}
