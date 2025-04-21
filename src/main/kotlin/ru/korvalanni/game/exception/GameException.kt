package ru.korvalanni.game.exception

import org.springframework.http.HttpStatus
import java.util.UUID

open class GameException(
    message: String,
    val statusCode: HttpStatus = HttpStatus.BAD_REQUEST,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class GameNotFoundException(id: UUID) : GameException("Game with ID $id not found", HttpStatus.NOT_FOUND)
class GameAlreadyCompletedException : GameException("Game already completed")
class InvalidCoordinatesException(row: Int, col: Int) : GameException("Invalid coordinates: row=$row, col=$col")
class GameCreationFailedException : GameException("Failed to create game")
class CellAlreadyOpenedException(row: Int, col: Int) :
    GameException("Cell at ($row, $col) has already been opened.")
