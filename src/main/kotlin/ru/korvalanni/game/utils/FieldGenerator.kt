package ru.korvalanni.game.utils

object FieldGenerator {
    fun generateField(width: Int, height: Int, minesCount: Int): List<List<String>> {
        val totalCells = width * height
        val flatPositions = (0 until totalCells).shuffled().take(minesCount)

        val field = MutableList(height) { MutableList(width) { " " } }
        for (pos in flatPositions) {
            val row = pos / width
            val col = pos % width
            field[row][col] = "M"
        }

        return field
    }

}