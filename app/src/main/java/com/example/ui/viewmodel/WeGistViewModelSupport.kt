package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.UUID

// Support models for the live-interaction UI (LFG lobbies, stream chat).

data class LfgLobby(
    val id: String = UUID.randomUUID().toString(),
    val gameName: String,
    val title: String,
    val hostName: String,
    val maxPlayers: Int = 4,
    val joinedPlayers: List<String> = listOf(),
    val voiceActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class StreamComment(
    val senderName: String,
    val content: String,
    val badge: String
)

data class FloatingEmoji(
    val id: String,
    val emoji: String,
    val xOffset: Int
)

class WeGistViewModelFactory(
    private val application: Application,
    private val repository: com.example.data.Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeGistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeGistViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
