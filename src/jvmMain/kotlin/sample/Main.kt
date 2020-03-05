package sample

import io.github.serpro69.kfaker.Faker
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import sample.api.ClientMessage
import sample.model.Player
import sample.model.UnixTime
import kotlin.time.ExperimentalTime


/**
 * TicTacToe Ktor module
 */
@ExperimentalTime
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@OptIn(UnstableDefault::class)
@ExperimentalCoroutinesApi
@InternalAPI
fun Application.main() {
    /**
     * Configure using application.conf
     */
    val config = environment.config
    val mode = config.config("service")
                     .property("environment").getString()
    log.info("environment: $mode")
    val production = mode == "production"

    // to manage web socket connections
    val playerSessionManager = PlayerSessionManager()
    // to generate funny player's names
    val faker = Faker()

    /**
     * Install features
     */
    if (!production) {
        install(CallLogging)
    }
    install(DefaultHeaders)
    install(Compression)
    install(AutoHeadResponse)
    install(StatusPages)
    install(WebSockets) {
        pingPeriodMillis = 0
    }

    /**
     * Routing
     */
    routing {
        // if there are too many routes here,
        // consider extracting them into separate files
        // by defining an extension function on `Route`
        get("/") {
            call.respondRedirect("static/index.html")
        }

        webSocket("/ws") {
            log.debug("new WebSocket connection: $this")

            // save the player -> [web sockets] list
            val player = Player(
                id = generateNonce(),
                name = faker.hitchhikersGuideToTheGalaxy.characters()
            )
            playerSessionManager.playerJoin(player, this)

            // handle WebSocket messages
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val command = frame.readText()
                        log.debug("[$player] received command: $command")

                        try {
                            val message = Json.parse<ClientMessage>(
                                ClientMessage.serializer(),
                                command
                            )
                            playerSessionManager.playerRequests(player, message)
                        } catch (e: JsonDecodingException) {
                            log.warn("Could not parse incoming ws frame: ${e.message}")
                        }
                    }
                }
            } catch (e: Throwable) {
                log.error("$e")
            } finally {
                log.debug("Closed WebSocket $this for $player")
                playerSessionManager.playerLeft(player)
            }
        }

        static("/static") {
            resource("index.html")
            resource("AwesomeTicTacToe.js")
            resource("AwesomeTicTacToe.js.map")
        }
    }
}


/**
 * Utils
 */

fun currentUnixTime(): UnixTime {
    return UnixTime(System.currentTimeMillis() / 1000)
}
