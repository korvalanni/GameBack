package ru.korvalanni.game.exception

import java.util.*

open class GameException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class GameNotFoundException(val id: UUID) : GameException("Game with ID $id not found")
object GameAlreadyCompletedException : GameException("Game already completed")
class InvalidCoordinatesException(val row: Int, val col: Int) : GameException("Invalid coordinates: row=$row, col=$col")
object GameCreationFailedException : GameException("Failed to create game")
object GameTurnFailedException : GameException("Failed to make a move")
object InvalidFieldSizeException : GameException("Field size must be at least 2x2")
class InvalidMinesCountException(val count: Int, val max: Int) : GameException("Mines count $count is invalid; must be between 1 and ${max - 1}")

