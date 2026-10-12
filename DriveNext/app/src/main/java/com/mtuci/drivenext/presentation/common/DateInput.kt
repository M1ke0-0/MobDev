package com.mtuci.drivenext.presentation.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.mtuci.drivenext.domain.validation.Validators
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Маска DD/MM/YYYY: пользователь вводит только цифры, слэши подставляются сами. */
class DateMaskWatcher(private val editText: EditText) : TextWatcher {

    private var isFormatting = false

    override fun afterTextChanged(s: Editable) {
        if (isFormatting) return
        val digits = s.filter(Char::isDigit).take(MAX_DIGITS)
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append('/')
                append(char)
            }
        }
        if (formatted != s.toString()) {
            isFormatting = true
            editText.setText(formatted)
            editText.setSelection(formatted.length)
            isFormatting = false
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    private companion object {
        const val MAX_DIGITS = 8
    }
}

/**
 * Показывает MaterialDatePicker. Можно выбрать только прошедшие даты.
 * Пикер возвращает полночь выбранного дня в UTC, поэтому и переводим обратно через UTC —
 * иначе в часовых поясах «западнее Гринвича» получился бы предыдущий день.
 */
fun FragmentManager.showDatePicker(
    tag: String,
    title: String,
    currentText: String,
    onPicked: (LocalDate) -> Unit,
    openAt: LocalDate = LocalDate.now()
) {
    val selected = Validators.parseDate(currentText)
    val constraints = CalendarConstraints.Builder()
        .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
        .setOpenAt((selected ?: openAt).toUtcMillis())
        .setValidator(DateValidatorPointBackward.now())
        .build()

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(title)
        .setCalendarConstraints(constraints)
        .apply { selected?.let { setSelection(it.toUtcMillis()) } }
        .build()

    picker.setOnDatePicked(onPicked)
    picker.show(this, tag)
}

/**
 * Слушатель не переживает поворот экрана, поэтому после пересоздания фрагмента
 * находим уже открытый пикер по тегу и вешаем слушатель заново.
 */
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@Suppress("UNCHECKED_CAST")
fun FragmentManager.reattachDatePicker(tag: String, onPicked: (LocalDate) -> Unit) {
    (findFragmentByTag(tag) as? MaterialDatePicker<Long>)?.setOnDatePicked(onPicked)
}

private fun MaterialDatePicker<Long>.setOnDatePicked(onPicked: (LocalDate) -> Unit) {
    clearOnPositiveButtonClickListeners()
    addOnPositiveButtonClickListener { selectedDateUtcMillis ->
        onPicked(Instant.ofEpochMilli(selectedDateUtcMillis).atZone(ZoneOffset.UTC).toLocalDate())
    }
}
