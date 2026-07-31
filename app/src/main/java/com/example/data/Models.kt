package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String,
    val senderName: String,
    val senderAvatar: String,
    val content: String,
    val type: String, // TEXT, VOICE, STICKER, MEDIA, GAME_INVITE
    val gameType: String? = null, // TIC_TAC_TOE, ROCK_PAPER_SCI
    val gameStatus: String? = null, // PENDING, ACTIVE, COMPLETED, REJECTED
    val gameWinner: String? = null,
    val gameData: String? = null, // Holds Tic-Tac-Toe grid or state (e.g. "X,O,,O,X,,,X,")
    val isEncrypted: Boolean = true,
    val encryptionAlgorithm: String = "Signal-GCM-256",
    val reactionsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_streams")
data class LiveStream(
    @PrimaryKey val id: String,
    val streamerName: String,
    val streamerAvatar: String,
    val streamTitle: String,
    val gameName: String,
    val thumbnailResName: String,
    val viewerCount: Int,
    val isUserStream: Boolean,
    val isLive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val userId: String = "me",
    val username: String = "WeGamer_Me",
    val avatarResName: String = "img_profile_me_1783282394572",
    val coins: Int = 1200,
    val wins: Int = 18,
    val streamsCount: Int = 4,
    val globalRank: Int = 245,
    val isDarkMode: Boolean = true,
    val chatWallpaperColor: String = "DEFAULT",
    val chatWallpaperPattern: String = "DOODLE",
    val privacyLastSeen: String = "Everyone",
    val privacyProfilePhoto: String = "Everyone",
    val privacyAbout: String = "Everyone",
    val privacyReadReceipts: Boolean = true,
    val disappearingMessagesTimer: String = "Off",
    val lastBackupTime: Long = 0L,
    val backupInterval: String = "Off",
    val backupDestination: String = "Local Storage",
    val authPhone: String = "",
    val authGmail: String = "",
    val authFacebook: String = "",
    val authX: String = "",
    val xp: Int = 450,
    val level: Int = 8,
    val gamerTag: String = "WG#9842",
    val questsCompletedJson: String = "[]",
    val isPremium: Boolean = false,
    val premiumTier: String = "FREE",
    val storageUsedBytes: Long = 26214400L,
    val storageLimitBytes: Long = 15L * 1024 * 1024 * 1024,
    val dailyStreak: Int = 1,
    val lastCheckInTime: Long = 0L,
    val referralCode: String = "WEGIST99",
    val referredByCode: String = "",
    val unlockedBorder: String = "NONE"
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val imageUrl: String? = null, // e.g. "img_retro_stream_thumbnail_1783282368689" etc
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val hasLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reels")
data class Reel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorName: String,
    val creatorAvatar: String,
    val caption: String,
    val gameTag: String,
    val thumbnailResName: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val hasLiked: Boolean = false,
    val viewsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val userId: String,
    val username: String,
    val avatarResName: String,
    val globalRank: Int,
    val wins: Int,
    val isOnline: Boolean = true,
    val presence: String = "online"
)

@Entity(tableName = "friend_requests")
data class FriendRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val receiverId: String, // "me" or other userId
    val status: String, // PENDING, ACCEPTED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorAvatar: String,
    val textContent: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)


