package ru.korvalanni.game.service

import GameInfoResponse
import GameTurnRequest
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.utils.FieldGenerator
import ru.korvalanni.game.utils.GameMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.korvalanni.game.exception.GameAlreadyCompletedException
import ru.korvalanni.game.exception.GameCreationFailedException
import ru.korvalanni.game.exception.GameNotFoundException
import ru.korvalanni.game.exception.InvalidCoordinatesException

import ru.korvalanni.game.repository.GameEntityRepository
import ru.korvalanni.game.utils.GameLogic

@Service
class GameService(
    private val repository: GameEntityRepository
) {
    private val log = LoggerFactory.getLogger(GameService::class.java)

    suspend fun newGame(req: NewGameRequest): GameInfoResponse =
        runCatching {
            val field = FieldGenerator.generateField(req.width, req.height, req.minesCount)
            val entity = GameMapper.toEntity(req, field)
            val saved = repository.save(entity)
            log.info("New game created: \${saved.id}")
            GameMapper.toResponse(saved)
        }.onFailure { log.error("Failed to create game", it) }
            .getOrElse { throw GameCreationFailedException() }

    suspend fun turn(input: GameTurnRequest): GameInfoResponse {
        val entity = repository.findById(input.gameId)
            ?: throw GameNotFoundException(input.gameId)

        if (entity.completed) throw GameAlreadyCompletedException()

        if (input.row !in 0 until entity.height || input.col !in 0 until entity.width)
            throw InvalidCoordinatesException(input.row, input.col)

        val field = GameLogic.parseField(entity.field)
        val (newField, completed) = GameLogic.handleMove(field.map { it.toMutableList() }.toMutableList(), input.row, input.col)
        val updatedEntity = GameMapper.updateEntity(entity, newField, completed)
        repository.update(updatedEntity)

        return GameMapper.toResponse(updatedEntity)
    }

}
