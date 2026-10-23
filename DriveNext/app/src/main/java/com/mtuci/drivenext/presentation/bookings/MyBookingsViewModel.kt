package com.mtuci.drivenext.presentation.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.model.Booking
import com.mtuci.drivenext.domain.repository.BookingRepository
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MyBookingsViewModel(bookingRepository: BookingRepository) : ViewModel() {

    /** null — список ещё читается из базы. После отмены бронирования Room пришлёт новый список сам. */
    val bookings: StateFlow<List<Booking>?> = bookingRepository.getBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { MyBookingsViewModel(appContainer().bookingRepository) }
        }
    }
}
