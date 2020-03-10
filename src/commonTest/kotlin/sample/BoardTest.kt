package sample

import sample.model.Board
import kotlin.test.*

expect annotation class JsName constructor(val name: String)

class BoardTest {
    @JsName("checkEmptyBoard")
    @Test
    fun `empty board is created empty`() {
        val board = Board.createEmptyBoard()
        val isEmpty = board.field.fold(true) { acc, row ->
            acc && row.fold(true) { anotherAcc, cell ->
                anotherAcc && cell == ""
            }
        }
        assertTrue(isEmpty)
    }

    @JsName("checkBoardGetSetBasics")
    @Test
    fun `get set basics`() {
        val board = Board.createEmptyBoard()
        val mark = "x"
        expect(mark) {
            board[0, 0] = mark
            board[0, 0]
        }
        assertFails { board[-1, -1] = mark }
    }

    @Test
    fun isInside() {
        val board = Board.createEmptyBoard()
        for (row in 0 until board.field.size) {
            for (col in 0 until board.field[0].size) {
                assertTrue { board.isInside(Pair(row, col)) }
            }
        }
        assertFalse { board.isInside(Pair(-1, -1)) }
        assertFalse { board.isInside(Pair(BOARD_ROWS, BOARD_COLS)) }
        assertFalse { board.isInside(Pair(BOARD_ROWS - 1, BOARD_COLS)) }
        assertFalse { board.isInside(Pair(BOARD_ROWS, BOARD_COLS - 1)) }
    }

    @JsName("mapInPlaceWithEmptyLambda")
    @Test
    fun `mapInPlace - empty body should not change the board`() {
        val board = Board.createEmptyBoard()
        board.mapInPlace { _, _, emoji ->
            emoji
        }
        val isEmpty = board.field.fold(true) { acc, row ->
            acc && row.fold(true) { anotherAcc, cell ->
                anotherAcc && cell == ""
            }
        }
        assertTrue(isEmpty)
    }

    @Test
    fun mapInPlace() {
        val board = Board.createEmptyBoard()
        val mark = "x"
        board.mapInPlace { row, col, emoji ->
            if (row == col) {
                mark
            } else {
                emoji
            }
        }
        for (row in 0 until board.field.size) {
            for (col in 0 until board.field[0].size) {
                if (row == col) {
                    assertTrue { board[row, col] == mark }
                } else {
                    assertTrue { board[row, col] == "" }
                }
            }
        }
    }
}