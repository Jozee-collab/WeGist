package com.example

import android.app.Application
import android.content.Context
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
    database = AppDatabase.getDatabase(context)
    repository = Repository(
      chatMessageDao = database.chatMessageDao(),
      liveStreamDao = database.liveStreamDao(),
      userStatsDao = database.userStatsDao(),
      postDao = database.postDao(),
      reelDao = database.reelDao()
    )
    val app = context.applicationContext as Application
    viewModel = WeGistViewModel(app, repository)
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("WeGist", appName)
  }

  @Test
  fun `test default theme is dark mode`() = runBlocking {
    val stats = viewModel.userStats.value
    assertTrue(stats.isDarkMode)
  }

  @Test
  fun `test toggle theme saves to database`() = runBlocking {
    viewModel.setDarkMode(false)
    // wait for flow/database reflection
    val stats = viewModel.userStats.first { !it.isDarkMode }
    assertFalse(stats.isDarkMode)
  }
}
