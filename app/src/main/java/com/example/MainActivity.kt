package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeGistViewModel
import com.example.ui.viewmodel.WeGistViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database, DAOs, and abstract Repository layer
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(
            chatMessageDao = database.chatMessageDao(),
            liveStreamDao = database.liveStreamDao(),
            userStatsDao = database.userStatsDao(),
            postDao = database.postDao(),
            reelDao = database.reelDao(),
            friendDao = database.friendDao(),
            friendRequestDao = database.friendRequestDao(),
            storyDao = database.storyDao()
        )

        // Instantiate WeGistViewModel using custom Factory
        val viewModelFactory = WeGistViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[WeGistViewModel::class.java]

        setContent {
            val stats by viewModel.userStats.collectAsStateWithLifecycle()
            val isDark = stats.isDarkMode
            val currentMode by viewModel.appMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDark, appMode = currentMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// Deprecated test compatibility helper
@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

