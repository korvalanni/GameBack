package ru.korvalanni.game.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ServiceErrorDto(
    val code: String? = null,
    val status: Int? = null,
    val error: String? = null
)
