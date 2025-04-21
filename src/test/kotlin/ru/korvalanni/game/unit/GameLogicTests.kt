package ru.korvalanni.game.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
            mutableListOf(" ", " "),
            mutableListOf(" ", " ")
        )
        val hiddenField = listOf(
            listOf("1", "1"),
            listOf("1", "M")
        )
        val (newField, completed) = GameLogic.handleMove(field, hiddenField, 0, 0)
        assertFalse(completed)
        assertEquals("1", newField[0][0])
    }

    @ParameterizedTest
    @CsvSource(
        "0,0,false", // обычный ход, не победа
        "1,1,true"   // подрыв на мине -> конец
    )
    fun `should handle normal moves and track completion`(row: Int, col: Int, shouldComplete: Boolean) {
        val field = mutableListOf(
            mutableListOf(" ", " "),
            mutableListOf(" ", " ")
        )
        val hiddenField = listOf(
            listOf("1", "1"),
            listOf("1", "M")
        )
        val (newField, completed) = GameLogic.handleMove(field, hiddenField, row, col)
        assertTrue(newField[row][col] != " ")
        assertEquals(shouldComplete, completed)
    }

    @Test
    fun `should mark X and reveal all mines on hit`() {
        val playerField = mutableListOf(
            mutableListOf(" ", " "),
            mutableListOf(" ", " ")
        )
        val hiddenField = listOf(
            listOf("1", "1"),
            listOf("M", "M")
        )
        val (newField, completed) = GameLogic.handleMove(playerField, hiddenField, 1, 1)
        assertEquals("X", newField[1][1])
        assertEquals("X", newField[1][0])
        assertTrue(completed)
    }
}
