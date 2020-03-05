package sample.extensions

import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import io.ktor.util.InternalAPI
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import sample.api.ServerMessage
import sample.model.Game
import sample.model.GameStatus

/**
 * WebSocketSession extensions
 * TODO: extract (send -> Json.stringify) for re-use
 */

@InternalAPI
@OptIn(UnstableDefault::class)
suspend fun WebSocketSession.sendAddGamesServerMessage(
    gameStatus: Set<GameStatus>
) {
    send(
        Json.stringify(
            ServerMessage.serializer(),
            ServerMessage.AddGames(
                gameStatus
            )
        )
    )
}

@InternalAPI
@OptIn(UnstableDefault::class)
suspend fun WebSocketSession.sendParticipateInGameServerMessage(game: Game) {
    send(
        Json.stringify(
            ServerMessage.serializer(),
            ServerMessage.ParticipateInGame(
                game
            )
        )
    )
}

@InternalAPI
@OptIn(UnstableDefault::class)
suspend fun WebSocketSession.sendUpdatePlayersServerMessage(game: Game) {
    send(
        Json.stringify(
            ServerMessage.serializer(),
            ServerMessage.UpdatePlayers(
                game.gameId,
                game.players
            )
        )
    )
}


