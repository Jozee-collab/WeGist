package com.example.games

/**
 * The result of applying a move (player or AI) to an in-chat game.
 *
 * A [null] [gameStatus] or [gameWinner] means "leave the message's current
 * value unchanged"; the ViewModel merges this into the persisted ChatMessage.
 * [needsAiFollowUp] tells the ViewModel to schedule the AI counter-move.
 */
data class GameUpdate(
    val gameData: String,
    val gameStatus: String?,
    val gameWinner: String?,
    val content: String,
    val needsAiFollowUp: Boolean = false
)
