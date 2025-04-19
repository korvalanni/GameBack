package ru.korvalanni.game.utils

import GameInfoResponse
import com.fasterxml.jackson.databind.ObjectMapper
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.repository.entity.GameEntity

object GameMapper {
    private val mapper = ObjectMapper()

    fun toEntity(req: NewGameRequest, field: List<List<String>>): GameEntity =
        GameEntity(
            width = req.width,
            height = req.height,
            minesCount = req.minesCount,
            field = mapper.writeValueAsString(field)
        )

    fun toResponse(entity: GameEntity): GameInfoResponse =
        GameInfoResponse(
            gameId = entity.id,
            width = entity.width,
            height = entity.height,
            minesCount = entity.minesCount,
            completed = entity.completed,
            field = mapper.readValue(entity.field, List::class.java) as List<List<String>>
        )

    fun updateEntity(entity: GameEntity, newField: List<List<String>>, completed: Boolean): GameEntity =
        entity.copy(
            field = mapper.writeValueAsString(newField),
            completed = completed
        )
}