package ru.korvalanni.game.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.korvalanni.game.controller.dto.GameInfoResponse
import ru.korvalanni.game.controller.dto.GameTurnRequest
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.exception.CellAlreadyOpenedException
import ru.korvalanni.game.exception.GameAlreadyCompletedException
import ru.korvalanni.game.exception.GameCreationFailedException
import ru.korvalanni.game.exception.GameNotFoundException
import ru.korvalanni.game.exception.InvalidCoordinatesException
import ru.korvalanni.game.repository.GameEntityRepository
import ru.korvalanni.game.utils.FieldGenerator
import ru.korvalanni.game.utils.GameLogic
import ru.korvalanni.game.utils.GameMapper

/**
 * Сервис для управления игровой логикой "Сапёра".
 *
 * @property repository Репозиторий для доступа к данным игры.
 */
@Service
class GameService(
    private val repository: GameEntityRepository,
) {
    /**
     * Создает новую игру.
     *
     * @param req Запрос на создание новой игры, содержащий параметры поля.
     * @return Информация о созданной игре.
     * @throws GameCreationFailedException Если произошла ошибка при создании игры.
     */
    suspend fun newGame(req: NewGameRequest): GameInfoResponse =
        runCatching {
            val entity = GameMapper.toEmptyEntity(req)
            val saved = repository.save(entity)
            log.info { "New game created: ${saved.id}" }
            GameMapper.toResponse(saved)
        }.onFailure { ex: Throwable ->
            log.error(ex) { "Failed to create game" }
        }.getOrElse { throw GameCreationFailedException() }

    /**
     * Обрабатывает ход игрока в указанной игре.
     *
     * @param input Запрос на ход, содержащий ID игры и координаты ячейки.
     * @return Обновленная информация об игре после хода.
     * @throws GameNotFoundException Если игра с указанным ID не найдена.
     * @throws InvalidCoordinatesException Если указанные координаты выходят за пределы поля.
     * @throws GameAlreadyCompletedException Если игра уже завершена.
     * @throws CellAlreadyOpenedException Если выбранная ячейка уже была открыта ранее.
     */
    suspend fun turn(input: GameTurnRequest): GameInfoResponse {
        val entity = repository.findById(input.gameId)
            ?: throw GameNotFoundException(input.gameId)

        if (input.row !in 0 until entity.height || input.col !in 0 until entity.width)
            throw InvalidCoordinatesException(input.row, input.col)

        if (entity.completed) throw GameAlreadyCompletedException()

        val playerField = GameLogic.parseField(entity.field)
            .map { it.toMutableList() }.toMutableList()

        // Проверка первого хода - если hiddenField пустой, генерируем поле с минами
        val isFirstMove = entity.hiddenField.isEmpty()
        val hiddenField = if (isFirstMove) {
            // Генерируем поле с минами, исключая первый клик пользователя
            val generatedField = FieldGenerator.generateField(
                entity.width,
                entity.height,
                entity.minesCount,
                excludeRow = input.row,
                excludeCol = input.col
            )
            generatedField
        } else {
            GameLogic.parseField(entity.hiddenField)
        }

        if (playerField[input.row][input.col] != " ") {
            throw CellAlreadyOpenedException(input.row, input.col)
        }

        // При первом ходе обновить скрытое поле (hiddenField) в сущности
        val updatedEntity = if (isFirstMove) {
            val mapper = ObjectMapper()
            val serializedHiddenField = mapper.writeValueAsString(hiddenField)
            entity.copy(hiddenField = serializedHiddenField)
        } else {
            entity
        }

        val (newPlayerField, completed) = GameLogic.handleMove(
            playerField,
            hiddenField,
            input.row,
            input.col
        )

        val finalEntity = GameMapper.updateEntity(updatedEntity, newPlayerField, completed)
        repository.update(finalEntity, isFirstMove)

        return GameMapper.toResponse(finalEntity)
    }

    private companion object {
        val log = KotlinLogging.logger { }
    }
}
