package sample.model

import kotlinx.serialization.Serializable
import sample.EMOJIS_TO_WIN
import sample.GameId

/**
 * Game Model
 */

typealias positionUpdate = (Pair<Int, Int>) -> Pair<Int, Int>

@Serializable
data class Game(
    val gameId: GameId,
    val creationTime: UnixTime,
    var players: Set<Player>,
    var board: Board = Board.createEmptyBoard(),
    var winner: Player? = null,
    var lastMoveByPlayer: Player? = null,
    var lastMoveTime: UnixTime? = null
) {

    fun addPlayer(newPlayer: Player) {
        players += newPlayer
    }

    fun removePlayer(playerToRemove: Player) {
        players -= playerToRemove

        // clean the board
        board.mapInPlace { _, _, emoji ->
            if (emoji == playerToRemove.emoji) {
                ""
            } else {
                emoji
            }
        }
    }

    private fun countCellsAlongDirection(row: Int, col: Int, nextPosition: positionUpdate): Int {
        var currPosition = Pair(row, col)
        val start = board[currPosition]
        var sameSymbolsCount = 0
        while (board.isInside(currPosition) && start == board[currPosition]) {
            currPosition = nextPosition(currPosition)
            sameSymbolsCount += 1
        }
        return sameSymbolsCount - 1
    }

    fun isItWinFor(row: Int, col: Int): Boolean {
        if (board[row, col] == "") {
            return false
        }

        val directions = listOf(
            listOf(Pair(-1, 0), Pair(1, 0)),  // checks -
            listOf(Pair(0, 1), Pair(0, -1)),  // checks |
            listOf(Pair(1, 1), Pair(-1, -1)), // checks \
            listOf(Pair(-1, 1), Pair(1, -1))  // checks /
        )
        for (direction in directions) {
            var sameCellsCount = 1 // the cell at the passed (row, col)
            for ((rowStep, colStep) in direction) {
                sameCellsCount += countCellsAlongDirection(row, col) {(row, col) ->
                    Pair(row + rowStep, col + colStep)
                }
            }
            if (sameCellsCount >= EMOJIS_TO_WIN) {
                return true
            }
        }

        return false
    }

    fun asGameStatus(): GameStatus {
        return GameStatus(
            gameId = gameId,
            numPlayers = players.size,
            creationTime = creationTime,
            lastMoveTime = lastMoveTime
        )
    }
}


@Serializable
data class GameStatus(
    val gameId: GameId,
    var numPlayers: Int,
    val creationTime: UnixTime,
    val lastMoveTime: UnixTime?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as GameStatus
        if (gameId != other.gameId) return false
        return true
    }
    override fun hashCode() = gameId.hashCode()
}

@Serializable
data class UnixTime(val timestamp: Long) : Comparable<UnixTime> {
    override fun compareTo(other: UnixTime): Int {
        return other.timestamp.compareTo(this.timestamp)
    }
}
