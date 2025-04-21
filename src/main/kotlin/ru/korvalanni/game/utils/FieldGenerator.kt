package ru.korvalanni.game.utils

/**
 * Объект для генерации игрового поля "Сапёра".
 */
object FieldGenerator {

    /**
     * Генерирует игровое поле с минами, исключая указанную ячейку.
     * Используется для генерации поля после первого хода игрока.
     *
     * @param width      Ширина поля.
     * @param height     Высота поля.
     * @param minesCount Желаемое количество мин.
     * @param excludeRow Индекс строки ячейки, которую нужно исключить (первый ход).
     * @param excludeCol Индекс колонки ячейки, которую нужно исключить (первый ход).
     * @return           Двумерный список строк, представляющий сгенерированное скрытое поле с минами ('M').
     */
    fun generateField(width: Int, height: Int, minesCount: Int, excludeRow: Int, excludeCol: Int): List<List<String>> {
        val totalCells = width * height
        val excludePos = excludeRow * width + excludeCol

        // Исключаем позицию первого щелчка из возможных позиций для мин
        val possiblePositions = (0 until totalCells).filter { it != excludePos }

        // Проверка, что мы не пытаемся разместить больше мин, чем у нас есть возможных позиций
        val actualMinesCount = minOf(minesCount, possiblePositions.size)

        val flatPositions = possiblePositions.shuffled().take(actualMinesCount)

        val field = MutableList(height) { MutableList(width) { " " } }
        for (pos in flatPositions) {
            val row = pos / width
            val col = pos % width
            field[row][col] = "M"
        }

        return field
    }
}
