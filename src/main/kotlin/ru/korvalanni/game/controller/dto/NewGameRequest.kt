package ru.korvalanni.game.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import ru.korvalanni.game.validation.ValidMinesCount

@ValidMinesCount
data class NewGameRequest(
    @JsonProperty("width")
    @field:Min(2)
    @field:Max(30)
    val width: Int,

    @JsonProperty("height")
    @field:Min(2)
    @field:Max(30)
    val height: Int,

    @JsonProperty("mines_count")
    @field:Min(1)
    val minesCount: Int
)