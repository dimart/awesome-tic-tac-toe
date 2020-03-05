package sample.components

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import styled.*


interface FAButtonProps: RProps {
    var title: String
    var onClick: (Event) -> Unit
    var icon: String
}

class FAButton: RComponent<FAButtonProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                position = Position.fixed
                bottom = 1.rem
                right = 1.rem
            }
            styledButton {
                css {
                    cursor = Cursor.pointer
                    +"mdc-fab mdc-fab--extended mdc-ripple-upgraded"
                }
                attrs {
                    onClickFunction = props.onClick
                }
                styledDiv {
                    css {
                        +"mdc-fab__ripple"
                    }
                }
                styledI {
                    css {
                        +"mdc-fab__icon material-icons"
                    }
                    +props.icon
                }
                styledSpan {
                    css {
                        +"mdc-fab__label"
                    }
                    +props.title
                }
            }
        }
    }
}

fun RBuilder.FAButton(handler: FAButtonProps.() -> Unit): ReactElement {
    return child(FAButton::class) {
        this.attrs(handler)
    }
}


