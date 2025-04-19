package ru.korvalanni.game.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.repository.GameEntityRepository

import ru.korvalanni.game.utils.GameMapper

class GameServiceTest {

    private val repository: GameEntityRepository = mock()
    private val service = GameService(repository)

    @Test
    fun `should create game with correct dimensions`() = runBlocking {
        val req = NewGameRequest(5, 5, 5)
        val entity = GameMapper.toEntity(req, listOf())

        whenever(repository.save(any())).thenReturn(entity)

        val response = service.newGame(req)
        assertEquals(5, response.width)
        assertEquals(5, response.height)
        assertEquals(5, response.minesCount)
    }
}
