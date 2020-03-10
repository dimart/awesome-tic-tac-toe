package sample

import sample.model.Game
import sample.model.Player
import sample.model.UnixTime
import kotlin.test.Test
import kotlin.test.assertTrue

class GameTest {
    @JsName("playerRemoval")
    @Test
    fun `removing a player removes player's moves`() {
        val player = Player("1", "A", "", ALLOWED_EMOJIS[0])
        val playerToRemove = Player("2", "Z", "", ALLOWED_EMOJIS[1])
        val game = Game("1", UnixTime(1), setOf())
        game.addPlayer(player)
        game.addPlayer(playerToRemove)

        // simulate some gameplay
        for (row in 0 until BOARD_ROWS) {
            game.board[row, row] = playerToRemove.emoji!!
            game.board[BOARD_ROWS - 1 - row, row] = player.emoji!!
        }

        game.removePlayer(playerToRemove)

        for (row in 0 until BOARD_ROWS) {
            assertTrue { game.board[row, row] == "" }
            assertTrue { game.board[BOARD_ROWS - 1 - row, row] == player.emoji!! }
        }
    }

    @JsName("horizontalWin")
    @Test
    fun `adding horizontal emoji line wins game`() {
        val game = Game("", UnixTime(1), setOf())
        val player = Player("1", "A", "", ALLOWED_EMOJIS[0])

        // try to put the win-emoji-line starting from every possible column
        // on every possible row
        // Note: if the board will be larger, may be testing all combinations is overkill)
        for (startColumn in 0 until BOARD_COLS - EMOJIS_TO_WIN + 1) {
            for (row in 0 until BOARD_ROWS) {
                game.addPlayer(player)
                for (col in startColumn until startColumn + EMOJIS_TO_WIN) {
                    game.board[row, col] = player.emoji!!
                }
                for (col in startColumn until startColumn + EMOJIS_TO_WIN) {
                    assertTrue(game.isItWinFor(row, col))
                }
                game.removePlayer(player)
            }
        }
    }

    @JsName("verticalWin")
    @Test
    fun `adding vertical emoji line wins game`() {
        val game = Game("", UnixTime(1), setOf())
        val player = Player("1", "A", "", ALLOWED_EMOJIS[0])

        for (startRow in 0 until BOARD_ROWS - EMOJIS_TO_WIN + 1) {
            for (col in 0 until BOARD_COLS) {
                game.addPlayer(player)
                for (row in startRow until startRow + EMOJIS_TO_WIN) {
                    game.board[row, col] = player.emoji!!
                }
                for (row in startRow until startRow + EMOJIS_TO_WIN) {
                    assertTrue(game.isItWinFor(row, col))
                }
                game.removePlayer(player)
            }
        }
    }

    @JsName("diagonalWin")
    @Test
    fun `adding diagonal emoji line wins game`() {
        val game = Game("", UnixTime(1), setOf())
        val player = Player("1", "A", "", ALLOWED_EMOJIS[0])

        for (startColumn in 0 until BOARD_COLS - EMOJIS_TO_WIN + 1) {
            for (startRow in 0 until BOARD_ROWS - EMOJIS_TO_WIN + 1) {
                game.addPlayer(player)
                for (i in 0 until EMOJIS_TO_WIN) {
                    game.board[startRow + i, startColumn + i] = player.emoji!!
                }
                for (i in 0 until EMOJIS_TO_WIN) {
                    assertTrue(game.isItWinFor(startRow + i, startColumn + i))
                }
                game.removePlayer(player)
            }
        }
    }

    @JsName("reverseDiagonalWin")
    @Test
    fun `adding reverse diagonal emoji line wins game`() {
        val game = Game("", UnixTime(1), setOf())
        val player = Player("1", "A", "", ALLOWED_EMOJIS[0])

        for (startColumn in 0 until BOARD_COLS - EMOJIS_TO_WIN + 1) {
            for (startRow in 0 until BOARD_ROWS - EMOJIS_TO_WIN + 1) {
                game.addPlayer(player)
                for (i in 0 until EMOJIS_TO_WIN) {
                    val row = startRow + (EMOJIS_TO_WIN - 1) - i
                    val col = startColumn + i
                    game.board[row, col] = player.emoji!!
                }
                for (i in 0 until EMOJIS_TO_WIN) {
                    val row = startRow + (EMOJIS_TO_WIN - 1) - i
                    val col = startColumn + i
                    assertTrue(game.isItWinFor(row, col))
                }
                game.removePlayer(player)
            }
        }
    }
}