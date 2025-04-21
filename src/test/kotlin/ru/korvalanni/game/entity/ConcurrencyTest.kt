package ru.korvalanni.game.entity

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.korvalanni.game.AbstractContainerTest
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.repository.GameEntityRepository
import ru.korvalanni.game.service.GameService

@SpringBootTest
class ConcurrencyTest @Autowired constructor(
    private val repository: GameEntityRepository,
    private val service: GameService,
): AbstractContainerTest() {
    @Test
    fun `should fail concurrent updates on same entity`() = runBlocking {
        val created = service.newGame(NewGameRequest(5, 5, 3))
        val base = repository.findById(created.gameId) ?: throw Exception("Entity not found")

        val errors = mutableListOf<Throwable>()

        coroutineScope {
            repeat(10) {
                launch {
                    try {
                        repository.update(base.copy(completed = true), false)
                    } catch (e: Exception) {
                        synchronized(errors) { errors.add(e) }
                    }
                }
            }
        }
        assert(errors.isNotEmpty()) { "Expected at least one concurrent modification failure" }
    }

}
