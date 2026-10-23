package com.mtuci.drivenext.presentation.main

import androidx.annotation.StringRes
import com.mtuci.drivenext.domain.model.Car

/** Переходы между экранами внутри MainActivity; фрагменты вызывают их, не зная, как устроена навигация. */
interface MainNavigator {
    fun openSearchResults(query: String)
    fun openProfile()
    fun openComingSoon(@StringRes titleRes: Int)
    fun openCarDetails(carId: String)
    fun openBooking(car: Car)
    fun openMyBookings()
    fun openBookingDetails(bookingId: Long)

    /** «Удачной поездки» с очисткой стека: «Назад» не вернёт на оформление аренды. */
    fun openBookingSuccess()

    /** Экран входа с очисткой стека — после выхода из профиля «Назад» не вернёт в приложение. */
    fun openLogin()
}
