package sample.model

import kotlinx.serialization.Serializable
import sample.BOARD_COLS
import sample.BOARD_ROWS
import sample.Emoji


@Serializable
data class Board(val field: MutableList<MutableList<Emoji>>) {
    companion object Factory {
        fun createEmptyBoard(): Board {
            return Board(
                field = MutableList(BOARD_ROWS) {
                    MutableList(BOARD_COLS) { "" }
                }
            )
        }
    }

    private fun isInside(row: Int, col: Int): Boolean {
        return row in 0 until field.size &&
                col in 0 until (field.getOrNull(0)?.size ?: 0)
    }

    fun isInside(pos: Pair<Int, Int>): Boolean {
        return isInside(pos.first, pos.second)
    }

    fun mapInPlace(transform: (Int, Int, Emoji) -> Emoji) {
        for ((row, fieldLine) in field.withIndex()) {
            for ((col, emoji) in fieldLine.withIndex()) {
                field[row][col] = transform(row, col, emoji)
            }
        }
    }

    /**
     * Operators
     */
    operator fun get(row: Int, col: Int): Emoji {
        return field[row][col]
    }

    operator fun get(pos: Pair<Int, Int>): Emoji {
        return field[pos.first][pos.second]
    }

    operator fun set(row: Int, col: Int, emoji: Emoji) {
        field[row][col] = emoji
    }
}
