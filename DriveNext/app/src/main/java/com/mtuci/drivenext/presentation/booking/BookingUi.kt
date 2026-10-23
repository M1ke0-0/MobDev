package com.mtuci.drivenext.presentation.booking

import android.content.Context
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ViewBookingCarBinding
import com.mtuci.drivenext.databinding.ViewInfoRowBinding
import com.mtuci.drivenext.domain.model.BookingPrice
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.presentation.common.loadCarImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// «08:00, 27 сентября 2024»
private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm, d MMMM yyyy", Locale.forLanguageTag("ru"))
private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

fun LocalDateTime.formatDateTime(): String = format(dateTimeFormatter)

fun LocalDateTime.formatDate(): String = format(dateFormatter)

/** Карточка машины на «Оформлении аренды» и «Бронировании #…». */
fun ViewBookingCarBinding.bind(car: Car) {
    tvModel.text = car.model
    tvBrand.text = car.brand
    tvPrice.text = root.context.getString(R.string.car_price_per_day, car.pricePerDay)
    ivCar.loadCarImage(car.imageUrl)
}

fun ViewInfoRowBinding.bind(label: String, value: String) {
    tvLabel.text = label
    tvValue.text = value
}

/** Строки «Аренда ×3 дня» и «Страховка ×3 дня» с суммой за весь период. */
fun Context.bindPriceRows(price: BookingPrice, rent: ViewInfoRowBinding, insurance: ViewInfoRowBinding) {
    val days = resources.getQuantityString(R.plurals.days, price.days, price.days)
    rent.bind(getString(R.string.booking_rent, days), getString(R.string.price_rub, price.rent))
    insurance.bind(getString(R.string.booking_insurance, days), getString(R.string.price_rub, price.insurance))
}
