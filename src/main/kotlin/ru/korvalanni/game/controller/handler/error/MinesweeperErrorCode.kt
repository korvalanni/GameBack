package ru.korvalanni.game.controller.handler.error

enum class MinesweeperErrorCode(
    val code: String,
    val status: Int,
    val message: String
) {
    UNKNOWN("ERR-000", 500, "Непредвиденная ошибка"),
    GAME_NOT_FOUND("ERR-001", 404, "Игра не найдена"),
    WRONG_COORDINATE("ERR-002", 400, "Неверные координаты"),
    VALIDATION_FAILED("ERR-003", 400, "Ошибка валидации параметров")
}
