package com.example.games

import com.example.data.GameStatuses
import kotlin.random.Random

/**
 * Pure game logic for the simplified in-chat Chess variant (4x4 board,
 * random AI). Pieces are unicode symbols; white = player, black = AI.
 *
 * Board format (gameData): 16 comma-separated cells, "." = empty.
 */
object ChessEngine {

    private val BLACK_PIECES = listOf("♜", "♞", "♝", "♚")
    private val WHITE_PIECES = listOf("♖", "♘", "♗", "♔")

    fun applyPlayerMove(gameData: String?, gameStatus: String?, fromIdx: Int, toIdx: Int): GameUpdate? {
        val board = gameData?.split(",")?.toMutableList() ?: mutableListOf()
        if (board.size != 16 || gameStatus == GameStatuses.COMPLETED) return null
        if (fromIdx !in board.indices || toIdx !in board.indices) return null

        val piece = board[fromIdx]
        board[fromIdx] = "."
        board[toIdx] = piece

        return GameUpdate(
            gameData = board.joinToString(","),
            gameStatus = GameStatuses.ACTIVE,
            gameWinner = null,
            content = "I made a chess move! $piece from square $fromIdx to $toIdx. Your turn!",
            needsAiFollowUp = true
        )
    }

    fun applyAiMove(gameData: String?, random: Random = Random.Default): GameUpdate? {
        val board = gameData?.split(",")?.toMutableList() ?: return null

        val blackIndices = board.indices.filter { board[it] in BLACK_PIECES }
        if (blackIndices.isNotEmpty()) {
            val fromIdx = blackIndices.random(random)
            val piece = board[fromIdx]
            val emptyOrWhiteIndices = board.indices.filter { board[it] == "." || board[it] in WHITE_PIECES }
            if (emptyOrWhiteIndices.isNotEmpty()) {
                val toIdx = emptyOrWhiteIndices.random(random)
                val captured = board[toIdx]
                board[fromIdx] = "."
                board[toIdx] = piece

                val text = if (captured != ".") {
                    "Alice captured your $captured with $piece at $toIdx! ⚔️ Your turn!"
                } else {
                    "Alice moved her chess piece $piece to square $toIdx. Your turn!"
                }

                return GameUpdate(board.joinToString(","), null, null, text)
            }
        }
        return null
    }
}
