package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners

/**
 * Pure game logic for in-chat "Guess the Number".
 *
 * gameData format: "<secret>,<attempts>,<maxAttempts>[,<lastFeedback>]"
 */
object NumberGuessEngine {

    fun guess(gameData: String?, gameStatus: String?, guessValue: Int): GameUpdate? {
        if (gameStatus == GameStatuses.COMPLETED) return null

        val parts = gameData?.split(",") ?: return null
        val secretVal = parts.getOrNull(0)?.toIntOrNull() ?: 25
        var attempts = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val maxAttempts = parts.getOrNull(2)?.toIntOrNull() ?: 6

        attempts += 1
        val isCorrect = guessValue == secretVal
        val isOutOfAttempts = attempts >= maxAttempts

        val feedback = when {
            isCorrect -> "Correct! You guessed it in $attempts attempts!"
            guessValue < secretVal -> "Guess $guessValue is Too Low! 📉"
            else -> "Guess $guessValue is Too High! 📈"
        }

        val status = if (isCorrect || isOutOfAttempts) GameStatuses.COMPLETED else GameStatuses.ACTIVE
        val winner = if (isCorrect) Winners.ME else if (isOutOfAttempts) Winners.OPPONENT else null

        val resultText = if (isCorrect) {
            "I solved the Guess the Number! The secret was **$secretVal**! 🎉 (+100 🪙)"
        } else if (isOutOfAttempts) {
            "Whoops, out of attempts! The correct number was **$secretVal**. Alice wins! 🤖"
        } else {
            "I guessed $guessValue. Feedback: $feedback. ${maxAttempts - attempts} attempts left!"
        }

        return GameUpdate(
            gameData = "$secretVal,$attempts,$maxAttempts,$feedback",
            gameStatus = status,
            gameWinner = winner,
            content = resultText
        )
    }
}
