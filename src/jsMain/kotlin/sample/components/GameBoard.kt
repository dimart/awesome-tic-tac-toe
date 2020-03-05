package sample.components

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import react.*
import sample.model.Board
import styled.*


interface GameBoardProps: RProps {
    var board: Board
    var onBoardCellClick: (row: Int, col: Int) -> Unit
}

class GameBoard: RComponent<GameBoardProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +"mdc-layout-grid__cell--span-9"
            }

            /**
             * NOTE: The usage of `table` is debatable, and often `div` is preferable
             * for performance, adaptivity and SEO reasons.
             * I'm gonna use Table for now, due to lack of time and its small size.
             */
            styledTable {
                css {
                    borderCollapse = BorderCollapse.collapse
                }
                for ((i, row) in props.board.field.withIndex()) {
                    styledTr {
                        for ((j, mark) in row.withIndex()) {
                            styledTd {
                                // field position
                                css {
                                    border = "1px solid"
                                    width = 45.px
                                    height = 45.px
                                    textAlign = TextAlign.center
                                    verticalAlign = VerticalAlign.middle
                                    cursor = Cursor.pointer
                                    fontSize = LinearDimension.fillAvailable
                                }
                                attrs {
                                    onClickFunction = {
                                        props.onBoardCellClick(i, j)
                                    }
                                }
                                +mark
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.gameBoard(handler: GameBoardProps.() -> Unit): ReactElement {
    return child(GameBoard::class) {
        this.attrs(handler)
    }
}
