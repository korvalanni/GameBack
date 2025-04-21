package ru.korvalanni.game.controller.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.korvalanni.game.controller.dto.NewGameRequest

class MinesCountValidator : ConstraintValidator<ValidMinesCount, NewGameRequest> {
    override fun isValid(
        request: NewGameRequest?,
        context: ConstraintValidatorContext,
    ): Boolean {
        val value = request ?: return true
        return value.minesCount < value.width * value.height
    }
}
