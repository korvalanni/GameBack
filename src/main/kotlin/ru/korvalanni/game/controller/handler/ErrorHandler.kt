package ru.korvalanni.game.controller.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import ru.korvalanni.game.controller.dto.ServiceErrorDto
import ru.korvalanni.game.controller.handler.error.MinesweeperErrorCode
import ru.korvalanni.game.exception.GameException

@Hidden
@RestControllerAdvice
class ErrorHandler {

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ServiceErrorDto> {
        log.error(ex) { "UNEXPECTED error: ${ex.message}" }
        return ResponseEntity
            .internalServerError()
            .body(toDto(MinesweeperErrorCode.UNKNOWN))
    }

    @ExceptionHandler(GameException::class)
    fun handleGameException(ex: GameException): ResponseEntity<ServiceErrorDto> {
        log.info { "game exception: ${ex.message}" }
        return ResponseEntity
            .status(ex.statusCode)
            .body(ServiceErrorDto(error = ex.message ?: "Unknown error occurred"))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidation(ex: ConstraintViolationException): ResponseEntity<ServiceErrorDto> {
        return ResponseEntity
            .badRequest()
            .body(toDto(MinesweeperErrorCode.VALIDATION_FAILED))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleBindException(ex: WebExchangeBindException): ResponseEntity<ServiceErrorDto> {
        return ResponseEntity
            .badRequest()
            .body(toDto(MinesweeperErrorCode.VALIDATION_FAILED))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ServiceErrorDto> {
        return ResponseEntity
            .badRequest()
            .body(toDto(MinesweeperErrorCode.VALIDATION_FAILED))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidArgument(ex: MethodArgumentNotValidException): ResponseEntity<ServiceErrorDto> {
        return ResponseEntity
            .badRequest()
            .body(toDto(MinesweeperErrorCode.VALIDATION_FAILED))
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleInputError(ex: ServerWebInputException): ResponseEntity<ServiceErrorDto> {
        return ResponseEntity
            .badRequest()
            .body(toDto(MinesweeperErrorCode.VALIDATION_FAILED))
    }

    private companion object {
        val log = KotlinLogging.logger { }
        fun toDto(code: MinesweeperErrorCode): ServiceErrorDto =
            ServiceErrorDto(code.name, code.status, code.message)

    }
}
