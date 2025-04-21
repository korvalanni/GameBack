package ru.korvalanni.game

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import ru.korvalanni.game.controller.Paths.API_NEW_GAME
import ru.korvalanni.game.controller.Paths.API_TURN
import ru.korvalanni.game.controller.dto.GameInfoResponse
import ru.korvalanni.game.controller.dto.GameTurnRequest
import ru.korvalanni.game.controller.dto.NewGameRequest
import java.util.*
import java.util.stream.Stream

class GameApplicationTests : AbstractContainerTest() {

	@Autowired
	private lateinit var webTestClient: WebTestClient

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	private fun createNewGame(width: Int, height: Int, minesCount: Int): GameInfoResponse {
		val request = NewGameRequest(width, height, minesCount)
		val result = webTestClient.post().uri(API_NEW_GAME)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.game_id").exists()
			.jsonPath("$.width").isEqualTo(width)
			.jsonPath("$.height").isEqualTo(height)
			.jsonPath("$.mines_count").isEqualTo(minesCount)
			.jsonPath("$.field").isArray
			.jsonPath("$.completed").isEqualTo(false)
			.returnResult()

		val content = result.responseBody?.let { String(it) } ?: ""
		return objectMapper.readValue(content, GameInfoResponse::class.java)
	}

	@Test
	fun `should create game with valid parameters`() {
		listOf(
			Triple(2, 2, 1),
			Triple(30, 30, 30 * 30 - 1)
		).forEach { (w, h, m) ->
			val game = createNewGame(w, h, m)
			assertThat(game.width).isEqualTo(w)
			assertThat(game.height).isEqualTo(h)
			assertThat(game.minesCount).isEqualTo(m)
			assertThat(game.field).hasSize(h)
			assertThat(game.field[0]).hasSize(w)
		}
	}

	@ParameterizedTest
	@MethodSource("invalidGameCreationParams")
	fun `should fail when creating game with invalid parameters`(width: Int, height: Int, minesCount: Int) {
		webTestClient.post().uri(API_NEW_GAME)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(NewGameRequest(width, height, minesCount))
			.exchange()
			.expectStatus().isBadRequest
			.expectBody().jsonPath("$.error").exists()
	}

	@ParameterizedTest
	@MethodSource("outOfBoundsParams")
	fun `should fail when making turn outside field boundaries for existing game`(row: Int, col: Int) {
		val game = createNewGame(5, 5, 3)
		performTurn(game.gameId, row, col)
			.expectStatus().isBadRequest
			.expectBody().jsonPath("$.error").exists()
	}

