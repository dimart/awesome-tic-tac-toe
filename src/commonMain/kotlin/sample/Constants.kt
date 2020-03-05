package sample

typealias Emoji = String
typealias GameId = String
typealias PlayerId = String

const val BOARD_ROWS = 10
const val BOARD_COLS = 10

// use yield for random Names? Maybe also by lazy?
val ALLOWED_EMOJIS: List<Emoji> = listOf(
    "👾", "🤖",
    "🌚", "🌝",
    "🧸", "🦔",
    "🍔", "🍏",
    "🧶", "💩"
).shuffled()

val MAX_PLAYERS_PER_GAME = ALLOWED_EMOJIS.size
const val EMOJIS_TO_WIN = 5
