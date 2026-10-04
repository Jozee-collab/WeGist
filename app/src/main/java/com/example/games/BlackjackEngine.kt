package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Blackjack against the house (Alice).
 *
 * gameData format: "<playerHand>,|<dealerHand>,"
 * Hands are comma-separated card values (1..11, where 11 is an Ace).
 */
object BlackjackEngine {

    const val ACTION_HIT = "HIT"
    const val ACTION_STAND = "STAND"

    fun play(gameData: String?, gameStatus: String?, action: String, random: Random = Random.Default): GameUpdate? {
        if (gameStatus == GameStatuses.COMPLETED) return null

        val parts = gameData?.split("|")?.toMutableList() ?: return null
        val playerHand = parts[0].split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()
        val oppHand = parts[1].split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()

        return when (action) {
            ACTION_HIT -> hit(parts, playerHand, random)
            ACTION_STAND -> stand(parts, playerHand, oppHand, random)
            else -> null
        }
    }

    private fun hit(parts: MutableList<String>, playerHand: MutableList<Int>, random: Random): GameUpdate {
        val newCard = (1..11).random(random)
        playerHand.add(newCard)
        val playerScore = score(playerHand)

        parts[0] = playerHand.joinToString(",")

        return if (playerScore > 21) {
            // Bust!
            GameUpdate(
                gameData = parts.joinToString("|"),
                gameStatus = GameStatuses.COMPLETED,
                gameWinner = Winners.OPPONENT,
                content = "Bust! Drew a $newCard, Total Score is $playerScore. Alice wins! 🤖"
            )
        } else {
            GameUpdate(
                gameData = parts.joinToString("|"),
                gameStatus = null,
                gameWinner = null,
                content = "You drew a $newCard. Your Hand: ${playerHand.joinToString(", ")} (Score: $playerScore). Hit or Stand?"
            )
        }
    }

    private fun stand(
        parts: MutableList<String>,
        playerHand: MutableList<Int>,
        oppHand: MutableList<Int>,
        random: Random
    ): GameUpdate {
        // Dealer/Opponent hits until 17+
        var oppScore = score(oppHand)
        while (oppScore < 17) {
            val newCard = (1..11).random(random)
            oppHand.add(newCard)
            oppScore = score(oppHand)
        }

        parts[1] = oppHand.joinToString(",")
        val playerScore = score(playerHand)

        val winner = when {
            oppScore > 21 -> Winners.ME
            playerScore > oppScore -> Winners.ME
            oppScore > playerScore -> Winners.OPPONENT
            else -> Winners.DRAW
        }

        val resultText = when (winner) {
            Winners.ME -> "You Win! Your score: $playerScore, Alice's score: $oppScore ${if (oppScore > 21) "(Busted)" else ""}. 🎉 (+120 🪙)"
            Winners.OPPONENT -> "Alice wins! Your score: $playerScore, Alice's score: $oppScore. 🤖"
            else -> "Dealer/Player Push! Both got $playerScore. 🤝"
        }

        return GameUpdate(
            gameData = parts.joinToString("|"),
            gameStatus = GameStatuses.COMPLETED,
            gameWinner = winner,
            content = resultText
        )
    }

    /** Blackjack score with soft-ace adjustment (11 counts as 1 when busting). */
    fun score(hand: List<Int>): Int {
        var score = hand.sum()
        var aces = hand.count { it == 11 }
        while (score > 21 && aces > 0) {
            score -= 10
            aces -= 1
        }
        return score
    }
}
