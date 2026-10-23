package com.mtuci.drivenext.presentation.bookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.model.Booking
import com.mtuci.drivenext.domain.repository.BookingRepository
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class BookingDetailsUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null
)

sealed interface BookingDetailsEvent {
    /** Бронирование отменено — возвращаемся к списку. */
    data object Close : BookingDetailsEvent
}

class BookingDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val bookingId: Long = checkNotNull(savedStateHandle[KEY_BOOKING_ID])

    private val _state = MutableStateFlow(BookingDetailsUiState())
    val state: StateFlow<BookingDetailsUiState> = _state.asStateFlow()

    private val _events = Channel<BookingDetailsEvent>(Channel.BUFFERED)
    val events: Flow<BookingDetailsEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _state.value = BookingDetailsUiState(isLoading = false, booking = bookingRepository.getBooking(bookingId))
        }
    }

    fun onCancelClick() {
        viewModelScope.launch {
            bookingRepository.cancel(bookingId)
            _events.send(BookingDetailsEvent.Close)
        }
    }

    companion object {
        const val KEY_BOOKING_ID = "booking_id"

        val Factory = viewModelFactory {
            initializer { BookingDetailsViewModel(createSavedStateHandle(), appContainer().bookingRepository) }
        }
    }
}
