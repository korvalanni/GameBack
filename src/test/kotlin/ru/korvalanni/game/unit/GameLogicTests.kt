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
    }

    @ParameterizedTest
    @CsvSource(
        "0,0,false", // обычный ход, не выигрыш
        "1,1,true",  // последняя безопасная клетка
    )
    fun `should handle normal moves`(row: Int, col: Int, shouldWin: Boolean) {
        val field = mutableListOf(
            mutableListOf("1", "1"),
            mutableListOf("1", " ")
        )
        val (newField, completed) = GameLogic.handleMove(field, row, col)
        assertEquals("1", newField[row][col])
        assertEquals(shouldWin, completed)
    }

    @Test
    fun `should handle mine hit`() {
        val field = mutableListOf(
            mutableListOf("1", "1"),
            mutableListOf("M", "1")
        )
        val (newField, completed) = GameLogic.handleMove(field, 1, 0)
        assertEquals("X", newField[1][0])
        assertTrue(completed)
    }
}
