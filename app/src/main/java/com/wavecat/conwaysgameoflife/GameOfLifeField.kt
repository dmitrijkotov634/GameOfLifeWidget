package com.wavecat.conwaysgameoflife


class GameOfLifeField(private val field: BooleanArray, val width: Int, val height: Int) {

    fun saveToString(): String {
        return field.joinToString(separator = "") {
            if (it) "1" else "0"
        }
    }

    operator fun set(x: Int, y: Int, value: Boolean) {
        field[y * width + x] = value
    }

    operator fun get(x: Int, y: Int): Boolean {
        return field[y * width + x]
    }

    fun next(): GameOfLifeField {
        val field = GameOfLifeField(BooleanArray(width * height), width, height)

        repeat(height) { y ->
            repeat(width) { x ->
                if (get(x, y)) {
                    val near = near(x, y)
                    field[x, y] = near == 2 || near == 3
                } else {
                    field[x, y] = near(x, y) == 3
                }
            }
        }

        return field
    }

    fun fieldEquals(game: GameOfLifeField): Boolean = game.field.contentEquals(field)

    private fun near(
        x: Int, y: Int, coordinates: Array<Pair<Int, Int>> = arrayOf(
            -1 to -1,
            -1 to 0,
            -1 to 1,
            0 to -1,
            0 to 1,
            1 to -1,
            1 to 0,
            1 to 1
        )
    ): Int {
        return coordinates.filter {
            get(Math.floorMod(x + it.first, width), Math.floorMod(y + it.second, height))
        }.size
    }

    companion object {
        fun createFromString(
            string: String,
            width: Int,
            height: Int,
        ): GameOfLifeField {
            val array = string.toCharArray()
            return GameOfLifeField(BooleanArray(width * height) {
                if (array.size > it) array[it] == '1' else false
            }, width, height)
        }
    }
}