package sample.components

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket
import react.*
import sample.MAX_PLAYERS_PER_GAME
import sample.api.ClientMessage
import sample.fromNow
import sample.model.GameStatus
import styled.*


interface GameStatusListProps: RProps {
    var gameStatuses: Set<GameStatus>
    var webSocket: WebSocket
}

class GameStatusList: RComponent<GameStatusListProps, RState>() {
    override fun RBuilder.render() {
        val filtered = props.gameStatuses
            .filter { it.numPlayers != 0 }
            .sortedBy { it.creationTime }

        for (gameStatus in filtered) {
            styledDiv {
                css {
                    +"mdc-layout-grid__cell--span-12 mdc-card"
                }

                // Card's Info
                styledDiv {
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.row
                    }

                    // Left Image
                    styledDiv {
                        css {
                            width = 110.px
                            backgroundColor = Color.wheat
                            borderTopLeftRadius = 4.px
                            +"mdc-card__media mdc-card__media--square"
                        }
                    }

                    // Status Block
                    styledDiv {
                        css {
                            padding = "1rem"
                        }
                        styledH2 {
                            css {
                                margin = "0"
                                +"mdc-typography mdc-typography--headline6"
                            }
                            + "Game Title"
                        }
                        styledH3 {
                            css {
                                margin = "0"
                                +"mdc-typography mdc-typography--subtitle2"
                            }
                            + "${gameStatus.numPlayers} / $MAX_PLAYERS_PER_GAME player(s)"
                        }
                        styledH3 {
                            css {
                                margin = "0"
                                +"mdc-typography mdc-typography--subtitle2"
                            }
                            + "Last move: ${gameStatus.lastMoveTime?.fromNow() ?: "never"}"
                        }
                    }
                }

                // Card's Buttons
                styledDiv {
                    css {
                        flexDirection = FlexDirection.rowReverse
                        +"mdc-card__actions mdc-card__action-buttons"
                    }
                    styledButton {
                        css {
                            +"mdc-button mdc-card__action mdc-card__action--button "
                        }
                        attrs {
                            disabled = gameStatus.numPlayers == MAX_PLAYERS_PER_GAME
                            onClickFunction = {
                                props.webSocket.send(
                                    Json.stringify(
                                        ClientMessage.serializer(),
                                        ClientMessage.EnterGame(gameStatus.gameId)
                                    )
                                )
                            }
                        }
                        styledSpan {
                            css {
                                +"mdc-button__ripple"
                            }
                        }
                        +"Join"
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            styledDiv {
                css {
                    +"mdc-layout-grid__cell--span-12"
                }
                +"No games yet. Create one! 😉"
            }
        }
    }
}

fun RBuilder.gameStatusList(handler: GameStatusListProps.() -> Unit): ReactElement {
    return child(GameStatusList::class) {
        this.attrs(handler)
    }
}
