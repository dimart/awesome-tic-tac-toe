package sample.model

import kotlinx.serialization.Serializable
import sample.Emoji
import sample.GameId
import sample.PlayerId

/**
 * Player Model
 */

@Serializable
data class Player(
    val id: PlayerId,
    val name: String = "",
    var currentGameId: GameId = "",
    var emoji: Emoji? = null
)
