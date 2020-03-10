package sample

import io.ktor.config.MapApplicationConfig
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import sample.api.ClientMessage
import sample.api.ServerMessage
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

actual annotation class JsName actual constructor(actual val name: String)

@ExperimentalTime
class TicTacToeServerTest {
    @UnstableDefault
    @ExperimentalCoroutinesApi
    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @OptIn(InternalAPI::class)
    @ExperimentalTime
    @Test
    fun `simulate two players session`() {
        withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("service.environment", "development")
            }
            main()
        }) {
            // First Player Joins
            handleWebSocketConversation("/ws") { incomingFirst, outgoingFirst ->

                incomingFirst.verifyNextMessageIsEmptyAddGames()

                // Second Player Joins
                handleWebSocketConversation("/ws") { incomingSecond, outgoingSecond ->

                    incomingSecond.verifyNextMessageIsEmptyAddGames()

                    /**
                     * 1st Player creates a game
                     */
                    outgoingFirst.send(
                        Frame.Text(
                            Json.stringify(
                                ClientMessage.serializer(),
                                ClientMessage.StartGame(BOARD_ROWS, BOARD_COLS)
                            )
                        )
                    )


                    val participateInGame = incomingFirst.verify(2) {
                        assertTrue(it[0] is ServerMessage.ParticipateInGame)
                        assertTrue(it[1] is ServerMessage.AddGames)
                        assertTrue((it[1] as ServerMessage.AddGames).newGames.size == 1)

                        // only the first player should be in the game at this moment
                        val participateInGame = it[0] as ServerMessage.ParticipateInGame
                        assertTrue(participateInGame.game.players.size == 1)
                        participateInGame
                    } as ServerMessage.ParticipateInGame

                    val addGames = incomingSecond.verify(1) {
                        assertTrue(it[0] is ServerMessage.AddGames)
                        val addGames = it[0] as ServerMessage.AddGames
                        assertTrue(addGames.newGames.size == 1)
                        addGames
                    } as ServerMessage.AddGames

                    assertTrue { addGames.newGames.contains(participateInGame.game.asGameStatus()) }

                    /**
                     * 2nd Player joins the game
                     */
                    outgoingSecond.send(
                        Frame.Text(
                            Json.stringify(
                                ClientMessage.serializer(),
                                ClientMessage.EnterGame(participateInGame.game.gameId)
                            )
                        )
                    )

                    val anotherParticipateInGame = incomingSecond.verify(2) {
                        assertTrue(it[0] is ServerMessage.UpdatePlayers)
                        assertTrue(it[1] is ServerMessage.ParticipateInGame)
                        it[1]
                    } as ServerMessage.ParticipateInGame

                    val firstPlayer = participateInGame.game.players.first()
                    assertTrue(anotherParticipateInGame.game.players.contains(firstPlayer))
                }
            }
        }
    }
}

@UnstableDefault
@OptIn(UnstableDefault::class)
suspend fun ReceiveChannel<Frame>.verify(
    numMessages: Int,
    verifyMessages: (List<ServerMessage>) -> ServerMessage?
): ServerMessage? {
    val messages = mutableListOf<ServerMessage>()
    // Note: perhaps there is a better functional way, like `take` or `flow`
    for (n in 0 until numMessages) {
        val frame = receive() as Frame.Text
        val message = Json.parse(ServerMessage.serializer(), frame.readText())
        messages += message
    }
    return verifyMessages(messages.toList())
}

@OptIn(UnstableDefault::class)
suspend fun ReceiveChannel<Frame>.verifyNextMessageIsEmptyAddGames() {
    verify(1) {
        assertTrue { it.size == 1 }
        assertTrue { it[0] is ServerMessage.AddGames }
        assertTrue { (it[0] as ServerMessage.AddGames).newGames.isEmpty() }
        null
    }
}