package ru.korvalanni.game.unit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
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
        val field = FieldGenerator.generateField(width, height, mines)
        val mineCount = field.sumOf { row -> row.count { it == "M" } }
        assertEquals(mines, mineCount)
    }

    @ParameterizedTest
    @CsvSource("3, 3, 10", "2, 2, 5")
    fun `should throw when mines exceed cells`(width: Int, height: Int, mines: Int) {
        assertThrows<IllegalArgumentException> {
            FieldGenerator.generateField(width, height, mines)
        }
    }

    @ParameterizedTest
    @CsvSource("5, 5, 5", "10, 10, 10")
    fun `should not place mines out of bounds`(width: Int, height: Int, mines: Int) {
        val field = FieldGenerator.generateField(width, height, mines)
        assertEquals(height, field.size)
        assertTrue(field.all { it.size == width })
    }
}