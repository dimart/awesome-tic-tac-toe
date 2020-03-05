package sample.api

import kotlinx.serialization.Serializable
import sample.GameId
import sample.model.Board
import sample.model.Game
import sample.model.GameStatus
import sample.model.Player

/**
 * Messages pushed to the clients by Server.
 */

@Serializable
sealed class ServerMessage {
    // we don't push the entire game list (except for new clients)
    // instead we push deltas
    @Serializable
    data class AddGames(val newGames: Set<GameStatus>) : ServerMessage()

    @Serializable
    data class ParticipateInGame(val game: Game) : ServerMessage()

    @Serializable
    data class UpdateBoard(
        // when the client is playing, we push board updates for its active game
        val gameId: GameId,
        val row: Int,
        val col: Int,
        val symbol: String
    ) : ServerMessage()

    // "batch" version of UpdateBoard
    @Serializable
    data class ReplaceBoard(
        val gameId: GameId,
        val board: Board
    ) : ServerMessage()

    @Serializable
    data class UpdatePlayers(
        val gameId: GameId,
        val players: Set<Player>
    ) : ServerMessage()

    @Serializable
    data class PlayerWon(
        val game: Game
    ) : ServerMessage()
}
