package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.content.ContextCompat
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

    // Android 13+ requires the POST_NOTIFICATIONS runtime permission before the
    // app may show chat-message notifications.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not — notifications are optional */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
