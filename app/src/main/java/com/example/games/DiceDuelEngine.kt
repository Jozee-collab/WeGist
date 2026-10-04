package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Dice Duel (best of 3 rounds, 2d6 per roll).
 *
 * gameData format: "<userScore>,<oppScore>,<round>,<lastUserRoll>,<lastOppRoll>"
 */
object DiceDuelEngine {

    fun roll(gameData: String?, gameStatus: String?, random: Random = Random.Default): GameUpdate? {
        if (gameStatus == GameStatuses.COMPLETED) return null

        val dataParts = gameData?.split(",")?.map { it.toIntOrNull() ?: 0 } ?: listOf(0, 0, 0, 0, 0)
        var (userScore, oppScore, round, lastUser, lastOpp) = dataParts

        val userRoll = (1..6).random(random) + (1..6).random(random)
        userScore += userRoll
        round += 1

        // AI rolls right after
        val oppRoll = (1..6).random(random) + (1..6).random(random)
        oppScore += oppRoll

        val isLastRound = round >= 3
        val status = if (isLastRound) GameStatuses.COMPLETED else GameStatuses.ACTIVE
        val winner = if (isLastRound) {
            if (userScore > oppScore) Winners.ME else if (oppScore > userScore) Winners.OPPONENT else Winners.DRAW
        } else null

        val resultText = if (isLastRound) {
            when {
                userScore > oppScore ->
                    "Dice Duel over! My Roll: $userRoll, Alice's Roll: $oppRoll.\nFinal Score: **$userScore - $oppScore**. I WON! 🏆"
                oppScore > userScore ->
                    "Dice Duel over! My Roll: $userRoll, Alice's Roll: $oppRoll.\nFinal Score: **$userScore - $oppScore**. Alice won! 🤖"
                else ->
                    "Dice Duel over! My Roll: $userRoll, Alice's Roll: $oppRoll.\nFinal Score: **$userScore - $oppScore**. It's a DRAW! 🤝"
            }
        } else {
            "Round $round: I rolled $userRoll. Alice rolled $oppRoll.\nRunning Score: $userScore - $oppScore! Your turn to roll again!"
        }

        return GameUpdate(
            gameData = "$userScore,$oppScore,$round,$userRoll,$oppRoll",
            gameStatus = status,
            gameWinner = winner,
            content = resultText
        )
    }
}
