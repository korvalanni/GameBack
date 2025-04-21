package ru.korvalanni.game.unit


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.korvalanni.game.utils.FieldGenerator

class FieldGeneratorTests {

    @ParameterizedTest
    @CsvSource(
        "10, 10, 15",
        "5, 5, 5",
        "20, 10, 50"
    )
    fun `should generate correct number of mines`(width: Int, height: Int, mines: Int) {
        val field = FieldGenerator.generateField(width, height, mines, 0, 0)
        val mineCount = field.sumOf { row -> row.count { it == "M" } }
        assertEquals(mines, mineCount)
    }

    @ParameterizedTest
    @CsvSource("5, 5, 5", "10, 10, 10")
    fun `should not place mines out of bounds`(width: Int, height: Int, mines: Int) {
        val field = FieldGenerator.generateField(width, height, mines, 0, 0)
        assertEquals(height, field.size)
        assertTrue(field.all { it.size == width })
    }

    @Test
    fun `should not place mine at excluded position`() {
        val width = 5
        val height = 5
        val excludeRow = 2
        val excludeCol = 3
        val mines = 5
        val field = FieldGenerator.generateField(width, height, mines, excludeRow, excludeCol)
        assertEquals(" ", field[excludeRow][excludeCol])
    }

    @Test
    fun `should cap minesCount when exceeding positions`() {
        val width = 2
        val height = 2
        val excludeRow = 0
        val excludeCol = 0
        val mines = 10
        val field = FieldGenerator.generateField(width, height, mines, excludeRow, excludeCol)
        val mineCount = field.sumOf { row -> row.count { it == "M" } }
        assertEquals(width * height - 1, mineCount)
    }

}
