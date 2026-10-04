package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Memory Match (16 cards = 8 pairs).
 *
 * gameData format: "<cardsLayout>,|<matchedArray>,|<firstSelection>,|<playerScore>,|<oppScore>,"
 * matchedArray uses "M" for matched cards and "." otherwise; firstSelection is
 * -1 when no card is currently flipped.
 */
object MemoryMatchEngine {

    fun applyPlayerMove(gameData: String?, gameStatus: String?, cardIdx: Int): GameUpdate? {
        if (gameStatus == GameStatuses.COMPLETED) return null

        val parts = gameData?.split("|")?.toMutableList() ?: return null
        if (parts.size < 5) return null

        val cardsLayout = parts[0].split(",")
        val matchedArray = parts[1].split(",").toMutableList()
        var firstSel = parts[2].toIntOrNull() ?: -1
        var playerScore = parts[3].toIntOrNull() ?: 0
        val oppScore = parts[4].toIntOrNull() ?: 0

        if (cardIdx !in matchedArray.indices) return null
        if (matchedArray[cardIdx] == "M" || cardIdx == firstSel) return null

        if (firstSel == -1) {
            // First card selected
            firstSel = cardIdx
            parts[2] = firstSel.toString()

            return GameUpdate(
                gameData = parts.joinToString("|"),
                gameStatus = null,
                gameWinner = null,
                content = "Flipped first card: ${cardsLayout[cardIdx]}! Flip second..."
            )
        }

        // Second card selected
        val firstCardVal = cardsLayout[firstSel]
        val secondCardVal = cardsLayout[cardIdx]

        return if (firstCardVal == secondCardVal) {
            // Match!
            matchedArray[firstSel] = "M"
            matchedArray[cardIdx] = "M"
            playerScore += 1

            val totalMatched = matchedArray.count { it == "M" }
            val isGameOver = totalMatched >= 16
            val status = if (isGameOver) GameStatuses.COMPLETED else GameStatuses.ACTIVE
            val winner = if (isGameOver) {
                if (playerScore > oppScore) Winners.ME else if (oppScore > playerScore) Winners.OPPONENT else Winners.DRAW
            } else null

            parts[1] = matchedArray.joinToString(",")
            parts[2] = "-1"
            parts[3] = playerScore.toString()

            val resultText = if (isGameOver) {
                "Memory Card match game completed! Matches Score: **$playerScore - $oppScore**. ${if (playerScore > oppScore) "I won! 🎉" else "Alice won! 🤖"}"
            } else {
                "Matched! Found a pair of $firstCardVal! Your score: $playerScore. Keep matching! 🧠"
            }

            GameUpdate(parts.joinToString("|"), status, winner, resultText)
        } else {
            // Mismatch! Reset selection and hand over to the AI.
            parts[2] = "-1"

            GameUpdate(
                gameData = parts.joinToString("|"),
                gameStatus = null,
                gameWinner = null,
                content = "Oops! ${cardsLayout[firstSel]} doesn't match $secondCardVal. AI's turn!",
                needsAiFollowUp = true
            )
        }
    }

    fun applyAiMove(gameData: String?, random: Random = Random.Default): GameUpdate? {
        val parts = gameData?.split("|")?.toMutableList() ?: return null
        if (parts.size < 5) return null

        val cardsLayout = parts[0].split(",")
        val matchedArray = parts[1].split(",").toMutableList()
        var oppScore = parts[4].toIntOrNull() ?: 0
        val playerScore = parts[3].toIntOrNull() ?: 0

        val unmatchedIndices = matchedArray.indices.filter { matchedArray[it] == "." }
        if (unmatchedIndices.size < 2) return null

        // AI is smart: 40% chance of finding a pair, otherwise random selection
        var firstIdx = -1
        var secondIdx = -1

        if (random.nextDouble() < 0.4) {
            // Find a match in available cards
            outer@ for (i in unmatchedIndices) {
                for (j in unmatchedIndices) {
                    if (i != j && cardsLayout[i] == cardsLayout[j]) {
                        firstIdx = i
                        secondIdx = j
                        break@outer
                    }
                }
            }
        }

        if (firstIdx == -1) {
            // Pick random
            val shuffled = unmatchedIndices.shuffled(random)
            firstIdx = shuffled[0]
            secondIdx = shuffled[1]
        }

        val firstCardVal = cardsLayout[firstIdx]
        val secondCardVal = cardsLayout[secondIdx]

        return if (firstCardVal == secondCardVal) {
            matchedArray[firstIdx] = "M"
            matchedArray[secondIdx] = "M"
            oppScore += 1
            parts[1] = matchedArray.joinToString(",")
            parts[4] = oppScore.toString()

            val totalMatched = matchedArray.count { it == "M" }
            val isGameOver = totalMatched >= 16
            val status = if (isGameOver) GameStatuses.COMPLETED else GameStatuses.ACTIVE
            val winner = if (isGameOver) {
                if (playerScore > oppScore) Winners.ME else if (oppScore > playerScore) Winners.OPPONENT else Winners.DRAW
            } else null

            val text = if (isGameOver) {
                "Alice matched $firstCardVal! Game completed. Matches: **$playerScore - $oppScore**. ${if (playerScore > oppScore) "I won! 🎉" else "Alice won! 🤖"}"
            } else {
                "Alice matched $firstCardVal and $secondCardVal! Score: $playerScore - $oppScore. Your turn!"
            }

            GameUpdate(parts.joinToString("|"), status, winner, text)
        } else {
            GameUpdate(
                gameData = parts.joinToString("|"),
                gameStatus = null,
                gameWinner = null,
                content = "Alice tried flipping cards at $firstIdx and $secondIdx but they didn't match. Your turn!"
            )
        }
    }
}
