package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.viewmodel.WeGistViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var repository: Repository
  private lateinit var viewModel: WeGistViewModel

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
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

  @Test
  fun `test send and accept friend request`() = runBlocking {
    // 1. Wait for ViewModel init block's seed coroutines to finish
    val friends = viewModel.allFriends.first { it.size == 3 }
    val requests = viewModel.pendingRequests.first { it.size == 2 }
    assertEquals(3, friends.size)
    assertEquals(2, requests.size)

    // 2. Send friend request
    viewModel.sendFriendRequest("gamer_friend", "GamerFriend", "avatar_url")
    val sentRequests = repository.sentRequests.first { it.isNotEmpty() }
    assertEquals(1, sentRequests.size)
    assertEquals("gamer_friend", sentRequests[0].receiverId)

    // 3. Simulate receiving friend request (insert to pending)
    val incomingReq = FriendRequest(
      id = 0,
      senderId = "dave_the_pro",
      senderName = "DaveThePro",
      senderAvatar = "avatar_url",
      receiverId = "me",
      status = "PENDING",
      timestamp = System.currentTimeMillis()
    )
    database.friendRequestDao().insertRequest(incomingReq)
    val updatedRequests = viewModel.pendingRequests.first { it.size == 3 }
    assertTrue(updatedRequests.any { it.senderId == "dave_the_pro" })

    // 4. Accept incoming friend request
    val targetReq = updatedRequests.first { it.senderId == "dave_the_pro" }
    viewModel.acceptFriendRequest(targetReq)
    
    // Check that pending request is marked ACCEPTED, and new Friend is added
    val updatedFriends = viewModel.allFriends.first { it.size == 4 }
    assertTrue(updatedFriends.any { it.userId == "dave_the_pro" })
  }

  @Test
  fun `test update friend presence`() = runBlocking {
    // Wait for initial seed friends
    val friends = viewModel.allFriends.first { it.size == 3 }
    val alice = friends.first { it.userId == "alice" }
    assertEquals("online", alice.presence)
    assertTrue(alice.isOnline)

    // Update Alice presence to in-game
    viewModel.updateFriendPresence("alice", "in-game")
    val updated1 = viewModel.allFriends.first { list ->
      list.any { it.userId == "alice" && it.presence == "in-game" }
    }.first { it.userId == "alice" }
    assertEquals("in-game", updated1.presence)
    assertTrue(updated1.isOnline)

    // Update Alice presence to offline
    viewModel.updateFriendPresence("alice", "offline")
    val updated2 = viewModel.allFriends.first { list ->
      list.any { it.userId == "alice" && it.presence == "offline" }
    }.first { it.userId == "alice" }
    assertEquals("offline", updated2.presence)
    assertFalse(updated2.isOnline)
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("WeGist", appName)
  }

  @Test
  fun `test default theme is dark mode`() = runBlocking {
    val stats = viewModel.userStats.first { it.username == "WeGamer_Me" }
    assertTrue(stats.isDarkMode)
  }

  @Test
  fun `test toggle theme saves to database`() = runBlocking {
    // Wait for ViewModel init block's seed coroutines to finish inserting UserStats
    val initialStats = viewModel.userStats.first { it.username == "WeGamer_Me" }
    assertTrue(initialStats.isDarkMode)

    viewModel.setDarkMode(false)
    // wait for flow/database reflection
    val stats = viewModel.userStats.first { !it.isDarkMode }
    assertFalse(stats.isDarkMode)
  }
}
