package ru.korvalanni.game.utils

import java.security.SecureRandom

object FieldGenerator {
    fun generateField(width: Int, height: Int, minesCount: Int): List<List<String>> {
        require(minesCount < width * height) {
            "Too many mines: $minesCount for field $width x $height"
        }

        val field = MutableList(height) { MutableList(width) { " " } }
        val positions = mutableSetOf<Pair<Int, Int>>()
        val rand = SecureRandom()
        while (positions.size < minesCount) {
            val r = rand.nextInt(height)
            val c = rand.nextInt(width)
            positions += r to c
        }
        for ((r, c) in positions) {
            field[r][c] = "M"
        }
        return field
    }
}