package sample.api

import kotlinx.serialization.Serializable
import sample.BOARD_COLS
import sample.BOARD_ROWS
import sample.GameId

/**
 * Player initiated requests.
 */
@Serializable
sealed class ClientMessage {
    @Serializable
    data class StartGame(val rows: Int = BOARD_ROWS, val cols: Int = BOARD_COLS) : ClientMessage()

    @Serializable
    data class EnterGame(val gameId: GameId) : ClientMessage()

    @Serializable
    data class LeaveGame(val gameId: GameId) : ClientMessage()

    @Serializable
    data class Move(
        val gameId: GameId,
        val row: Int,
        val col: Int
    ) : ClientMessage()
}
