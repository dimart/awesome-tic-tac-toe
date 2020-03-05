package sample.components

import react.*
import styled.*


interface AppBarProps: RProps {
    var title: String
}

class AppBar: RComponent<AppBarProps, RState>() {
    override fun RBuilder.render() {
        styledHeader {
            css {
                +"mdc-top-app-bar"
            }

            styledDiv {
                css {
                    +"mdc-top-app-bar__row"
                }

                styledSection {
                    css {
                        +"mdc-top-app-bar__section mdc-top-app-bar__section--align-start"
                    }

                    styledSpan {
                        css {
                            +"mdc-top-app-bar__title"
                        }
                        +props.title
                    }
                }
            }
        }
    }
}

fun RBuilder.appBar(handler: AppBarProps.() -> Unit): ReactElement {
    return child(AppBar::class) {
        this.attrs(handler)
    }
}
