package com.example.games

import com.example.data.GameStatuses
import com.example.data.Winners
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Pure JVM unit tests for the in-chat game engines (no Android dependencies).
 */
class GameEnginesTest {

    // ---------- TicTacToeEngine ----------

    @Test
    fun `tictactoe player move places X and reports active`() {
        val update = TicTacToeEngine.applyPlayerMove(",,,,,,,,", null, 4)
        assertNotNull(update)
        val cells = update!!.gameData.split(",")
        assertEquals("X", cells[4])
        assertEquals(GameStatuses.ACTIVE, update.gameStatus)
        assertNull(update.gameWinner)
    }

    @Test
    fun `tictactoe rejects move on occupied cell and completed game`() {
        assertNull(TicTacToeEngine.applyPlayerMove("X,,,,,,,,", null, 0))
        assertNull(TicTacToeEngine.applyPlayerMove(",,,,,,,,", GameStatuses.COMPLETED, 0))
        assertNull(TicTacToeEngine.applyPlayerMove(",,,,,,,,", null, 9))
    }

    @Test
    fun `tictactoe detects row win for player`() {
        val board = "X,X,X,,,,,O,O"
        assertTrue(TicTacToeEngine.checkWin(board.split(","), "X"))
        assertFalse(TicTacToeEngine.checkWin(board.split(","), "O"))
    }

    @Test
    fun `tictactoe detects diagonal win and full-board player win completes game`() {
        val update = TicTacToeEngine.applyPlayerMove("X,,O,X,O,,X,O,", null, 6)
        assertNotNull(update)
        assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
        assertEquals(Winners.ME, update.gameWinner)
        assertTrue(update.content.contains("won"))
    }

    @Test
    fun `tictactoe draw when board full without winner`() {
        // X has no winning line, O has no winning line, last cell empty
        val board = "X,O,X,X,O,O,O,X," // cell 8 empty; nobody wins by filling it
        val update = TicTacToeEngine.applyPlayerMove(board, GameStatuses.ACTIVE, 8)
        assertNotNull(update)
        assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
        assertEquals(Winners.DRAW, update.gameWinner)
    }

    @Test
    fun `tictactoe ai move fills exactly one empty cell with O`() {
        val update = TicTacToeEngine.applyAiMove("X,,,,,O,,", Random(42))
        assertNotNull(update)
        val cells = update!!.gameData.split(",")
        assertEquals(9, cells.size)
        assertEquals(1, cells.count { it == "O" })
        assertEquals(listOf(0, 6), cells.indices.filter { it != -1 && cells[it] == "X" }.sorted())
    }

    @Test
    fun `tictactoe ai wins when completing a line`() {
        // O at 0 and 1; only cell 2 empty -> AI must place O there and win
        val update = TicTacToeEngine.applyAiMove("O,O,,X,X,,,X,X", Random(1))
        assertNotNull(update)
        assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
        assertEquals(Winners.OPPONENT, update.gameWinner)
    }

    // ---------- RockPaperScissorsEngine ----------

    @Test
    fun `rps always completes and stores both choices`() {
        for (choice in RockPaperScissorsEngine.CHOICES) {
            val update = RockPaperScissorsEngine.play(choice, Random(7))
            assertEquals(GameStatuses.COMPLETED, update.gameStatus)
            val parts = update.gameData.split(",")
            assertEquals(choice, parts[0])
            assertTrue(parts[1] in RockPaperScissorsEngine.CHOICES)
        }
    }

    @Test
    fun `rps winner logic is correct`() {
        listOf(1L, 2L, 3L, 4L, 5L).forEach { seed ->
            for (choice in RockPaperScissorsEngine.CHOICES) {
                val update = RockPaperScissorsEngine.play(choice, Random(seed))
                val (user, opp) = update.gameData.split(",")
                val expected = when {
                    user == opp -> Winners.DRAW
                    (user == "ROCK" && opp == "SCISSORS") ||
                        (user == "PAPER" && opp == "ROCK") ||
                        (user == "SCISSORS" && opp == "PAPER") -> Winners.ME
                    else -> Winners.OPPONENT
                }
                assertEquals("seed=$seed choice=$choice", expected, update.gameWinner)
            }
        }
    }

    // ---------- TriviaEngine ----------

