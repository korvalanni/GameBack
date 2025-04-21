package ru.korvalanni.game.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object GameLogic {

    private val mapper = jacksonObjectMapper()

    private const val MINE = "M"
    private const val BLANK = " "
    private const val REVEALED_MINE = "X"

    fun parseField(serialized: String): List<List<String>> =
        mapper.readValue<List<List<String>>>(serialized)

    /**
     * Обрабатывает ход игрока.
     *
     * @param playerField Текущее видимое игровое поле (может быть модифицировано).
     * @param hiddenField Скрытое поле с минами.
     * @param row         Индекс строки выбранной ячейки.
     * @param col         Индекс колонки выбранной ячейки.
     * @return            Пара: обновленное видимое поле и флаг, указывающий на завершение игры.
     */
    fun handleMove(
        playerField: MutableList<MutableList<String>>,
        hiddenField: List<List<String>>,
        row: Int,
        col: Int,
    ): Pair<List<List<String>>, Boolean> {

        // Проверка на мину в скрытом поле
        if (hiddenField[row][col] == MINE) {
            playerField[row][col] = REVEALED_MINE // Отмечаем кликнутую мину
            revealEntireField(playerField, hiddenField, REVEALED_MINE) // Показываем все остальное поле для проигрыша
            return playerField to true // Игра завершена (проигрыш)
        }

        openCell(playerField, hiddenField, row, col)

        val gameWon = checkWinCondition(playerField, hiddenField)
        if (gameWon) {
            revealEntireField(playerField, hiddenField, MINE) // Показываем все остальное поле для выигрыша
            return playerField to true // Игра завершена (выигрыш)
        }

        return playerField to false // Игра продолжается
    }

    /**
     * Рекурсивно открывает ячейку и, если она пустая (0 мин вокруг), соседние ячейки.
     *
     * @param playerField Видимое поле (модифицируется).
     * @param hiddenField Скрытое поле.
     * @param row         Индекс строки для открытия.
     * @param col         Индекс колонки для открытия.
     */
    private fun openCell(
        playerField: MutableList<MutableList<String>>,
        hiddenField: List<List<String>>,
        row: Int,
        col: Int,
    ) {
        if (!isInBounds(playerField, row, col) || playerField[row][col] != BLANK) {
            return
        }

        val count = countAdjacentMines(hiddenField, row, col)
        playerField[row][col] = count.toString()

        if (count == 0) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr != 0 || dc != 0) {
                        openCell(playerField, hiddenField, row + dr, col + dc)
                    }
                }
            }
        }
    }

    /**
     * Открывает все неоткрытые ячейки на поле при завершении игры.
     * Безопасные ячейки показывают количество соседних мин.
     * Ячейки с минами помечаются заданным символом.
     *
     * @param playerField Видимое поле (модифицируется).
     * @param hiddenField Скрытое поле.
     * @param mineSymbol  Символ для отображения мин ('X' при проигрыше, 'M' при выигрыше).
     */
    private fun revealEntireField(
        playerField: MutableList<MutableList<String>>,
        hiddenField: List<List<String>>,
        mineSymbol: String,
    ) {
        for (r in hiddenField.indices) {
            for (c in hiddenField[0].indices) {
                if (playerField[r][c] == BLANK) {
                    if (hiddenField[r][c] == MINE) {
                        playerField[r][c] = mineSymbol
                    } else {
                        val count = countAdjacentMines(hiddenField, r, c)
                        playerField[r][c] = count.toString()
                    }
                }
            }
        }
    }

    /**
     * Подсчитывает количество мин в соседних ячейках.
     *
     * @param hiddenField Скрытое поле.
     * @param row         Индекс строки центральной ячейки.
     * @param col         Индекс колонки центральной ячейки.
     * @return            Количество мин в 8 соседних ячейках.
     */
    private fun countAdjacentMines(hiddenField: List<List<String>>, row: Int, col: Int): Int {
        var count = 0
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = row + dr
                val c = col + dc
                if (isInBounds(hiddenField, r, c) && hiddenField[r][c] == MINE) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * Проверяет условие выигрыша.
     * Выигрыш наступает, если все ячейки, не содержащие мин, открыты.
     *
     * @param playerField Видимое поле.
     * @param hiddenField Скрытое поле.
     * @return `true`, если условие выигрыша выполнено, иначе `false`.
     */
    private fun checkWinCondition(
        playerField: List<List<String>>,
        hiddenField: List<List<String>>,
    ): Boolean {
        val height = hiddenField.size
        val width = hiddenField[0].size
        for (r in 0 until height) {
            for (c in 0 until width) {
                // Если ячейка не мина И она все еще закрыта на видимом поле -> не победа
                if (hiddenField[r][c] != MINE && playerField[r][c] == BLANK) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Проверяет, находятся ли координаты в пределах поля.
     *
     * @param field Игровое поле (любое).
     * @param row   Проверяемый индекс строки.
     * @param col   Проверяемый индекс колонки.
     * @return `true`, если координаты в пределах поля, иначе `false`.
     */
    private fun isInBounds(field: List<List<String>>, row: Int, col: Int): Boolean =
        row in field.indices && col in field[0].indices
}
