package sample.contrib

@JsModule("moment")
external interface Moment {
    fun fromNow(withoutSuffix: Boolean? = definedExternally): String
}
@JsModule("moment")
external fun moment(
    input: Any? = definedExternally,
    token: Any? = definedExternally
): Moment