	@Test
	fun `should return 400 when gameId is null`() {
		val invalidJson = """{"game_id":null,"row":0,"col":0}"""
		webTestClient.post().uri(API_TURN)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalidJson)
			.exchange()
			.expectStatus().isBadRequest
			.expectBody().jsonPath("$.error").exists()
	}

	@Test
	fun `should fail when repeating turn on already opened cell`() {
		var badRequestReceived = false
		var attempts = 0

		while (!badRequestReceived && attempts < MAX_ATTEMPTS) {
			attempts++
			val game = createNewGame(10, 10, 1)

			val firstTurnInfo = performTurnAndExpectSuccess(game.gameId, 0, 0)

			if (firstTurnInfo.completed) continue

			val secondTurnResult = performTurn(game.gameId, 0, 0)

			try {
				secondTurnResult
					.expectStatus().isBadRequest
					.expectBody().jsonPath("$.error").exists()

				badRequestReceived = true
				break
			} catch (e: Exception) { }
		}

		assertThat(badRequestReceived).withFailMessage("Did not receive BadRequest on repeated turn after $MAX_ATTEMPTS attempts")
			.isTrue()
	}

	@Test
	fun `should complete game when hitting mine`() {
		var lostInfo: GameInfoResponse? = null
		var attempts = 0
		while (lostInfo == null && attempts < MAX_ATTEMPTS) {
			attempts++
			val game = createNewGame(3, 3, 7)
			val coords = (0 until game.height).flatMap { r -> (0 until game.width).map { c -> r to c } }.shuffled()

			for ((r, c) in coords) {
				try {
					val result = performTurn(game.gameId, r, c)
						.expectBody(GameInfoResponse::class.java)
						.returnResult()

					val info = result.responseBody
					if (info != null && info.completed) {
						val hitMine = info.field.any { row -> row.any { it == "X" } }
						if (hitMine) {
							lostInfo = info
							break
						}
					}
				} catch (e: Exception) { }
			}
		}

		assertThat(lostInfo).withFailMessage("Game did not result in a loss after $MAX_ATTEMPTS attempts.").isNotNull
		assertThat(lostInfo!!.completed).isTrue()
		assertThat(lostInfo.field).`as`("Field should contain an exploded mine ('X')")
			.anyMatch { row -> row.any { it == "X" } }
	}

	@Test
	fun `should auto-open adjacent cells when opening cell with zero mines nearby`() {
		var autoOpenVerified = false
		var attempts = 0
		var lastInfo: GameInfoResponse? = null

		while (!autoOpenVerified && attempts < MAX_ATTEMPTS) {
			attempts++
			val game = createNewGame(5, 5, 1)
			try {
				val info = performTurnAndExpectSuccess(game.gameId, 0, 0)
				lastInfo = info
				val opened = info.field.sumOf { row -> row.count { it != " " } }
				if (opened > 1) {
					autoOpenVerified = true
				}
				if (info.completed && !autoOpenVerified) continue

			} catch (e: Exception) { }
		}

		assertThat(autoOpenVerified)
			.withFailMessage("Auto-opening did not occur at (0,0) after $MAX_ATTEMPTS attempts. Last state: $lastInfo")
			.isTrue()
	}

	@Test
	fun `should win game when all safe cells are opened`() {
		val width = 2
		val height = 2
		val minesCount = 1
		val safeCellsCount = width * height - minesCount

		var winningInfo: GameInfoResponse? = null
		var attempts = 0

		while (winningInfo == null && attempts < MAX_ATTEMPTS) {
			attempts++
			val game = createNewGame(width, height, minesCount)

			// Перебираем все ячейки, пока не выиграем или не проиграем
			val coords = listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1).shuffled()

			for ((r, c) in coords) {
				val info = performTurnAndExpectSuccess(game.gameId, r, c)

				if (info.completed) {
					// Если нет подорванных мин (X), значит выиграли
					if (!info.field.any { row -> row.contains("X") }) {
						winningInfo = info
					}
					break
				}
			}
		}

		assertThat(winningInfo).withFailMessage("Game could not be won after $MAX_ATTEMPTS attempts").isNotNull

		assertThat(winningInfo!!.completed).isTrue()
		val revealedMines = winningInfo.field.sumOf { row -> row.count { it == "M" } }
		val revealedExploded = winningInfo.field.sumOf { row -> row.count { it == "X" } }
		val revealedSafe = winningInfo.field.sumOf { row -> row.count { it != " " && it != "M" && it != "X" } }

		assertThat(revealedMines).`as`("Check number of revealed mines on win").isEqualTo(minesCount)
		assertThat(revealedExploded).`as`("Check no exploded mines on win").isEqualTo(0)
		assertThat(revealedSafe).`as`("Check number of revealed safe cells on win").isEqualTo(safeCellsCount)
	}

	@Test
	fun `should return game parameters on valid turn`() {
		val width = 5
		val height = 5
		val mines = 3
		val game = createNewGame(width, height, mines)
		val info = performTurnAndExpectSuccess(game.gameId, 0, 0)
		assertThat(info.gameId).isEqualTo(game.gameId)
		assertThat(info.width).isEqualTo(width)
		assertThat(info.height).isEqualTo(height)
		assertThat(info.minesCount).isEqualTo(mines)
	}

	private fun performTurn(gameId: UUID, row: Int, col: Int) =
		webTestClient.post().uri(API_TURN)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(GameTurnRequest(gameId, row, col))
			.exchange()

	private fun performTurnAndExpectSuccess(gameId: UUID, row: Int, col: Int): GameInfoResponse {
		val response = performTurn(gameId, row, col)
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.game_id").isEqualTo(gameId.toString())
			.jsonPath("$.field").isArray
			.returnResult()
		val content = response.responseBody?.let { String(it) } ?: ""
		return objectMapper.readValue(content, GameInfoResponse::class.java)
	}

	companion object {
		private const val MAX_ATTEMPTS = 100

		@JvmStatic
		fun invalidGameCreationParams(): Stream<Arguments> = Stream.of(
			Arguments.of(31, 10, 5),
			Arguments.of(10, 31, 5),
			Arguments.of(5, 5, 25),
			Arguments.of(-1, 5, 3)
		)

		@JvmStatic
		fun outOfBoundsParams(): Stream<Arguments> = Stream.of(
			Arguments.of(5, 0),
			Arguments.of(0, 5),
			Arguments.of(-1, 0),
			Arguments.of(0, -1)
		)
	}
}
