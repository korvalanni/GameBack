package ru.korvalanni.game

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractContainerTest {


    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15").apply {
            withDatabaseName("game")
            withUsername("user")
            withPassword("password")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/game" }
            registry.add("spring.r2dbc.username") { "user" }
            registry.add("spring.r2dbc.password") { "password" }
            registry.add("spring.liquibase.url") { "jdbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/game" }
        }
    }
}
