package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Connect Four on a 5x5 board.
 *
 * Board format (gameData): 25 comma-separated cells, index = row * 5 + col,
 * "R" = player (red), "Y" = AI (yellow), "" = empty.
 */
object ConnectFourEngine {

    fun applyPlayerMove(gameData: String?, gameStatus: String?, col: Int): GameUpdate? {
        val board = gameData?.split(",")?.toMutableList() ?: MutableList(25) { "" }
        if (gameStatus == GameStatuses.COMPLETED) return null
        if (col !in 0..4) return null

        // Find lowest empty slot in this column (row goes from 4 down to 0)
        var targetRow = -1
        for (r in 4 downTo 0) {
            val idx = r * 5 + col
            if (board[idx].isEmpty()) {
                targetRow = r
                break
            }
        }
        if (targetRow == -1) return null

        val targetIdx = targetRow * 5 + col
        board[targetIdx] = "R" // Red (Me)

        val isUserWin = checkWin(board, "R")
        val isDraw = !board.contains("") && !isUserWin

        val status = if (isUserWin || isDraw) GameStatuses.COMPLETED else GameStatuses.ACTIVE
        val winner = if (isUserWin) Winners.ME else if (isDraw) Winners.DRAW else null
        val content = when {
            isUserWin -> "I connected four in a row! 🎉"
            isDraw -> "The Connect Four game is a draw!"
            else -> "I dropped a chip in col ${col + 1}. Your turn!"
        }

        return GameUpdate(board.joinToString(","), status, winner, content)
    }

    fun applyAiMove(gameData: String?, random: Random = Random.Default): GameUpdate? {
        val board = gameData?.split(",")?.toMutableList() ?: return null

        val availableCols = (0..4).filter { col -> (0..4).any { r -> board[r * 5 + col].isEmpty() } }
        if (availableCols.isNotEmpty()) {
            val aiCol = availableCols.random(random)
            var targetRow = -1
            for (r in 4 downTo 0) {
                val idx = r * 5 + aiCol
                if (board[idx].isEmpty()) {
                    targetRow = r
                    break
                }
            }
            if (targetRow != -1) {
                val targetIdx = targetRow * 5 + aiCol
                board[targetIdx] = "Y" // Yellow (AI)

                val isAIWin = checkWin(board, "Y")
                val isDraw = !board.contains("") && !isAIWin

                val status = if (isAIWin || isDraw) GameStatuses.COMPLETED else GameStatuses.ACTIVE
                val winner = if (isAIWin) Winners.OPPONENT else if (isDraw) Winners.DRAW else null
                val content = when {
                    isAIWin -> "Alice connected four in a row! 🤖"
                    isDraw -> "The Connect Four game is a draw!"
                    else -> "I made my move! Column ${aiCol + 1}."
                }

                return GameUpdate(board.joinToString(","), status, winner, content)
            }
        }
        return null
    }

    fun checkWin(board: List<String>, player: String): Boolean {
        // Horizontal
        for (row in 0..4) {
            for (col in 0..1) {
                val base = row * 5 + col
                if (board[base] == player && board[base + 1] == player && board[base + 2] == player && board[base + 3] == player) return true
            }
        }
        // Vertical
        for (col in 0..4) {
            for (row in 0..1) {
                val base = row * 5 + col
                if (board[base] == player && board[base + 5] == player && board[base + 10] == player && board[base + 15] == player) return true
            }
        }
        // Diagonal down-right
        for (row in 0..1) {
            for (col in 0..1) {
                val base = row * 5 + col
                if (board[base] == player && board[base + 6] == player && board[base + 12] == player && board[base + 18] == player) return true
            }
        }
        // Diagonal down-left
        for (row in 0..1) {
            for (col in 2..4) {
                val base = row * 5 + col
                if (board[base] == player && board[base + 4] == player && board[base + 8] == player && board[base + 12] == player) return true
            }
        }
        return false
    }
}
