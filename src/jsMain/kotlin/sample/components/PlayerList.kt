package sample.components

import kotlinx.css.em
import kotlinx.css.fontSize
import react.*
import sample.model.Player
import styled.*


interface PlayerListProps: RProps {
    var players: Set<Player>
    var winner: Player?
}

class PlayerList: RComponent<PlayerListProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +"mdc-layout-grid__cell--span-3"
            }
            styledUl {
                css {
                    +"mdc-card mdc-card--outlined mdc-list mdc-list--avatar-list"
                }
                for (player in props.players) {

                    val isWinner = (props.winner != null && player.id == props.winner?.id)
                    val playerStyle = if (isWinner) "mdc-list-item mdc-list-item--activated" else "mdc-list-item"
                    val playerName = if (isWinner) "WINNER!" else player.name

                    styledLi {
                        css {
                            fontSize = 1.em
                            +playerStyle
                        }
                        styledSpan {
                            css {
                                +"mdc-list-item__graphic"
                            }
                            +(player.emoji ?: "😡")
                        }
                        +playerName
                    }
                }
            }
        }
    }
}

fun RBuilder.playerList(handler: PlayerListProps.() -> Unit): ReactElement {
    return child(PlayerList::class) {
        this.attrs(handler)
    }
}
