package ru.korvalanni.game

import GameTurnRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import java.util.stream.Stream

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameApplicationTests {

	@LocalServerPort
	var port: Int = 0

	lateinit var client: WebTestClient
	lateinit var repeatGameId: UUID

	@BeforeEach
	fun setup() {
		client = WebTestClient.bindToServer()
			.baseUrl("http://localhost:$port/api")
			.build()

		createGamesTable()

		val request = mapOf("width" to 5, "height" to 5, "mines_count" to 3)

		client.post().uri("/new")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.game_id")
			.value<String> {repeatGameId = UUID.fromString(it)}

		// Совершим первый ход по координате (0, 0)
		postTurn(GameTurnRequest(repeatGameId, 0, 0)).exchange().expectStatus().isOk
	}

	private fun createGamesTable() {
		val options = io.r2dbc.spi.ConnectionFactoryOptions.builder()
			.option(io.r2dbc.spi.ConnectionFactoryOptions.DRIVER, "postgresql")
			.option(io.r2dbc.spi.ConnectionFactoryOptions.HOST, postgres.host)
			.option(io.r2dbc.spi.ConnectionFactoryOptions.PORT, postgres.getMappedPort(5432))
			.option(io.r2dbc.spi.ConnectionFactoryOptions.USER, "user")
			.option(io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD, "password")
			.option(io.r2dbc.spi.ConnectionFactoryOptions.DATABASE, "game")
			.build()

		val client = DatabaseClient.create(io.r2dbc.spi.ConnectionFactories.get(options))

		client.sql(
			"""
            CREATE TABLE IF NOT EXISTS games (
                id UUID PRIMARY KEY,
                width INT NOT NULL,
                height INT NOT NULL,
                mines_count INT NOT NULL,
                completed BOOLEAN NOT NULL,
                field TEXT[][] NOT NULL,
                version INT NOT NULL
            )
        """.trimIndent()
		).then().block()
	}


	private fun postTurn(request: GameTurnRequest) =
		client.post().uri("/turn")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)

	companion object {
		@Container
		val postgres = PostgreSQLContainer("postgres:15").apply {
			withDatabaseName("game")
			withUsername("user")
			withPassword("password")
			withReuse(false) // каждый раз создаётся новый контейнер
		}

		@JvmStatic
		@DynamicPropertySource
		fun configure(registry: DynamicPropertyRegistry) {
			registry.add("spring.r2dbc.url") { "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/game" }
			registry.add("spring.r2dbc.username") { "user" }
			registry.add("spring.r2dbc.password") { "password" }

			registry.add("spring.liquibase.url") { "jdbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/game" }
			registry.add("spring.liquibase.user") { "user" }
			registry.add("spring.liquibase.password") { "password" }
			registry.add("spring.liquibase.enabled") { "true" }
			registry.add("spring.liquibase.change-log") { "classpath:/db/changelog/db.changelog-master.yaml" }
		}

		@JvmStatic
		fun invalidTurnCases(): Stream<Arguments> = Stream.of(
			Arguments.of(UUID.fromString("01234567-89AB-CDEF-0123-456789ABCDEF"), 100, 100, 404),
			Arguments.of(UUID.randomUUID(), 0, 0, 404),
		)
	}

	@ParameterizedTest
	@MethodSource("invalidTurnCases")
	fun `should fail on invalid turns`(gameId: UUID, row: Int, col: Int, expectedStatus: Int) {
		val request = GameTurnRequest(gameId, row, col)
		postTurn(request).exchange()
			.expectStatus().isEqualTo(expectedStatus)
	}

	@Test
	fun `should return 400 when gameId is null`() {
		val request = mapOf(
			"game_id" to null,
			"row" to 0,
			"col" to 0
		)

		client.post().uri("/turn")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest
	}

	@Test
	fun `should create game and perform valid moves`() {
		val request = mapOf("width" to 5, "height" to 5, "mines_count" to 3)

		val created = client.post().uri("/new")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.game_id").exists()
			.returnResult()

		val gameId = created.responseBodyContent
			?.toString(Charsets.UTF_8)
			?.let {
				Regex("\"game_id\"\\s*:\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(1)
			}?.let(UUID::fromString)
			?: error("Game ID not found")

		postTurn(GameTurnRequest(gameId, 0, 1)).exchange()
			.expectStatus().isOk
			.expectBody().jsonPath("$.field").isArray
	}
}