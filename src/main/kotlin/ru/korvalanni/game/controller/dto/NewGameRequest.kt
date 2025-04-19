package ru.korvalanni.game.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class NewGameRequest(
    @JsonProperty("width")
    @field:Min(2)
    @field:Max(100)
    val width: Int,

    @JsonProperty("height")
    @field:Min(2)
    @field:Max(100)
    val height: Int,

    @JsonProperty("mines_count")
    @field:Min(1)
    val minesCount: Int
)