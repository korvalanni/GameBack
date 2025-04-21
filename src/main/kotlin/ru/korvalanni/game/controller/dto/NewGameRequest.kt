package ru.korvalanni.game.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import ru.korvalanni.game.controller.validation.ValidMinesCount

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@Schema(description = "Запрос на создание новой игры")
@ValidMinesCount
data class NewGameRequest(
    @Schema(description = "Ширина игрового поля", example = "10", minimum = "2", maximum = "30")
    @JsonProperty("width")
    @field:Min(2)
    @field:Max(30)
    val width: Int,

    @Schema(description = "Высота игрового поля", example = "10", minimum = "2", maximum = "30")
    @JsonProperty("height")
    @field:Min(2)
    @field:Max(30)
    val height: Int,

    @Schema(description = "Количество мин на поле", example = "10", minimum = "1")
    @JsonProperty("mines_count")
    @field:Min(1)
    val minesCount: Int,
)
