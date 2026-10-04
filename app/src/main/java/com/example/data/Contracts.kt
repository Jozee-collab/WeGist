package com.example.data

/**
 * Typed string constants for fields that are persisted as raw strings in Room.
 *
 * The database schema stores these columns as TEXT (no TypeConverters), so these
 * constants are the single source of truth for the magic values used across the
 * app. Screens may still compare against the literal values; new code should
 * use these constants instead.
 */
object ChatTypes {
    const val TEXT = "TEXT"
    const val VOICE = "VOICE"
    const val STICKER = "STICKER"
    const val MEDIA = "MEDIA"
    const val FILE = "FILE"
    const val GAME_INVITE = "GAME_INVITE"
    const val COINS_TRANSFER = "COINS_TRANSFER"
}

object GameTypes {
    const val TIC_TAC_TOE = "TIC_TAC_TOE"
    const val ROCK_PAPER_SCI = "ROCK_PAPER_SCI"
    const val TRIVIA = "TRIVIA"
    const val CHESS = "CHESS"
    const val CONNECT_FOUR = "CONNECT_FOUR"
    const val BATTLESHIP = "BATTLESHIP"
    const val DICE_DUEL = "DICE_DUEL"
    const val MEMORY_MATCH = "MEMORY_MATCH"
    const val NUMBER_GUESSER = "NUMBER_GUESSER"
    const val BLACKJACK = "BLACKJACK"
    const val CUSTOM_LINK = "CUSTOM_LINK"
}

object GameStatuses {
    const val PENDING = "PENDING"
    const val ACTIVE = "ACTIVE"
    const val COMPLETED = "COMPLETED"
    const val REJECTED = "REJECTED"
}

object Winners {
    const val ME = "me"
    const val OPPONENT = "opponent"
    const val DRAW = "DRAW"
}

object Presence {
    const val ONLINE = "online"
    const val IN_GAME = "in-game"
    const val OFFLINE = "offline"
}
