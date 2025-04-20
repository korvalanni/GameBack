package ru.korvalanni.game.service

import GameTurnRequest
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.korvalanni.game.repository.GameEntityRepository
import ru.korvalanni.game.repository.entity.GameEntity

import java.util.*
import java.util.stream.Stream

class GameServiceTest {

    private val repository = mock<GameEntityRepository>()
    private val service = GameService(repository)
    private val mapper = ObjectMapper()

    @ParameterizedTest
    @MethodSource("turnOutcomes")
    fun `should return correct completion status and mark cell`(
        field: List<List<String>>,
        row: Int,
        col: Int,
        shouldComplete: Boolean,
        expectedCellValue: String
    ) = runBlocking {
        val id = UUID.randomUUID()

        val entity = GameEntity(
            id = id,
            width = field[0].size,
            height = field.size,
            minesCount = field.flatten().count { it == "M" },
            completed = false,
            field = mapper.writeValueAsString(field),
            version = 1
        )

        whenever(repository.findById(id)).thenReturn(entity)
        whenever(repository.update(any())).thenAnswer { it.arguments[0] }

        val result = service.turn(GameTurnRequest(id, row, col))

        assertEquals(expectedCellValue, result.field[row][col])
        assertEquals(shouldComplete, result.completed)
    }

    companion object {
        @JvmStatic
        fun turnOutcomes(): Stream<Arguments> = Stream.of(
            // Победа: последняя безопасная клетка
            Arguments.of(
                listOf(
                    listOf("1", "1"),
                    listOf(" ", "M")
                ), 1, 0, true, "1"
            ),
            // Не победа: клетка ещё осталась
            Arguments.of(
                listOf(
                    listOf("1", "0"),
                    listOf("0", "M")
                ), 1, 0, false, "0"
            ),
            // Поражение: попали на мину
            Arguments.of(
                listOf(
                    listOf("M", "1"),
                    listOf("1", "1")
                ), 0, 0, false, "X"
            )
        )
    }
}