    @Test
    fun `trivia correct answer wins and stores indices`() {
        val q = TriviaEngine.questionFor(0) // Minecraft correct at index 2
        val update = TriviaEngine.answer(0, null, 2)
        assertNotNull(update)
        assertEquals(Winners.ME, update!!.gameWinner)
        assertEquals(GameStatuses.COMPLETED, update.gameStatus)
        assertEquals("2,2", update.gameData)
        assertTrue(update.content.contains("CORRECT"))
    }

    @Test
    fun `trivia wrong answer loses`() {
        val update = TriviaEngine.answer(1, null, 0) // NES question, correct is 1985 (index 1)
        assertNotNull(update)
        assertEquals(Winners.OPPONENT, update!!.gameWinner)
        assertTrue(update.content.contains("WRONG"))
    }

    @Test
    fun `trivia ignores answers on completed game`() {
        assertNull(TriviaEngine.answer(0, GameStatuses.COMPLETED, 2))
    }

    // ---------- ChessEngine ----------

    @Test
    fun `chess player move moves piece and requests ai follow up`() {
        val board = "♖,♘,♗,♔,.,.,.,.,♜,♞,♝,♚,.,.,.,."
        val update = ChessEngine.applyPlayerMove(board, null, 1, 5)
        assertNotNull(update)
        val cells = update!!.gameData.split(",")
        assertEquals(".", cells[1])
        assertEquals("♘", cells[5])
        assertEquals(GameStatuses.ACTIVE, update.gameStatus)
        assertTrue(update.needsAiFollowUp)
    }

    @Test
    fun `chess rejects wrong board size and completed games`() {
        assertNull(ChessEngine.applyPlayerMove(".,.,.", null, 0, 1))
        assertNull(ChessEngine.applyPlayerMove(".,.,.,.,.,.,.,.,.,.,.,.,.,.,.,.", GameStatuses.COMPLETED, 0, 1))
    }

    @Test
    fun `chess ai moves exactly one black piece`() {
        val board = "♖,.,.,.,.,.,.,.,♜,♞,♝,♚,.,.,.,."
        val update = ChessEngine.applyAiMove(board, Random(11))
        assertNotNull(update)
        val before = board.split(",")
        val after = update!!.gameData.split(",")
        val movedFrom = before.indices.filter { before[it] != after[it] }
        assertEquals(2, movedFrom.size) // one emptied, one filled
        val from = movedFrom.first { after[it] == "." }
        val to = movedFrom.first { after[it] != "." }
        assertEquals(before[from], after[to])
    }

    // ---------- ConnectFourEngine ----------

    @Test
    fun `connect four chip drops to lowest empty row`() {
        val update = ConnectFourEngine.applyPlayerMove(List(25) { "" }.joinToString(","), null, 2)
        assertNotNull(update)
        // row 4 (bottom) col 2 -> index 22
        val cells = update!!.gameData.split(",")
        assertEquals("R", cells[22])
    }

    @Test
    fun `connect four stacks on top of existing chips`() {
        val board = MutableList(25) { "" }
        board[22] = "R" // bottom of col 2 taken
        val update = ConnectFourEngine.applyPlayerMove(board.joinToString(","), null, 2)
        assertNotNull(update)
        assertEquals("R", update!!.gameData.split(",")[17]) // row 3 col 2
    }

    @Test
    fun `connect four rejects full column and completed game`() {
        val board = MutableList(25) { "R" }
        assertNull(ConnectFourEngine.applyPlayerMove(board.joinToString(","), null, 0))
        assertNull(ConnectFourEngine.applyPlayerMove(List(25) { "" }.joinToString(","), GameStatuses.COMPLETED, 0))
    }

    @Test
    fun `connect four detects horizontal win`() {
        // R at bottom row cols 0..2; drop in col 3 to win
        val board = MutableList(25) { "" }
        board[20] = "R"; board[21] = "R"; board[22] = "R"
        val update = ConnectFourEngine.applyPlayerMove(board.joinToString(","), null, 3)
        assertNotNull(update)
        assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
        assertEquals(Winners.ME, update.gameWinner)
    }

    @Test
    fun `connect four detects vertical win`() {
        val board = MutableList(25) { "" }
        board[20] = "R"; board[15] = "R"; board[10] = "R" // col 0 rows 2-4
        val update = ConnectFourEngine.applyPlayerMove(board.joinToString(","), null, 0)
        assertNotNull(update)
        assertEquals(Winners.ME, update!!.gameWinner)
    }

