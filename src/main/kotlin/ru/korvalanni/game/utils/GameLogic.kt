package ru.korvalanni.game.utils

import com.fasterxml.jackson.databind.ObjectMapper

object GameLogic {

    private val mapper = ObjectMapper()

    fun parseField(serialized: String): List<List<String>> =
        mapper.readValue(serialized, List::class.java) as List<List<String>>

    fun handleMove(field: MutableList<MutableList<String>>, row: Int, col: Int): Pair<List<List<String>>, Boolean> {
        if (field[row][col] == "M") {
            field[row][col] = "X"
            return field to true
        }

        field[row][col] = "1"
        val completed = field.flatten().none { it == " " || it == "M" }
        return field to completed
    }
}