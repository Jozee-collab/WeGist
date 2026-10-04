package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Rock-Paper-Scissors (single round).
 *
 * gameData format: "<userChoice>,<opponentChoice>"
 */
object RockPaperScissorsEngine {

    val CHOICES = listOf("ROCK", "PAPER", "SCISSORS")

    fun play(userChoice: String, random: Random = Random.Default): GameUpdate {
        val opponentChoice = CHOICES.random(random)

        val winner = when {
            userChoice == opponentChoice -> Winners.DRAW
            (userChoice == "ROCK" && opponentChoice == "SCISSORS") ||
                (userChoice == "PAPER" && opponentChoice == "ROCK") ||
                (userChoice == "SCISSORS" && opponentChoice == "PAPER") -> Winners.ME
            else -> Winners.OPPONENT
        }

        val resultText = when (winner) {
            Winners.ME -> "I won! I chose $userChoice, Opponent chose $opponentChoice. 🎉"
            Winners.OPPONENT -> "Opponent won! I chose $userChoice, Opponent chose $opponentChoice. 🤖"
            else -> "It's a tie! Both chose $userChoice. 🤝"
        }

        return GameUpdate(
            gameData = "$userChoice,$opponentChoice",
            gameStatus = GameStatuses.COMPLETED,
            gameWinner = winner,
            content = resultText
        )
    }
}
