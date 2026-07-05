package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class WeGistViewModel(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    // Active Navigation / Navigation Target state
    val activeChatId = MutableStateFlow<String?>(null)
    val activeStreamId = MutableStateFlow<String?>(null)
    val isUserBroadcasting = MutableStateFlow(false)

    // Broadcasting custom title & selected game
    val broadcastTitle = MutableStateFlow("Arcade Madness LIVE!")
    val broadcastGameName = MutableStateFlow("Space Shooter")

    // Reactive database streams
    val allMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStreams: StateFlow<List<LiveStream>> = repository.activeStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats> = repository.userStats
        .map { it ?: UserStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val allPosts: StateFlow<List<Post>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReels: StateFlow<List<Reel>> = repository.allReels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Privacy Controls (in-memory settings, but can be updated)
    val restrictInvitesToFriends = MutableStateFlow(true)
    val restrictStreamsToFriends = MutableStateFlow(false)

    // Live Streaming Viewers Interaction States
    private val _streamComments = MutableStateFlow<List<StreamComment>>(emptyList())
    val streamComments: StateFlow<List<StreamComment>> = _streamComments.asStateFlow()

    // Floating reaction emojis channel (sent during live streams)
    private val _floatingEmojis = MutableStateFlow<List<FloatingEmoji>>(emptyList())
    val floatingEmojis: StateFlow<List<FloatingEmoji>> = _floatingEmojis.asStateFlow()

    // Interactive arcade gameplay states inside live streaming / games hub
    val arcadeScore = MutableStateFlow(0)
    val arcadeHighscore = MutableStateFlow(3200)

    init {
        // Seed initial data if database is empty
        viewModelScope.launch {
            repository.clearNonUserStreams()
            
            // Seed UserStats
            repository.insertUserStats(UserStats())

            // Seed Live Streams if empty
            val currentStreams = repository.activeStreams.first()
            if (currentStreams.isEmpty()) {
                repository.insertStream(
                    LiveStream(
                        id = "stream_bob",
                        streamerName = "Bob_Destroyer",
                        streamerAvatar = "img_profile_me_1783282394572", // generic
                        streamTitle = "Breaking brick highscores today! 🔥",
                        gameName = "Brick Breaker Classic",
                        thumbnailResName = "img_puzzle_stream_thumbnail_1783282381092",
                        viewerCount = 1420,
                        isUserStream = false
                    )
                )
                repository.insertStream(
                    LiveStream(
                        id = "stream_queen",
                        streamerName = "SpaceQueen",
                        streamerAvatar = "img_profile_me_1783282394572",
                        streamTitle = "Ranked space matches! 🌌 Let's go!",
                        gameName = "Space Shooter",
                        thumbnailResName = "img_retro_stream_thumbnail_1783282368689",
                        viewerCount = 890,
                        isUserStream = false
                    )
                )
            }

            // Seed initial messages to simulate active chats
            val currentMessages = repository.allMessages.first()
            if (currentMessages.isEmpty()) {
                // Alice Chat (chatId = "alice")
                repository.insertMessage(
                    ChatMessage(
                        chatId = "alice",
                        senderName = "Alice (Retro Gamer)",
                        senderAvatar = "img_profile_me_1783282394572",
                        content = "Hey! Down for some casual gaming? 🎮",
                        type = "TEXT"
                    )
                )
                repository.insertMessage(
                    ChatMessage(
                        chatId = "alice",
                        senderName = "Alice (Retro Gamer)",
                        senderAvatar = "img_profile_me_1783282394572",
                        content = "I challenge you to Tic-Tac-Toe! Make your move:",
                        type = "GAME_INVITE",
                        gameType = "TIC_TAC_TOE",
                        gameStatus = "PENDING",
                        gameData = ",,,,,,,," // Empty board (9 commas-separated cells)
                    )
                )

                // Bob Chat (chatId = "bob")
                repository.insertMessage(
                    ChatMessage(
                        chatId = "bob",
                        senderName = "Bob_Destroyer",
                        senderAvatar = "img_profile_me_1783282394572",
                        content = "What's up, I just went live broadcasting Brick Breaker!",
                        type = "TEXT"
                    )
                )
                repository.insertMessage(
                    ChatMessage(
                        chatId = "bob",
                        senderName = "Bob_Destroyer",
                        senderAvatar = "img_profile_me_1783282394572",
                        content = "https://wegist.live/stream_bob",
                        type = "MEDIA" // simulated stream sharing
                    )
                )
            }

            // Seed initial posts if empty
            val currentPosts = repository.allPosts.first()
            if (currentPosts.isEmpty()) {
                repository.insertPost(
                    Post(
                        authorName = "Bob_Destroyer",
                        authorAvatar = "img_profile_me_1783282394572",
                        content = "Just set an absolute new personal highscore in Brick Breaker! Check out my stream tomorrow 🎮🔥 I might stream Rock-Paper-Scissors challenges too!",
                        imageUrl = "img_puzzle_stream_thumbnail_1783282381092",
                        likesCount = 24,
                        commentsCount = 3,
                        hasLiked = false
                    )
                )
                repository.insertPost(
                    Post(
                        authorName = "SpaceQueen",
                        authorAvatar = "img_profile_me_1783282394572",
                        content = "Space Shooter rank matching is so intense tonight! Who's down for a co-op or 1v1 challenge? Let's go! 🌌🛸🚀",
                        likesCount = 18,
                        commentsCount = 7,
                        hasLiked = false
                    )
                )
                repository.insertPost(
                    Post(
                        authorName = "Alice (Retro Gamer)",
                        authorAvatar = "img_profile_me_1783282394572",
                        content = "Hello WeGist! Just joined the best gaming social community around! 👾⚡ Mini-games here are so addictive.",
                        likesCount = 31,
                        commentsCount = 12,
                        hasLiked = false
                    )
                )
            }

            // Seed initial reels if empty
            val currentReels = repository.allReels.first()
            if (currentReels.isEmpty()) {
                repository.insertReel(
                    Reel(
                        creatorName = "Alice (Retro Gamer)",
                        creatorAvatar = "img_profile_me_1783282394572",
                        caption = "Brick Breaker insane double reflection trickshot! 🎯🙌 Double coins incoming!",
                        gameTag = "Brick Breaker Classic",
                        thumbnailResName = "img_puzzle_stream_thumbnail_1783282381092",
                        likesCount = 145,
                        commentsCount = 9,
                        hasLiked = false,
                        viewsCount = 1205
                    )
                )
                repository.insertReel(
                    Reel(
                        creatorName = "SpaceQueen",
                        creatorAvatar = "img_profile_me_1783282394572",
                        caption = "Surviving with literally 1 HP in the space shooter arena! absolute madness! 🛸💥🌌",
                        gameTag = "Space Shooter",
                        thumbnailResName = "img_retro_stream_thumbnail_1783282368689",
                        likesCount = 289,
                        commentsCount = 24,
                        hasLiked = false,
                        viewsCount = 3491
                    )
                )
            }
        }

        // Periodic simulated comments/reactions when viewing a stream
        startSimulatedStreamActivity()
    }

    // --- MESSAGING & GAMES ACTIONS ---

    fun sendMessage(chatId: String, content: String, type: String = "TEXT", gameType: String? = null, gameData: String? = null) {
        viewModelScope.launch {
            val me = userStats.value
            val message = ChatMessage(
                chatId = chatId,
                senderName = me.username,
                senderAvatar = me.avatarResName,
                content = content,
                type = type,
                gameType = gameType,
                gameStatus = if (gameType != null) "PENDING" else null,
                gameData = gameData
            )
            repository.insertMessage(message)

            // Dynamic interactive AI opponent reply
            if (type == "TEXT") {
                delay(1200)
                simulateOpponentReply(chatId, content)
            }
        }
    }

    private suspend fun simulateOpponentReply(chatId: String, userText: String) {
        val replyContent = when {
            userText.contains("game", ignoreCase = true) || userText.contains("play", ignoreCase = true) -> {
                "I am always ready! Challenge me to Tic Tac Toe or Rock Paper Scissors! ⚔️"
            }
            userText.contains("hi", ignoreCase = true) || userText.contains("hello", ignoreCase = true) -> {
                "Hey! What's up? Just finishing a stream. Let's play a game!"
            }
            else -> {
                "Nice! Speaking of games, send me a challenge from the game invite menu (+ button)!"
            }
        }

        val name = if (chatId == "alice") "Alice (Retro Gamer)" else "Bob_Destroyer"
        repository.insertMessage(
            ChatMessage(
                chatId = chatId,
                senderName = name,
                senderAvatar = "img_profile_me_1783282394572",
                content = replyContent,
                type = "TEXT"
            )
        )
        // Fire native push notification
        com.example.util.NotificationHelper.showNotification(getApplication(), name, replyContent)
    }

    // Interactive Tic-Tac-Toe Move
    fun playTicTacToeMove(messageId: Int, index: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            val board = message.gameData?.split(",")?.toMutableList() ?: MutableList(9) { "" }

            if (board[index].isEmpty() && message.gameStatus != "COMPLETED") {
                // User moves as "X"
                board[index] = "X"
                val updatedBoardData = board.joinToString(",")

                // Check win/draw
                val isUserWin = checkTicTacToeWin(board, "X")
                val isDraw = !board.contains("") && !isUserWin

                val status = if (isUserWin || isDraw) "COMPLETED" else "ACTIVE"
                val winner = if (isUserWin) "me" else if (isDraw) "DRAW" else null

                val updatedMsg = message.copy(
                    gameStatus = status,
                    gameWinner = winner,
                    gameData = updatedBoardData,
                    content = if (isUserWin) "I won the Tic-Tac-Toe challenge! 🎉" else if (isDraw) "The Tic-Tac-Toe game is a draw!" else "Your turn in Tic-Tac-Toe!"
                )
                repository.updateMessage(updatedMsg)

                if (isUserWin) {
                    awardCoins(100)
                    incrementWins()
                }

                // If game is active, trigger AI counter-move
                if (status == "ACTIVE") {
                    delay(1500)
                    triggerTicTacToeAIMove(updatedMsg, board)
                }
            }
        }
    }

    private suspend fun triggerTicTacToeAIMove(message: ChatMessage, board: MutableList<String>) {
        // Find empty cells
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isNotEmpty()) {
            // Pick a random empty index
            val aiMove = emptyIndices.random()
            board[aiMove] = "O"
            val updatedBoardData = board.joinToString(",")

            val isAIWin = checkTicTacToeWin(board, "O")
            val isDraw = !board.contains("") && !isAIWin

            val status = if (isAIWin || isDraw) "COMPLETED" else "ACTIVE"
            val winner = if (isAIWin) "opponent" else if (isDraw) "DRAW" else null

            val updatedMsg = message.copy(
                gameStatus = status,
                gameWinner = winner,
                gameData = updatedBoardData,
                content = if (isAIWin) "Alice won the Tic-Tac-Toe challenge! 🤖" else if (isDraw) "The Tic-Tac-Toe game is a draw!" else "I made my move! Your turn!"
            )
            repository.updateMessage(updatedMsg)
        }
    }

    private fun checkTicTacToeWin(board: List<String>, player: String): Boolean {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )
        return winPatterns.any { pattern ->
            pattern.all { board[it] == player }
        }
    }

    // Rock-Paper-Scissors Move
    fun playRockPaperScissors(messageId: Int, userChoice: String) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch

            val choices = listOf("ROCK", "PAPER", "SCISSORS")
            val opponentChoice = choices.random()

            val winner = when {
                userChoice == opponentChoice -> "DRAW"
                (userChoice == "ROCK" && opponentChoice == "SCISSORS") ||
                (userChoice == "PAPER" && opponentChoice == "ROCK") ||
                (userChoice == "SCISSORS" && opponentChoice == "PAPER") -> "me"
                else -> "opponent"
            }

            val resultText = when (winner) {
                "me" -> "I won! I chose $userChoice, Opponent chose $opponentChoice. 🎉"
                "opponent" -> "Opponent won! I chose $userChoice, Opponent chose $opponentChoice. 🤖"
                else -> "It's a tie! Both chose $userChoice. 🤝"
            }

            val updatedMsg = message.copy(
                gameStatus = "COMPLETED",
                gameWinner = winner,
                gameData = "$userChoice,$opponentChoice",
                content = resultText
            )
            repository.updateMessage(updatedMsg)

            if (winner == "me") {
                awardCoins(80)
                incrementWins()
            }
        }
    }

    // --- LIVE STREAMING HUB ACTIONS ---

    private fun startSimulatedStreamActivity() {
        viewModelScope.launch {
            while (true) {
                delay(2500)
                // If viewing a stream or broadcasting, add simulated comments/emojis
                if (activeStreamId.value != null || isUserBroadcasting.value) {
                    addSimulatedComment()
                    if (Math.random() > 0.4) {
                        triggerFloatingReaction(listOf("❤️", "🔥", "🎮", "🚀", "🤣", "👑").random())
                    }
                }
            }
        }
    }

    fun selectStream(streamId: String?) {
        activeStreamId.value = streamId
        _streamComments.value = emptyList() // clear on join
        if (streamId != null) {
            // Seed a few initial comments
            _streamComments.value = listOf(
                StreamComment("Gamer99", "This gameplay is wild!", "🔥"),
                StreamComment("RetroGuy", "No way, she actually made that jump!", "🙌")
            )
        }
    }

    fun startUserBroadcast(title: String, game: String) {
        viewModelScope.launch {
            broadcastTitle.value = title
            broadcastGameName.value = game
            isUserBroadcasting.value = true

            // Add user stream to database list
            val userStream = LiveStream(
                id = "stream_user",
                streamerName = userStats.value.username,
                streamerAvatar = "img_profile_me_1783282394572",
                streamTitle = title,
                gameName = game,
                thumbnailResName = if (game == "Space Shooter") "img_retro_stream_thumbnail_1783282368689" else "img_puzzle_stream_thumbnail_1783282381092",
                viewerCount = 1, // starts from 1
                isUserStream = true
            )
            repository.insertStream(userStream)
            _streamComments.value = listOf(
                StreamComment("System", "Broadcast started successfully. You are now LIVE! 📡", "✨")
            )

            // Let viewer count grow periodically
            launch {
                var viewers = 1
                while (isUserBroadcasting.value) {
                    delay(5000)
                    viewers += (3..12).random()
                    val updatedStream = userStream.copy(viewerCount = viewers)
                    repository.insertStream(updatedStream)
                }
            }
        }
    }

    fun stopUserBroadcast() {
        viewModelScope.launch {
            isUserBroadcasting.value = false
            repository.deleteStreamById("stream_user")
            
            // Increment user stream count reward
            val me = userStats.value
            repository.insertUserStats(
                me.copy(
                    streamsCount = me.streamsCount + 1,
                    coins = me.coins + 150 // streaming reward!
                )
            )
        }
    }

    fun sendStreamComment(content: String) {
        val me = userStats.value
        val newComment = StreamComment(me.username, content, "")
        _streamComments.value = _streamComments.value + newComment
    }

    private fun addSimulatedComment() {
        val names = listOf("Pro_Gamer", "LuckyStike", "PixelWizard", "NiteOwl", "CyberKnight", "NoobSlayer", "WannaBePro", "SpeedRunner")
        val comments = listOf(
            "Insane skills! 🔥",
            "How did you do that?! 😮",
            "Woooow let's go!! 🎉",
            "Can I join your lobby?",
            "GG, that was super clean!",
            "Ripping through this level!",
            "Cheering for you! 🌟",
            "Subscribed! Best gameplay this week"
        )
        val badges = listOf("", "🔥", "🛡️", "👾", "👑", "🚀")

        val newComment = StreamComment(names.random(), comments.random(), badges.random())
        _streamComments.value = (_streamComments.value + newComment).takeLast(30)
    }

    fun triggerFloatingReaction(emoji: String) {
        val id = UUID.randomUUID().toString()
        val randomXOffset = (-100..100).random()
        val newEmoji = FloatingEmoji(id, emoji, randomXOffset)
        _floatingEmojis.value = (_floatingEmojis.value + newEmoji).takeLast(15)

        // Clear out after 3 seconds so memory is preserved
        viewModelScope.launch {
            delay(3000)
            _floatingEmojis.value = _floatingEmojis.value.filter { it.id != id }
        }
    }

    fun sendVirtualGift(giftCost: Int) {
        viewModelScope.launch {
            val me = userStats.value
            if (me.coins >= giftCost) {
                // Deduct coins
                repository.insertUserStats(me.copy(coins = me.coins - giftCost))
                // Send alert comment
                val giftName = when (giftCost) {
                    50 -> "Cheering Star ⭐"
                    100 -> "Cool Shield 🛡️"
                    else -> "Golden Crown 👑"
                }
                _streamComments.value = _streamComments.value + StreamComment(
                    me.username,
                    "sent a virtual gift: $giftName!",
                    "🎁"
                )
                triggerFloatingReaction("🎁")
                triggerFloatingReaction("💖")
            }
        }
    }

    // --- REWARDS & SETTINGS ---

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(username = newUsername))
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(isDarkMode = enabled))
        }
    }

    fun updateWallpaper(color: String, pattern: String) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(chatWallpaperColor = color, chatWallpaperPattern = pattern))
        }
    }

    fun updatePrivacySettings(
        lastSeen: String,
        profilePhoto: String,
        about: String,
        readReceipts: Boolean,
        disappearingTimer: String
    ) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(
                privacyLastSeen = lastSeen,
                privacyProfilePhoto = profilePhoto,
                privacyAbout = about,
                privacyReadReceipts = readReceipts,
                disappearingMessagesTimer = disappearingTimer
            ))
        }
    }

    fun performBackup(interval: String, destination: String) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(
                lastBackupTime = System.currentTimeMillis(),
                backupInterval = interval,
                backupDestination = destination
            ))
        }
    }

    fun linkAccount(type: String, value: String) {
        viewModelScope.launch {
            val me = userStats.value
            val updated = when (type) {
                "PHONE" -> me.copy(authPhone = value)
                "GMAIL" -> me.copy(authGmail = value)
                "FACEBOOK" -> me.copy(authFacebook = value)
                "X" -> me.copy(authX = value)
                else -> me
            }
            repository.insertUserStats(updated)
        }
    }

    fun awardCoins(amount: Int) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(coins = me.coins + amount))
        }
    }

    private suspend fun incrementWins() {
        val me = userStats.value
        repository.insertUserStats(
            me.copy(
                wins = me.wins + 1,
                globalRank = maxOf(1, me.globalRank - 3) // rank gets better!
            )
        )
    }

    // --- POSTS & REELS ACTIONS ---

    fun createPost(content: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val me = userStats.value
            val newPost = Post(
                authorName = me.username,
                authorAvatar = me.avatarResName,
                content = content,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPost(newPost)
            awardCoins(50) // award coins for posting!
        }
    }

    fun toggleLikePost(post: Post) {
        viewModelScope.launch {
            val updatedPost = post.copy(
                hasLiked = !post.hasLiked,
                likesCount = if (post.hasLiked) post.likesCount - 1 else post.likesCount + 1
            )
            repository.updatePost(updatedPost)
        }
    }

    fun addPostComment(post: Post) {
        viewModelScope.launch {
            val updatedPost = post.copy(commentsCount = post.commentsCount + 1)
            repository.updatePost(updatedPost)
            awardCoins(10) // reward for commenting
        }
    }

    fun createReel(caption: String, gameTag: String, thumbnailRes: String) {
        viewModelScope.launch {
            val me = userStats.value
            val newReel = Reel(
                creatorName = me.username,
                creatorAvatar = me.avatarResName,
                caption = caption,
                gameTag = gameTag,
                thumbnailResName = thumbnailRes,
                likesCount = 0,
                commentsCount = 0,
                viewsCount = 1,
                timestamp = System.currentTimeMillis()
            )
            repository.insertReel(newReel)
            awardCoins(100) // bigger reward for reels!
        }
    }

    fun toggleLikeReel(reel: Reel) {
        viewModelScope.launch {
            val updatedReel = reel.copy(
                hasLiked = !reel.hasLiked,
                likesCount = if (reel.hasLiked) reel.likesCount - 1 else reel.likesCount + 1
            )
            repository.updateReel(updatedReel)
        }
    }

    fun incrementReelViews(reel: Reel) {
        viewModelScope.launch {
            val updatedReel = reel.copy(viewsCount = reel.viewsCount + 1)
            repository.updateReel(updatedReel)
        }
    }

    fun addReelComment(reel: Reel) {
        viewModelScope.launch {
            val updatedReel = reel.copy(commentsCount = reel.commentsCount + 1)
            repository.updateReel(updatedReel)
            awardCoins(10)
        }
    }

    fun shareContentToChat(chatId: String, contentType: String, author: String, content: String, mediaUrl: String?, note: String? = null) {
        viewModelScope.launch {
            val me = userStats.value
            val message = ChatMessage(
                chatId = chatId,
                senderName = me.username,
                senderAvatar = me.avatarResName,
                content = content,
                type = "REPOST",
                gameType = contentType, // "POST" or "REEL"
                gameStatus = author,     // original author name
                gameData = mediaUrl      // image/thumbnail
            )
            repository.insertMessage(message)

            if (!note.isNullOrBlank()) {
                delay(300) // slight buffer
                val noteMsg = ChatMessage(
                    chatId = chatId,
                    senderName = me.username,
                    senderAvatar = me.avatarResName,
                    content = note,
                    type = "TEXT"
                )
                repository.insertMessage(noteMsg)
            }

            awardCoins(30) // bonus reward for sharing with friends!
            
            // Trigger dynamic interactive response
            delay(1500)
            simulateShareReply(chatId, contentType, author)
        }
    }

    private suspend fun simulateShareReply(chatId: String, contentType: String, author: String) {
        val replyContent = when {
            author == "Bob_Destroyer" -> {
                "Oh wow, Bob's posts are always legendary! 🎮 Thanks for sharing this!"
            }
            author == "SpaceQueen" -> {
                "SpaceQueen is an absolute beast at Space Shooter! Breathtaking clip."
            }
            author.contains("Alice") -> {
                "Alice's gameplay is top tier! Thanks for sharing this with me! 🔥"
            }
            else -> {
                "This is such a cool $contentType! Thanks for sharing this with me! 🔥"
            }
        }
        val name = if (chatId == "alice") "Alice (Retro Gamer)" else if (chatId == "bob") "Bob_Destroyer" else "Gaming Guild"
        repository.insertMessage(
            ChatMessage(
                chatId = chatId,
                senderName = name,
                senderAvatar = "img_profile_me_1783282394572",
                content = replyContent,
                type = "TEXT"
            )
        )
    }
}

// Data definitions for Live Interaction UI
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
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeGistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeGistViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
