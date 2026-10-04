package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Tic-Tac-Toe.
 *
 * Board format (gameData): 9 comma-separated cells, "X" = player, "O" = AI, "" = empty.
 */
object TicTacToeEngine {

    fun applyPlayerMove(gameData: String?, gameStatus: String?, index: Int): GameUpdate? {
        val board = gameData?.split(",")?.toMutableList() ?: MutableList(9) { "" }
        if (index !in board.indices) return null
        if (board[index].isNotEmpty() || gameStatus == GameStatuses.COMPLETED) return null

        board[index] = "X"

        val isUserWin = checkWin(board, "X")
        val isDraw = !board.contains("") && !isUserWin

        val status = if (isUserWin || isDraw) GameStatuses.COMPLETED else GameStatuses.ACTIVE
        val winner = if (isUserWin) Winners.ME else if (isDraw) Winners.DRAW else null
        val content = when {
            isUserWin -> "I won the Tic-Tac-Toe challenge! 🎉"
            isDraw -> "The Tic-Tac-Toe game is a draw!"
            else -> "Your turn in Tic-Tac-Toe!"
        }

        return GameUpdate(board.joinToString(","), status, winner, content)
    }

    fun applyAiMove(gameData: String?, random: Random = Random.Default): GameUpdate? {
        val board = gameData?.split(",")?.toMutableList() ?: return null
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isNotEmpty()) {
            val aiMove = emptyIndices.random(random)
            board[aiMove] = "O"

            val isAIWin = checkWin(board, "O")
            val isDraw = !board.contains("") && !isAIWin

            val status = if (isAIWin || isDraw) GameStatuses.COMPLETED else GameStatuses.ACTIVE
            val winner = if (isAIWin) Winners.OPPONENT else if (isDraw) Winners.DRAW else null
            val content = when {
                isAIWin -> "Alice won the Tic-Tac-Toe challenge! 🤖"
                isDraw -> "The Tic-Tac-Toe game is a draw!"
                else -> "I made my move! Your turn!"
            }

            return GameUpdate(board.joinToString(","), status, winner, content)
        }
        return null
    }

    fun checkWin(board: List<String>, player: String): Boolean {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )
        return winPatterns.any { pattern ->
            pattern.all { board[it] == player }
        }
    }
}
