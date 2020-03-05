package sample

import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import sample.api.ClientMessage
import sample.api.ServerMessage
import sample.model.Game
import sample.model.Player


/**
 * GameSessionActor (GSA) is used to process messages
 * sent by the players during the game.
 * Each GSA is bounded to a game,
 * so that a game's state is confined within the actor.
 */
sealed class GameSessionActorMessage
data class GetGame(val response: CompletableDeferred<Game>) : GameSessionActorMessage()
data class Process(val player: Player, val message: ClientMessage) : GameSessionActorMessage()

typealias BroadcastFunction = suspend (ServerMessage, Set<Player>) -> Unit

@InternalAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun CoroutineScope.gameSessionActor(
    gameId: String,
    player: Player,
    emit: BroadcastFunction
) = actor<GameSessionActorMessage>(capacity = Channel.UNLIMITED) {

    // configure new game
    val game = Game(
        gameId = gameId,
        players = setOf(player),
        creationTime = currentUnixTime()
    )
    // update player with game id
    player.currentGameId = game.gameId
    player.emoji = nextEmoji(game.players)

    // helper function to handle `ClientMessage` in `Process`
    suspend fun handleProcessMessage(processMessage: Process) {
        val (currPlayer, clientMessage) = processMessage

        when (clientMessage) {
            is ClientMessage.EnterGame -> {
                currPlayer.emoji = nextEmoji(game.players)
                currPlayer.currentGameId = game.gameId
                game.addPlayer(currPlayer)
            }
            is ClientMessage.StartGame -> {
                val errorMessage = """
                    GSA was created at StartGame, therefore another StartGame is not expected
                """.trimIndent()
                throw AssertionError(errorMessage)
            }
            is ClientMessage.LeaveGame -> {
                // update model
                game.removePlayer(currPlayer)
                currPlayer.currentGameId = ""
                // notify players to update their list
                emit(
                    ServerMessage.UpdatePlayers(
                        game.gameId,
                        game.players
                    ),
                    game.players
                )
                // ... and their board
                emit(
                    ServerMessage.ReplaceBoard(
                        game.gameId,
                        game.board
                    ),
                    game.players
                )
            }
            is ClientMessage.Move -> {
                if (game.board[clientMessage.row, clientMessage.col] != ""
                    || game.lastMoveByPlayer == currPlayer
                    || game.winner != null) {
                    return
                } else {
                    // update model
                    val row = clientMessage.row
                    val col = clientMessage.col
                    game.board[row, col] = currPlayer.emoji!!
                    game.lastMoveByPlayer = currPlayer
                    game.lastMoveTime = currentUnixTime()

                    // Send updates to the board
                    emit(
                        ServerMessage.UpdateBoard(
                            game.gameId,
                            row, col,
                            game.board[row, col]
                        ),
                        game.players
                    )

                    // check if the player wins! :)
                    if (game.isItWinFor(row, col)) {
                        game.winner = currPlayer
                        emit(ServerMessage.PlayerWon(game), game.players)
                    }
                }
            }
        }
    }

    // iterate over incoming messages
    channel.consumeEach { event ->
        when (event) {
            is Process -> {
                handleProcessMessage(event)
            }
            is GetGame -> {
                event.response.complete(game)
            }
        }
    }
}


/**
 * Utils
 */

fun nextEmoji(players: Set<Player>): Emoji {
    val takenEmojis = players.map { it.emoji }
    return ALLOWED_EMOJIS.first { !takenEmojis.contains(it) }
}
