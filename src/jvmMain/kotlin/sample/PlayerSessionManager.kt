package sample

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.generateNonce
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import sample.api.ClientMessage
import sample.api.ServerMessage
import sample.extensions.sendAddGamesServerMessage
import sample.extensions.sendParticipateInGameServerMessage
import sample.extensions.sendUpdatePlayersServerMessage
import sample.model.Game
import sample.model.GameStatus
import sample.model.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * A channel of an active `Game` backed by `GameSessionActor`.
 */
typealias GameSession = SendChannel<GameSessionActorMessage>

/**
 * PlayerSessionManager keeps track of Players and their WebSockets.
 * It handles ClientMessages:
 *   - on GameStart: creates GameSessionActor`s
 *   - otherwise: passes it to existing GameSessionActor
 */
class PlayerSessionManager {

    /**
     * Associates a Player (by Id) to its WebSocket.
     * There might be several opened sockets for the same User,
     * since a browser is able to open several tabs.
     * However, we don't distinguish between the tabs intentionally.
     */
    private val player2sockets = ConcurrentHashMap<PlayerId, WebSocketSession>()

    /**
     * Associates an active Game (by Id) to its GameSessionActor.
     */
    private val activeGames = ConcurrentHashMap<GameId, GameSession>()

    /**
     * Handles the case when the Player has just opened the web app
     */
    @InternalAPI
    suspend fun playerJoin(player: Player, socket: WebSocketSession) {
        // save Player <-> WebSocket
        player2sockets[player.id] = socket

        // send active game list to this player immediately
        val activeGamesStatus = collectActiveGamesStatus()
        socket.sendAddGamesServerMessage(activeGamesStatus)
    }

    /**
     * Handles Player's actions
     */
    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @InternalAPI
    @KtorExperimentalAPI
    suspend fun playerRequests(player: Player, message: ClientMessage) {
        when (message) {
            is ClientMessage.StartGame -> {
                player2sockets[player.id]?.let { socket ->
                    // Create GSA for the game
                    val gameId = generateNonce()
                    val actor = socket.gameSessionActor(gameId, player) { serverMessage, players ->
                        broadcastResults(serverMessage, players)
                    }
                    activeGames[gameId] = actor

                    // Request the game object back
                    val game = requestGameFromActor(gameId)

                    // Allow user to play
                    socket.sendParticipateInGameServerMessage(game)

                    // Broadcast the Update
                    broadcastGameStatuses(setOf(game.asGameStatus()))
                }
            }
            is ClientMessage.EnterGame -> {
                activeGames[message.gameId]?.let { gameSessionActor ->
                    gameSessionActor.send(Process(player, message))

                    // update player list
                    val game = requestGameFromActor(message.gameId)
                    game.players.forEach {
                        player2sockets[it.id]?.sendUpdatePlayersServerMessage(game)
                    }

                    // enter game
                    player2sockets[player.id]?.sendParticipateInGameServerMessage(game)
                }
            }
            is ClientMessage.LeaveGame -> {
                val gameId = message.gameId
                activeGames[gameId]?.let { gameSessionActor ->
                    // ask GSA to update the game's model
                    gameSessionActor.send(Process(player, message))

                    // send out new game status to ALL players
                    val game = requestGameFromActor(gameId)
                    val status = setOf(game.asGameStatus())
                    player2sockets.forEach {
                        it.value.sendAddGamesServerMessage(status)
                    }

                    // if game session is empty -> Stop the actor
                    if (game.players.isEmpty()) {
                        activeGames.remove(gameId)?.close()
                    }
                }
            }
            is ClientMessage.Move -> {
                activeGames[message.gameId]?.send(Process(player, message))
                broadcastGameStatuses()
            }
        }
    }

    /**
     * Broadcasts ServerMessage to the Players passed.
     */
    private suspend fun broadcastResults(serverMessage: ServerMessage, players: Set<Player>) {
        players.forEach {
            player2sockets[it.id]?.send(
                Frame.Text(
                    Json.stringify(
                        ServerMessage.serializer(),
                        serverMessage
                    )
                )
            )
        }
    }

    /**
     * Handles the case when the Player closed the browser tab.
     */
    @ObsoleteCoroutinesApi
    @InternalAPI
    suspend fun playerLeft(player: Player) {
        // Remove player's WebSocket since it is terminated anyway
        // Therefore, we'll stop communicating with him/her.
        player2sockets.remove(player.id)

        // Simulate player's request
        playerRequests(player, ClientMessage.LeaveGame(player.currentGameId))
    }

    private suspend fun requestGameFromActor(gameId: GameId): Game {
        val gameReq = CompletableDeferred<Game>()
        activeGames[gameId]?.send(GetGame(gameReq))
        return gameReq.await()
    }

    /**
     * Asks each GameSessionActor about their game status.
     */
    @InternalAPI
    private suspend fun collectActiveGames() = activeGames.map { (_, actor) ->
        val gameReq = CompletableDeferred<Game>()
        actor.send(GetGame(gameReq))
        gameReq.await()
    }

    @InternalAPI
    private suspend fun collectActiveGamesStatus() = collectActiveGames().map {
        GameStatus(it.gameId, it.players.size, it.creationTime, it.lastMoveTime)
    }.toSet()

    @InternalAPI
    private suspend fun broadcastGameStatuses(statuses: Set<GameStatus> = setOf()) {
        if (statuses.isEmpty()) {
            val allStatuses = collectActiveGamesStatus()
            player2sockets.forEach {
                it.value.sendAddGamesServerMessage(allStatuses)
            }
        } else {
            player2sockets.forEach {
                it.value.sendAddGamesServerMessage(statuses)
            }
        }
    }
}