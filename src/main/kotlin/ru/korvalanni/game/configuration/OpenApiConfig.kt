package ru.korvalanni.game.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun gameOpenAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Game API")
                .description("API для игры в сапера")
                .version("1.0.0")
                .contact(
                    Contact()
                        .name("korvalanni")
                        .url("https://github.com/korvalanni")
                )
        )
}
