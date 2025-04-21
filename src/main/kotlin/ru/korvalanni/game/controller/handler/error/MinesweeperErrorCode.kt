package ru.korvalanni.game.controller.handler.error

enum class MinesweeperErrorCode(
    val code: String,
    val status: Int,
    val message: String,
) {
    UNKNOWN("ERR-000", 500, "Непредвиденная ошибка"),
    VALIDATION_FAILED("ERR-001", 400, "Ошибка валидации параметров")
}
