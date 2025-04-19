package ru.korvalanni.game.controller

import GameInfoResponse
import GameTurnRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.service.GameService

@RestController
@RequestMapping("/api")
class GameController(
    private val service: GameService
) {
    @PostMapping("/new")
    suspend fun newGame(@Valid @RequestBody req: NewGameRequest): GameInfoResponse =
        service.newGame(req)

    @PostMapping("/turn")
    suspend fun turn(@RequestBody req: GameTurnRequest): GameInfoResponse =
        service.turn(req)

    @GetMapping
    fun ping(): String = "OK"

}