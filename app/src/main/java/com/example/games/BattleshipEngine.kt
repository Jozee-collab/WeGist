package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import kotlin.random.Random

/**
 * Pure game logic for in-chat Battleship.
 *
 * gameData format: "<playerShots>,|<aiShips>,|<aiShots>,|<playerShips>,"
 * Each segment is a comma-separated list of cell states (".", "H", "M")
 * or ship cell indices respectively.
 */
object BattleshipEngine {

    fun applyPlayerMove(gameData: String?, gameStatus: String?, cellIdx: Int): GameUpdate? {
        if (gameStatus == GameStatuses.COMPLETED) return null

        val parts = gameData?.split("|")?.toMutableList() ?: return null
        if (parts.size < 4) return null

        val playerShots = parts[0].split(",").toMutableList()
        val aiShips = parts[1].split(",")
        val playerShips = parts[3].split(",")

        if (cellIdx !in playerShots.indices) return null
        if (playerShots[cellIdx] != ".") return null // Already shot here

        val isHit = aiShips.contains(cellIdx.toString())
        playerShots[cellIdx] = if (isHit) "H" else "M"

        val totalPlayerHits = playerShots.count { it == "H" }
        val isUserWin = totalPlayerHits >= aiShips.size

        parts[0] = playerShots.joinToString(",")

        val status = if (isUserWin) GameStatuses.COMPLETED else GameStatuses.ACTIVE
        val winner = if (isUserWin) Winners.ME else null

        val resultText = if (isUserWin) {
            "I sank all AI ships and won the Battleship battle! 🎉 (+150 🪙)"
        } else if (isHit) {
            "Hit! I found a ship at sector $cellIdx! 💥 AI's turn..."
        } else {
            "Miss at sector $cellIdx. 💨 AI's turn..."
        }

        return GameUpdate(parts.joinToString("|"), status, winner, resultText)
    }

    fun applyAiMove(gameData: String?, random: Random = Random.Default): GameUpdate? {
        val parts = gameData?.split("|")?.toMutableList() ?: return null
        if (parts.size < 4) return null

        val aiShots = parts[2].split(",").toMutableList()
        val playerShips = parts[3].split(",")

        val availableTargetIndices = aiShots.indices.filter { aiShots[it] == "." }
        if (availableTargetIndices.isNotEmpty()) {
            val target = availableTargetIndices.random(random)
            val isHit = playerShips.contains(target.toString())
            aiShots[target] = if (isHit) "H" else "M"

            val totalAiHits = aiShots.count { it == "H" }
            val isAiWin = totalAiHits >= playerShips.size

            parts[2] = aiShots.joinToString(",")

            val status = if (isAiWin) GameStatuses.COMPLETED else GameStatuses.ACTIVE
            val winner = if (isAiWin) Winners.OPPONENT else null

            val resultText = if (isAiWin) {
                "Alice sank all your ships! 🤖 You lost!"
            } else if (isHit) {
                "Alice launched an attack and HIT your ship at sector $target! 🔥 Your turn!"
            } else {
                "Alice missed her attack at sector $target. 💨 Your turn!"
            }

            return GameUpdate(parts.joinToString("|"), status, winner, resultText)
        }
        return null
    }
}
