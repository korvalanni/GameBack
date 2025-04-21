package ru.korvalanni.game.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ru.korvalanni.game.controller.dto.GameInfoResponse
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.repository.entity.GameEntity

object GameMapper {
    private val mapper = jacksonObjectMapper()

    fun toEmptyEntity(req: NewGameRequest): GameEntity {
        val playerField = List(req.height) { List(req.width) { " " } }
        return GameEntity(
            width = req.width,
            height = req.height,
            minesCount = req.minesCount,
            field = mapper.writeValueAsString(playerField),
            hiddenField = ""
        )
    }

    fun toResponse(entity: GameEntity): GameInfoResponse =
        GameInfoResponse(
            gameId = entity.id,
            width = entity.width,
            height = entity.height,
            minesCount = entity.minesCount,
            completed = entity.completed,
            field = mapper.readValue<List<List<String>>>(entity.field)
        )

    fun updateEntity(entity: GameEntity, newField: List<List<String>>, completed: Boolean): GameEntity =
        entity.copy(
            field = mapper.writeValueAsString(newField),
            completed = completed
        )
}
