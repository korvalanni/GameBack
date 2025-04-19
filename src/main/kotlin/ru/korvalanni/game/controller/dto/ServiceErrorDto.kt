package ru.korvalanni.game.controller.dto

data class ServiceErrorDto(
    val code: String,
    val status: Int,
    val message: String
)