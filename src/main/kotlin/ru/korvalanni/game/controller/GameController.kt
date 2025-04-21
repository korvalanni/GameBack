package ru.korvalanni.game.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.korvalanni.game.controller.Paths.API_BASE
import ru.korvalanni.game.controller.Paths.NEW_GAME
import ru.korvalanni.game.controller.Paths.TURN
import ru.korvalanni.game.controller.dto.GameInfoResponse
import ru.korvalanni.game.controller.dto.GameTurnRequest
import ru.korvalanni.game.controller.dto.NewGameRequest
import ru.korvalanni.game.controller.dto.ServiceErrorDto
import ru.korvalanni.game.service.GameService

@Tag(name = "Игровой API", description = "Операции для создания и игры в сапера")
@RestController
@Validated
@RequestMapping(API_BASE)
class GameController(
    private val service: GameService,
) {
    @Operation(
        summary = "Создать новую игру",
        description = "Создает новую игру с указанными размерами поля и количеством мин"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Игра успешно создана"),
            ApiResponse(
                responseCode = "400", description = "Некорректные параметры запроса",
                content = [Content(schema = Schema(implementation = ServiceErrorDto::class))]
            )
        ]
    )
    @PostMapping(NEW_GAME)
    suspend fun newGame(
        @Parameter(description = "Параметры новой игры")
        @Valid @RequestBody req: NewGameRequest,
    ): GameInfoResponse = service.newGame(req)

    @Operation(summary = "Сделать ход", description = "Выполняет ход в указанной игре по переданным координатам")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ход успешно выполнен"),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные параметры запроса или невозможно выполнить ход",
                content = [Content(schema = Schema(implementation = ServiceErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Игра не найдена",
                content = [Content(schema = Schema(implementation = ServiceErrorDto::class))]
            )
        ]
    )
    @PostMapping(TURN)
    suspend fun turn(
        @Parameter(description = "Параметры хода")
        @Valid @RequestBody req: GameTurnRequest,
    ): GameInfoResponse = service.turn(req)
}
