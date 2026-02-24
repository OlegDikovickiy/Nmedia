package ru.netology.nmadia_hw.error

sealed class AppError(message: String) : RuntimeException(message)

class ApiError(val code: Int, message: String) : AppError("Ошибка сервера ($code): $message")
object NetworkError : AppError("Ошибка сети")
object UnknownError : AppError("Неизвестная ошибка")