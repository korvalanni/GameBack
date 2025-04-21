package ru.korvalanni.game.controller.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@Schema(description = "Информация об игре")
data class GameInfoResponse(
    @Schema(description = "Уникальный идентификатор игры (uuid)", example = "123e4567-e89b-12d3-a456-426614174000")
    val gameId: UUID,

    @Schema(description = "Ширина игрового поля", example = "10")
    val width: Int,

    @Schema(description = "Высота игрового поля", example = "10")
    val height: Int,

    @Schema(description = "Количество мин на поле", example = "10")
    val minesCount: Int,

    @Schema(description = "Признак завершенности игры", example = "false")
    val completed: Boolean,

    @Schema(description = "Игровое поле с отображением ячеек")
    val field: List<List<String>>,
)
