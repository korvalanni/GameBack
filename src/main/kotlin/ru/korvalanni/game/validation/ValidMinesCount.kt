package ru.korvalanni.game.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [MinesCountValidator::class])
annotation class ValidMinesCount(
    val message: String = "Количество мин должно быть меньше чем количество ячеек на поле (width * height)",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