    @Test
    fun `connect four detects diagonal win`() {
        val board = MutableList(25) { "" }
        // diagonal down-right: (0,0) (1,1) (2,2) filled with R; win by (3,3)
        board[0] = "R"; board[6] = "R"; board[12] = "R"
        val update = ConnectFourEngine.applyPlayerMove(board.joinToString(","), null, 3)
        assertNotNull(update)
        assertEquals(Winners.ME, update!!.gameWinner)
    }

    @Test
    fun `connect four ai drops exactly one Y in a valid spot`() {
        val board = MutableList(25) { "" }
        board[22] = "R"
        val update = ConnectFourEngine.applyAiMove(board.joinToString(","), Random(3))
        assertNotNull(update)
        val after = update!!.gameData.split(",")
        assertEquals(1, after.count { it == "Y" })
        // Y must rest on a filled cell (gravity): every cell below it is non-empty
        val yIdx = after.indices.first { after[it] == "Y" }
        val col = yIdx % 5
        for (row in (yIdx / 5) - 1 downTo 0) {
            assertTrue("cell below Y must be filled", after[row * 5 + col].isNotEmpty())
        }
    }

    // ---------- BattleshipEngine ----------

    private fun battleshipData(): String {
        // playerShots 8 cells all ".", aiShips at 2 and 5, aiShots 8 cells ".", playerShips at 1 and 6
        return ".,.,.,.,.,.,.,.|2,5|.,.,.,.,.,.,.,.|1,6"
    }

    @Test
    fun `battleship hit and miss are recorded`() {
        val hit = BattleshipEngine.applyPlayerMove(battleshipData(), null, 2)
        assertNotNull(hit)
        assertTrue(hit!!.content.contains("Hit"))
        assertEquals(GameStatuses.ACTIVE, hit.gameStatus)

        val miss = BattleshipEngine.applyPlayerMove(battleshipData(), null, 0)
        assertNotNull(miss)
        assertTrue(miss!!.content.contains("Miss"))
    }

    @Test
    fun `battleship rejects repeat shots and bad data`() {
        val shotOnce = BattleshipEngine.applyPlayerMove(battleshipData(), null, 2)!!
        assertNull(BattleshipEngine.applyPlayerMove(shotOnce.gameData, shotOnce.gameStatus, 2))
        assertNull(BattleshipEngine.applyPlayerMove(".,.|2|.,.|1", null, 0))
        assertNull(BattleshipEngine.applyPlayerMove(battleshipData(), GameStatuses.COMPLETED, 0))
    }

    @Test
    fun `battleship sinking all ships wins the game`() {
        var data = battleshipData()
        var status: String? = null
        for (cell in listOf(2, 5)) {
            val update = BattleshipEngine.applyPlayerMove(data, status, cell)
            assertNotNull(update)
            status = update!!.gameStatus
            data = update.gameData
        }
        assertEquals(GameStatuses.COMPLETED, status)
        assertTrue(data.split("|")[0].split(",").count { it == "H" } == 2)
    }

    @Test
    fun `battleship ai shoots exactly one new cell`() {
        val update = BattleshipEngine.applyAiMove(battleshipData(), Random(5))
        assertNotNull(update)
        val aiShots = update!!.gameData.split("|")[2].split(",")
        assertEquals(7, aiShots.count { it == "." })
        assertEquals(1, aiShots.count { it == "H" || it == "M" })
    }

    // ---------- DiceDuelEngine ----------

    @Test
    fun `dice duel completes after three rounds with consistent arithmetic`() {
        var data = "0,0,0,0,0"
        var status: String? = null
        var round = 0
        var lastWinner: String? = null
        repeat(3) { i ->
            val update = DiceDuelEngine.roll(data, status, Random(10 + i))
            assertNotNull(update)
            val parts = update!!.gameData.split(",").map { it.toInt() }
            val (userScore, oppScore, r, userRoll, oppRoll) = parts
            val prev = data.split(",").map { it.toInt() }
            assertEquals(prev[0] + userRoll, userScore)
            assertEquals(prev[1] + oppRoll, oppScore)
            assertTrue("rolls are 2d6", userRoll in 2..12 && oppRoll in 2..12)
            round = r
            status = update.gameStatus
            lastWinner = update.gameWinner
            data = update.gameData
        }
        assertEquals(3, round)
        assertEquals(GameStatuses.COMPLETED, status)
        val (u, o) = data.split(",").map { it.toInt() }
        val expectedWinner = if (u > o) Winners.ME else if (o > u) Winners.OPPONENT else Winners.DRAW
        assertEquals(expectedWinner, lastWinner)
        // completed game rejects further rolls
        assertNull(DiceDuelEngine.roll(data, status, Random(1)))
    }

