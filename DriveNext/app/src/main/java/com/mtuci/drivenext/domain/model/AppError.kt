package com.mtuci.drivenext.domain.model

/** Ошибки, которые data-слой отдаёт наверх вместо исключений конкретных библиотек. */
sealed class AppError : Exception() {
    class NoConnection : AppError()
    class NotConfigured : AppError()
    class InvalidCredentials : AppError()
    class EmailNotConfirmed : AppError()
    class UserAlreadyExists : AppError()
    class WeakPassword : AppError()
    class TooManyRequests : AppError()
    class FileReadFailed : AppError()
    class SessionExpired : AppError()
    class Server(val details: String?) : AppError()
    class Unknown(val details: String?) : AppError()
}
