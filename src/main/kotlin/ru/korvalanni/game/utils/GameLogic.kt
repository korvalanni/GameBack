package ru.korvalanni.game.utils

import com.fasterxml.jackson.databind.ObjectMapper

object GameLogic {

    private val mapper = ObjectMapper()

    fun parseField(serialized: String): List<List<String>> =
        mapper.readValue(serialized, List::class.java) as List<List<String>>

    fun handleMove(field: MutableList<MutableList<String>>, row: Int, col: Int): Pair<List<List<String>>, Boolean> {
        if (!isInBounds(field, row, col)) return field to false

        when (field[row][col]) {
            "M" -> {
                revealAllCells(field, row, col)
                return field to false
            }
            " " -> openCell(field, row, col)
            else -> return field to false
        }

        val total = field.size * field[0].size
        val revealed = field.flatten().count { it != " " && it != "M" }
        val expectedRevealed = total - field.flatten().count { it == "M" }
        val completed = revealed == expectedRevealed


        return field to completed
    }



    private fun openCell(field: MutableList<MutableList<String>>, row: Int, col: Int) {
        if (row !in field.indices || col !in field[0].indices) return
        if (field[row][col] != " ") return

        val count = countAdjacentMines(field, row, col)
        field[row][col] = count.toString()

        if (count == 0) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr != 0 || dc != 0) {
                        openCell(field, row + dr, col + dc) // рекурсивный вызов
                    }
                }
            }
        }
    }

    private fun revealAllCells(field: MutableList<MutableList<String>>, clickedRow: Int, clickedCol: Int) {
        for (r in field.indices) {
            for (c in field[0].indices) {
                when (field[r][c]) {
                    "M" -> field[r][c] = if (r == clickedRow && c == clickedCol) "X" else "M"
                    " " -> {
                        val count = countAdjacentMines(field, r, c)
                        field[r][c] = count.toString()
                    }
                }
            }
        }
    }


    private fun countAdjacentMines(field: List<List<String>>, row: Int, col: Int): Int {
        val directions = listOf(-1 to -1, -1 to 0, -1 to 1,
            0 to -1,          0 to 1,
            1 to -1, 1 to 0,  1 to 1)
        return directions.count { (dr, dc) ->
            val r = row + dr
            val c = col + dc
            isInBounds(field, r, c) && field[r][c] == "M"
        }
    }

    private fun isInBounds(field: List<List<String>>, row: Int, col: Int): Boolean =
        row in field.indices && col in field[0].indices
}