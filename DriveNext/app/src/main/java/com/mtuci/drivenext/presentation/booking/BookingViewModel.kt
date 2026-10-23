package com.mtuci.drivenext.presentation.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.model.Booking
import com.mtuci.drivenext.domain.model.BookingPrice
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.repository.BookingRepository
import com.mtuci.drivenext.domain.usecase.GetProfileUseCase
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class BookingUiState(
    val car: Car,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val price: BookingPrice
)

sealed interface BookingEvent {
    data object OpenSuccess : BookingEvent
}

class BookingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository,
    private val getProfile: GetProfileUseCase
) : ViewModel() {

    private val car: Car = checkNotNull(savedStateHandle[KEY_CAR])

    // Даты храним как номер дня (epochDay) в SavedStateHandle — переживают поворот экрана.
    // По умолчанию аренда с завтрашнего дня на 3 дня.
    private val startDay = savedStateHandle.getStateFlow(KEY_START, LocalDate.now().plusDays(1).toEpochDay())
    private val endDay = savedStateHandle.getStateFlow(KEY_END, LocalDate.now().plusDays(4).toEpochDay())

    // Защита от двойного нажатия «Продолжить» — иначе создадутся два бронирования
    private var isSaving = false

    val state: StateFlow<BookingUiState> = combine(startDay, endDay) { start, end -> buildState(start, end) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, buildState(startDay.value, endDay.value))

    private val _events = Channel<BookingEvent>(Channel.BUFFERED)
    val events: Flow<BookingEvent> = _events.receiveAsFlow()

    val selectedStartDay: Long get() = startDay.value
    val selectedEndDay: Long get() = endDay.value

    fun onDatesSelected(startEpochDay: Long, endEpochDay: Long) {
        savedStateHandle[KEY_START] = startEpochDay
        // Конец аренды хотя бы на следующий день после начала
        savedStateHandle[KEY_END] = maxOf(endEpochDay, startEpochDay + 1)
    }

    fun onContinueClick() {
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            // ФИО и номер ВУ берём из профиля; если он ещё не загружен — пробуем загрузить
            val profile = getProfile.profile.value ?: getProfile.refresh().getOrNull()
            val current = state.value
            bookingRepository.create(
                Booking(
                    id = 0L,
                    car = car,
                    start = current.start,
                    end = current.end,
                    driverName = profile?.fullName.orEmpty(),
                    licenseNumber = profile?.licenseNumber.orEmpty()
                )
            )
            _events.send(BookingEvent.OpenSuccess)
        }
    }

    private fun buildState(startDay: Long, endDay: Long): BookingUiState {
        val start = LocalDate.ofEpochDay(startDay).atTime(RENT_TIME)
        val end = LocalDate.ofEpochDay(endDay).atTime(RENT_TIME)
        return BookingUiState(car, start, end, BookingPrice.calculate(car.pricePerDay, start, end))
    }

    companion object {
        const val KEY_CAR = "car"
        private const val KEY_START = "start_day"
        private const val KEY_END = "end_day"

        /** Машину выдают и принимают в 08:00. */
        private val RENT_TIME: LocalTime = LocalTime.of(8, 0)

        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                BookingViewModel(createSavedStateHandle(), container.bookingRepository, container.getProfileUseCase)
            }
        }
    }
}
