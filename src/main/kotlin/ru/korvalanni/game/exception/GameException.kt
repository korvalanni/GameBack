package ru.korvalanni.game.exception

import java.util.UUID

open class GameException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class GameNotFoundException(id: UUID) : GameException("Game with ID $id not found")
class GameAlreadyCompletedException : GameException("Game already completed")
class InvalidCoordinatesException(row: Int, col: Int) : GameException("Invalid coordinates: row=$row, col=$col")
class GameCreationFailedException : GameException("Failed to create game")
