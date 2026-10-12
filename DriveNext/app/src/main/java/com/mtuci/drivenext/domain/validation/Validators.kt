package com.mtuci.drivenext.domain.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object Validators {

    /** name@domainname.com или name@domainname.ru — как требует ТЗ. */
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.(com|ru)$")

    // uuuu + STRICT: 31/02/2000 или 00/13/1999 не пройдут.
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    const val DATE_PATTERN_LENGTH = 10
    const val LICENSE_NUMBER_LENGTH = 10
    const val MIN_DRIVER_AGE = 18
    private const val MAX_AGE = 120

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun parseDate(text: String): LocalDate? {
        if (text.length != DATE_PATTERN_LENGTH) return null
        return try {
            LocalDate.parse(text, DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun formatDate(date: LocalDate): String = DATE_FORMATTER.format(date)

    /** Дата рождения существует, не в будущем и пользователю уже есть 18 лет. */
    fun parseBirthDate(text: String, today: LocalDate = LocalDate.now()): LocalDate? {
        val date = parseDate(text) ?: return null
        val isAdult = !date.plusYears(MIN_DRIVER_AGE.toLong()).isAfter(today)
        val isRealistic = date.isAfter(today.minusYears(MAX_AGE.toLong()))
        return date.takeIf { isAdult && isRealistic }
    }

    fun parseIssueDate(text: String, birthDate: LocalDate?, today: LocalDate = LocalDate.now()): LocalDate? {
        val date = parseDate(text) ?: return null
        if (date.isAfter(today)) return null
        if (birthDate != null && date.isBefore(birthDate)) return null
        return date
    }

    fun isValidLicenseNumber(number: String): Boolean =
        number.length == LICENSE_NUMBER_LENGTH && number.all(Char::isDigit)
}
