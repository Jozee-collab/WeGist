package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners

/**
 * Pure game logic for in-chat Trivia.
 *
 * gameData format after answering: "<selectedOptionIdx>,<correctIdx>"
 */
object TriviaEngine {

    data class TriviaQuestion(
        val question: String,
        val options: List<String>,
        val correctIdx: Int
    )

    fun questionFor(messageId: Int): TriviaQuestion {
        val qIdx = messageId % 3
        return when (qIdx) {
            0 -> TriviaQuestion(
                "Which video game is officially the best-selling of all time?",
                listOf("Tetris", "Grand Theft Auto V", "Minecraft"),
                2
            )
            1 -> TriviaQuestion(
                "In what year was the original Nintendo Entertainment System (NES) released?",
                listOf("1983", "1985", "1989"),
                1
            )
            else -> TriviaQuestion(
                "What is the name of the main protagonist in the Halo franchise?",
                listOf("Doomguy", "Master Chief", "Marcus Fenix"),
                1
            )
        }
    }

    fun answer(messageId: Int, gameStatus: String?, selectedOptionIdx: Int): GameUpdate? {
        if (gameStatus == GameStatuses.COMPLETED) return null

        val (question, options, correctIdx) = questionFor(messageId)

        val isCorrect = selectedOptionIdx == correctIdx
        val chosenOptionText = options.getOrElse(selectedOptionIdx) { "Unknown" }
        val correctOptionText = options.getOrElse(correctIdx) { "Unknown" }

        val resultText = if (isCorrect) {
            "Solved Trivia! Question: \"$question\"\nMy Answer: **$chosenOptionText** (CORRECT! 🎉 +50 🪙)"
        } else {
            "Failed Trivia! Question: \"$question\"\nMy Answer: **$chosenOptionText** (WRONG! Correct was $correctOptionText 🤖)"
        }

        return GameUpdate(
            gameData = "$selectedOptionIdx,$correctIdx",
            gameStatus = GameStatuses.COMPLETED,
            gameWinner = if (isCorrect) Winners.ME else Winners.OPPONENT,
            content = resultText
        )
    }
}
