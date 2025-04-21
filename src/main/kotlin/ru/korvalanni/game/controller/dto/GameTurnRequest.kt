package ru.korvalanni.game.controller.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@Schema(description = "Запрос на совершение хода в игре")
data class GameTurnRequest(
    @Schema(description = "Уникальный идентификатор игры", example = "123e4567-e89b-12d3-a456-426614174000")
    val gameId: UUID,

    @Schema(description = "Индекс строки для хода", example = "5", minimum = "0", maximum = "29")
    @field:Min(0)
    @field:Max(29)
    val row: Int,

    @Schema(description = "Индекс столбца для хода", example = "5", minimum = "0", maximum = "29")
    @field:Min(0)
    @field:Max(29)
    val col: Int,
)
