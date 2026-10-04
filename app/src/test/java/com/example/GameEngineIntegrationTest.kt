package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.Repository
import com.example.ui.viewmodel.WeGistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration test: verifies that a game move flows through the ViewModel
 * (engine delegation -> message merge -> Room persistence) correctly.
 *
 * Uses an UnconfinedTestDispatcher as Main so viewModelScope coroutines run
 * eagerly without depending on the Robolectric main looper.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameEngineIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: Repository
    private lateinit var viewModel: WeGistViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = Repository(
            chatMessageDao = database.chatMessageDao(),
            liveStreamDao = database.liveStreamDao(),
            userStatsDao = database.userStatsDao(),
            postDao = database.postDao(),
            reelDao = database.reelDao(),
            friendDao = database.friendDao(),
            friendRequestDao = database.friendRequestDao(),
            storyDao = database.storyDao()
        )
        val app = context.applicationContext as Application
        viewModel = WeGistViewModel(app, repository)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private suspend fun insertTicTacToeGame(status: String = "PENDING"): Int {
        val id = repository.insertMessage(
            ChatMessage(
                chatId = "alice",
                senderName = "Alice (Retro Gamer)",
                senderAvatar = "img_profile_me_1783282394572",
                content = "I challenge you to Tic-Tac-Toe! Make your move:",
                type = "GAME_INVITE",
                gameType = "TIC_TAC_TOE",
                gameStatus = status,
                gameData = List(9) { "" }.joinToString(",") // exactly 9 cells
            )
        )
        return id.toInt()
    }

    /** Waits until the given message is visible in the ViewModel's StateFlow cache. */
    private suspend fun awaitMessageVisible(id: Int) {
        viewModel.allMessages.first { list -> list.any { it.id == id } }
    }

    @Test
    fun `tic tac toe move persists through viewmodel into room`() = runBlocking {
        // Wait for the seeder to finish so allMessages is populated
        viewModel.allMessages.first { it.isNotEmpty() }

        val messageId = insertTicTacToeGame()
        // Ensure the inserted message has propagated into the StateFlow cache
        // BEFORE playing, otherwise allMessages.value may not contain it yet.
        awaitMessageVisible(messageId)

        viewModel.playTicTacToeMove(messageId, 4)

        val updated = viewModel.allMessages.first { list ->
            list.any { it.id == messageId && it.gameData != null && it.gameData.startsWith("X") }
        }.first { it.id == messageId }

        assertEquals("ACTIVE", updated.gameStatus)
        val cells = updated.gameData!!.split(",")
        assertEquals(9, cells.size)
        assertEquals("X", cells[4])
        assertNotNull(updated.content)
    }

    @Test
    fun `winning tic tac toe move completes game and awards coins`() = runBlocking {
        viewModel.allMessages.first { it.isNotEmpty() }

        // X at 0 and 3; O at 2, 4, 7, 8; cell 6 empty -> playing 6 wins column 0-3-6
        val id = repository.insertMessage(
            ChatMessage(
                chatId = "alice",
                senderName = "Alice (Retro Gamer)",
                senderAvatar = "img_profile_me_1783282394572",
                content = "TTT",
                type = "GAME_INVITE",
                gameType = "TIC_TAC_TOE",
                gameStatus = "ACTIVE",
                gameData = "X,,O,X,O,,,O,O"
            )
        ).toInt()

        val statsBefore = viewModel.userStats.first { it.username == "WeGamer_Me" }.coins

        awaitMessageVisible(id)
        viewModel.playTicTacToeMove(id, 6)

        val updated = viewModel.allMessages.first { list ->
            list.any { it.id == id && it.gameStatus == "COMPLETED" }
        }.first { it.id == id }

        assertEquals("COMPLETED", updated.gameStatus)
        assertEquals("me", updated.gameWinner)

        // +100 coins awarded for the win
        val statsAfter = viewModel.userStats.first { it.coins >= statsBefore + 100 }.coins
        assertTrue(statsAfter >= statsBefore + 100)
    }

    @Test
    fun `rock paper scissors move resolves through viewmodel`() = runBlocking {
        viewModel.allMessages.first { it.isNotEmpty() }

        val id = repository.insertMessage(
            ChatMessage(
                chatId = "alice",
                senderName = "Alice (Retro Gamer)",
                senderAvatar = "img_profile_me_1783282394572",
                content = "RPS challenge",
                type = "GAME_INVITE",
                gameType = "ROCK_PAPER_SCI",
                gameStatus = "PENDING",
                gameData = null
            )
        ).toInt()

        awaitMessageVisible(id)
        viewModel.playRockPaperScissors(id, "ROCK")

        val updated = viewModel.allMessages.first { list ->
            list.any { it.id == id && it.gameStatus == "COMPLETED" }
        }.first { it.id == id }

        assertEquals("COMPLETED", updated.gameStatus)
        assertTrue(updated.gameWinner in listOf("me", "opponent", "DRAW"))
        assertTrue(updated.gameData == "ROCK,ROCK" || updated.gameData!!.startsWith("ROCK,"))
    }
}