    @Test
    fun `dice duel rejects rolling a completed game`() {
        assertNull(DiceDuelEngine.roll("10,5,3,4,3", GameStatuses.COMPLETED, Random(1)))
    }

    // ---------- MemoryMatchEngine ----------

    private fun memoryData(): String {
        // 16 cards, 8 pairs: A A B B C C D D E E F F G G H H
        val layout = "A,A,B,B,C,C,D,D,E,E,F,F,G,G,H,H".split(",")
        val shuffled = listOf("C", "A", "H", "E", "B", "G", "D", "F", "F", "D", "G", "B", "E", "H", "A", "C")
        return shuffled.joinToString(",") + "|" + List(16) { "." }.joinToString(",") + "|-1|0|0"
    }

    @Test
    fun `memory first flip stores selection`() {
        val update = MemoryMatchEngine.applyPlayerMove(memoryData(), null, 3)
        assertNotNull(update)
        assertEquals("3", update!!.gameData.split("|")[2])
        assertTrue(update.content.contains("Flipped first card"))
        assertNull(update.gameStatus) // unchanged
    }

    @Test
    fun `memory matching pair scores and marks cards`() {
        val data = memoryData()
        // find a pair: card values C(0) and C(15)
        val first = MemoryMatchEngine.applyPlayerMove(data, null, 0)!!
        val second = MemoryMatchEngine.applyPlayerMove(first.gameData, null, 15)
        assertNotNull(second)
        val parts = second!!.gameData.split("|")
        val matched = parts[1].split(",")
        assertEquals("M", matched[0])
        assertEquals("M", matched[15])
        assertEquals("-1", parts[2])
        assertEquals("1", parts[3])
        assertEquals(GameStatuses.ACTIVE, second.gameStatus)
        assertFalse(second.needsAiFollowUp)
    }

    @Test
    fun `memory mismatch hands over to ai`() {
        val data = memoryData()
        val first = MemoryMatchEngine.applyPlayerMove(data, null, 0)!! // C
        val second = MemoryMatchEngine.applyPlayerMove(first.gameData, null, 1)!! // A (mismatch)
        assertTrue(second.content.contains("doesn't match"))
        assertTrue(second.needsAiFollowUp)
        assertEquals("-1", second.gameData.split("|")[2])
    }

    @Test
    fun `memory rejects matched cards and completed games`() {
        assertNull(MemoryMatchEngine.applyPlayerMove(memoryData(), GameStatuses.COMPLETED, 0))
        val first = MemoryMatchEngine.applyPlayerMove(memoryData(), null, 0)!!
        assertNull(MemoryMatchEngine.applyPlayerMove(first.gameData, null, 0)) // same card twice
    }

    @Test
    fun `memory ai with two matching cards left always matches and finishes game`() {
        // Only cards 0 and 1 unmatched, and they are a pair -> game over after AI match.
        // Both the "smart" branch (finds the pair) and the random branch (only 2 cards
        // left) must produce a match, so this is deterministic for any seed.
        val layout = "X,X,M,M,M,M,M,M,M,M,M,M,M,M,M,M"
        val matched = ".,.,M,M,M,M,M,M,M,M,M,M,M,M,M,M"
        for (seed in 1L..10L) {
            val data = "$layout|$matched|-1|7|7"
            val update = MemoryMatchEngine.applyAiMove(data, Random(seed))
            assertNotNull("seed=$seed", update)
            val parts = update!!.gameData.split("|")
            assertEquals("M", parts[1].split(",")[0])
            assertEquals("M", parts[1].split(",")[1])
            assertEquals("8", parts[4]) // AI score 8 vs player 7
            assertEquals(GameStatuses.COMPLETED, update.gameStatus)
            assertEquals(Winners.OPPONENT, update.gameWinner) // 7 - 8 -> opponent wins
        }
    }

