package ru.korvalanni.game.unit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.api.Test
import ru.korvalanni.game.utils.GameLogic

class GameLogicTests {

    @Test
    fun `should parse serialized field correctly`() {
        val json = """[["1","1"],["M"," "]]"""
        val result = GameLogic.parseField(json)
        assertEquals(2, result.size)
        assertEquals("M", result[1][0])
        assertEquals(" ", result[1][1])
    }

    @Test
    fun `should return false when clicking on already opened cell`() {
        val field = mutableListOf(
            mutableListOf("1", "1"),
            mutableListOf("1", "1")
        )
        val (newField, completed) = GameLogic.handleMove(field, 0, 0)
        assertFalse(completed)
        assertEquals("1", newField[0][0]) // Не изменилось
    }

    @ParameterizedTest
    @CsvSource(
        "0,0,false", // обычный ход, не победа
        "1,1,false"   // подрыв на мине -> конец
    )
    fun `should handle normal moves and track completion`(row: Int, col: Int, shouldWin: Boolean) {
        val field = mutableListOf(
            mutableListOf(" ", " "),
            mutableListOf(" ", "M")
        )
        val (newField, completed) = GameLogic.handleMove(field, row, col)
        assertTrue(newField[row][col] != " ") // Открыта
        assertEquals(shouldWin, completed)
    }

    @Test
    fun `should mark X and reveal all mines on hit`() {
        val field = mutableListOf(
            mutableListOf(" ", " "),
            mutableListOf("M", "X")
        )
        val (newField, completed) = GameLogic.handleMove(field, 1, 1)
        assertEquals("X", newField[1][1])
        assertEquals("M", newField[1][0]) // другая мина тоже показана
        assertFalse(completed)
    }
}
