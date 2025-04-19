package ru.korvalanni.game.controller.handler

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import ru.korvalanni.game.controller.dto.ServiceErrorDto
import ru.korvalanni.game.controller.handler.error.MinesweeperErrorCode
import ru.korvalanni.game.exception.GameNotFoundException

@RestControllerAdvice
class ErrorHandler {
    private val log = LoggerFactory.getLogger(ErrorHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ServiceErrorDto> {
        log.error("UNEXPECTED", ex)
        return ResponseEntity
            .internalServerError()
            .body(toDto(MinesweeperErrorCode.UNKNOWN))
    }

    @ExceptionHandler(GameNotFoundException::class)
    fun handleGameNotFound(ex: GameNotFoundException): ResponseEntity<ServiceErrorDto> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(toDto(MinesweeperErrorCode.GAME_NOT_FOUND))
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

    companion object {
        fun toDto(code: MinesweeperErrorCode): ServiceErrorDto =
            ServiceErrorDto(code.name, code.status, code.message)

    }
}