    // ---------- NumberGuessEngine ----------

    @Test
    fun `number guess too low and too high feedback`() {
        val data = "50,0,6"
        val low = NumberGuessEngine.guess(data, null, 10)
        assertNotNull(low)
        assertTrue(low!!.content.contains("Too Low"))
        val high = NumberGuessEngine.guess(data, null, 90)
        assertNotNull(high)
        assertTrue(high!!.content.contains("Too High"))
    }

    @Test
    fun `number guess correct answer wins`() {
        val update = NumberGuessEngine.guess("50,2,6", null, 50)
        assertNotNull(update)
        assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
        assertEquals(Winners.ME, update.gameWinner)
        assertTrue(update.content.contains("50"))
    }

    @Test
    fun `number guess runs out of attempts and loses`() {
        val update = NumberGuessEngine.guess("50,5,6", null, 12)
        assertNotNull(update)
        assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
        assertEquals(Winners.OPPONENT, update.gameWinner)
        assertTrue(update.content.contains("out of attempts"))
    }

    // ---------- BlackjackEngine ----------

    @Test
    fun `blackjack score handles aces correctly`() {
        assertEquals(21, BlackjackEngine.score(listOf(11, 10)))
        assertEquals(12, BlackjackEngine.score(listOf(11, 11)))
        assertEquals(21, BlackjackEngine.score(listOf(11, 11, 9)))
        assertEquals(21, BlackjackEngine.score(listOf(11, 5, 5)))
        assertEquals(25, BlackjackEngine.score(listOf(10, 10, 5)))
        assertEquals(20, BlackjackEngine.score(listOf(10, 10)))
    }

    @Test
    fun `blackjack hit adds exactly one card`() {
        val update = BlackjackEngine.play("10,9|10,7", null, BlackjackEngine.ACTION_HIT, Random(4))
        assertNotNull(update)
        val playerHand = update!!.gameData.split("|")[0].split(",").map { it.toInt() }
        assertEquals(3, playerHand.size)
        if (update.gameStatus != GameStatuses.COMPLETED) {
            assertNull(update.gameWinner) // hand still in play
            assertTrue(update.content.contains("Hit or Stand"))
        }
    }

    @Test
    fun `blackjack bust on hit loses immediately`() {
        // player has 20; most draws bust (only an Ace keeps it alive)
        var busted = false
        for (seed in 1L..40L) {
            val update = BlackjackEngine.play("10,10|10,7", null, BlackjackEngine.ACTION_HIT, Random(seed))
            if (update!!.gameStatus == GameStatuses.COMPLETED) {
                assertEquals(Winners.OPPONENT, update.gameWinner)
                assertTrue(update.content.contains("Bust"))
                busted = true
                break
            }
        }
        assertTrue("expected at least one bust in 40 seeded hits", busted)
    }

    @Test
    fun `blackjack stand resolves with dealer hitting to 17+ or busting`() {
        for (seed in 1L..25L) {
            val update = BlackjackEngine.play("10,9|10,6", null, BlackjackEngine.ACTION_STAND, Random(seed))
            assertNotNull(update)
            assertEquals(GameStatuses.COMPLETED, update!!.gameStatus)
            val (playerPart, dealerPart) = update.gameData.split("|")
            val playerScore = BlackjackEngine.score(playerPart.split(",").map { it.toInt() })
            val dealerScore = BlackjackEngine.score(dealerPart.split(",").map { it.toInt() })
            assertTrue("dealer must reach 17+ or bust", dealerScore >= 17)
            val expectedWinner = when {
                dealerScore > 21 -> Winners.ME
                playerScore > dealerScore -> Winners.ME
                dealerScore > playerScore -> Winners.OPPONENT
                else -> Winners.DRAW
            }
            assertEquals("seed=$seed", expectedWinner, update.gameWinner)
        }
    }

    @Test
    fun `blackjack rejects unknown actions and completed games`() {
        assertNull(BlackjackEngine.play("10|10", null, "FOLD", Random(1)))
        assertNull(BlackjackEngine.play("10|10", GameStatuses.COMPLETED, BlackjackEngine.ACTION_HIT, Random(1)))
    }
}
