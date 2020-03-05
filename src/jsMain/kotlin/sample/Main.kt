package sample

import kotlinx.css.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket
import react.*
import react.dom.render
import sample.api.ClientMessage
import sample.api.ServerMessage
import sample.components.*
import sample.contrib.moment
import sample.model.Game
import sample.model.GameStatus
import sample.model.UnixTime
import styled.css
import styled.styledDiv
import styled.styledH2
import kotlin.browser.document
import kotlin.browser.window


/**
 * NOTE:
 * Next steps:
 *   - use Kotlin.JS "version" of MaterialUI/ RingUI instead of static CSS
 *   - add React Router to fix back button from game
 *   - UI enhancements (clearer winning message, etc)
 */

data class ServerConfig(val host: String, val port: Int)
val serverConfig = ServerConfig(window.location.hostname, 8080)

fun main() {
    render(document.getElementById("root")) {
        child(App::class) {}
    }
}

interface AppState: RState {
    var webSocket: WebSocket
    var activeGame: Game?
    var games: Set<GameStatus>
}

class App: RComponent<RProps, AppState>() {

    override fun AppState.init() {
        webSocket = createWebSocket()
        activeGame = null
        games = setOf()
    }

    private fun createWebSocket(): WebSocket {
        val protocol = if (window.location.protocol == "http:") "ws:" else "wss:"
        val endpoint = "ws"
        return WebSocket("$protocol//${serverConfig.host}:${serverConfig.port}/$endpoint")
    }

    @OptIn(UnstableDefault::class)
    override fun componentDidMount() {
        state.webSocket.onopen = {
            console.log("connected to $serverConfig")
        }
        state.webSocket.onerror = {
            console.log("Error: $it")
        }
        state.webSocket.onclose = {
            console.log("disconnected from $serverConfig")
        }
        state.webSocket.onmessage = {
            console.log("received ${it.data} from $serverConfig")

            // using ServerMessage sealed class
            if (it.data is String) {
                val message = Json.parse(ServerMessage.serializer(), it.data as String)
                handleServerMessages(message)
            }
        }
    }

    private fun handleServerMessages(message: ServerMessage) {
        when (message) {
            is ServerMessage.AddGames -> {
                setState {
                    games = message.newGames + state.games
                }
            }
            is ServerMessage.ParticipateInGame -> {
                setState {
                    activeGame = message.game
                }
            }
            is ServerMessage.ReplaceBoard -> {
                state.activeGame?.let {
                    setState {
                        activeGame = it.copy(
                            board = message.board
                        )
                    }
                }
            }
            is ServerMessage.UpdateBoard -> {
                state.activeGame?.let {
                    val (_, row, col, newEmoji) = message
                    val updatedBoard = it.board.copy()
                    updatedBoard[row, col] = newEmoji
                    setState {
                        activeGame = it.copy(
                            board = updatedBoard
                        )
                    }
                }
            }
            is ServerMessage.UpdatePlayers -> {
                state.activeGame?.let {
                    setState {
                        activeGame = it.copy(players = message.players)
                    }
                }
            }
            is ServerMessage.PlayerWon -> {
                setState {
                    activeGame = message.game
                }
            }
        }
    }

    @OptIn(UnstableDefault::class)
    override fun RBuilder.render() {

        val isPlaying = state.activeGame != null

        /**
         * App Bar
         */
        appBar {
            title = "👾 Awesome Tic-Tac-Toe 👾"
        }

        /**
         * Content Body
         */
        styledDiv {
            // FIXME: Consider using StyleSheet object mix-in for common css
            css {
                paddingTop = 64.px
                maxWidth = 760.px
                marginTop = 0.px
                marginRight = LinearDimension.auto
                marginBottom = 0.px
                marginLeft = LinearDimension.auto
            }
            styledH2 {
                css {
                    paddingLeft = 24.px
                    +"mdc-typography mdc-typography--headline5"
                }
                +if (isPlaying) "Board" else "Active Games"
            }
            styledDiv {
                css {
                    +"mdc-layout-grid"
                }
                styledDiv {
                    css {
                        +"mdc-layout-grid__inner"
                    }

                    state.activeGame?.let { activeGame ->
                        /**
                         * GameBoard Component
                         */
                        gameBoard {
                            board = activeGame.board
                            onBoardCellClick = {row, col ->
                                if (activeGame.winner == null) {
                                    state.webSocket.send(
                                        Json.stringify(
                                            ClientMessage.serializer(),
                                            ClientMessage.Move(state.activeGame!!.gameId, row, col)
                                        )
                                    )
                                }
                            }
                        }
                        playerList {
                            players = activeGame.players
                            winner = activeGame.winner
                        }
                    } ?: run {
                        /**
                         * GameList Component
                         */
                        gameStatusList {
                            gameStatuses = state.games
                            webSocket = state.webSocket
                        }
                    }
                }
            }

            /**
             * Start Game Button
             */
            state.activeGame?.let {
                FAButton {
                    title = "Leave Game"
                    icon = "remove"
                    onClick = {_ ->
                        state.webSocket.send(
                            Json.stringify(
                                ClientMessage.serializer(),
                                ClientMessage.LeaveGame(it.gameId)
                            )
                        )
                        setState {
                            activeGame = null
                        }
                    }
                }
            } ?: run {
                FAButton {
                    title = "New Game"
                    icon = "add"
                    onClick = {
                        state.webSocket.send(
                            Json.stringify(
                                ClientMessage.serializer(),
                                ClientMessage.StartGame()
                            )
                        )
                    }
                }
            }
        }
    }
}

fun UnixTime.fromNow(): String {
    return moment(timestamp * 1000, "x").fromNow()
}
