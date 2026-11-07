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

    // App Mode: GAMING, CASUAL, ENTERPRISE
    val appMode = MutableStateFlow("GAMING")

    fun setAppMode(mode: String) {
        appMode.value = mode
    }

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

    val allStories: StateFlow<List<Story>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFriends: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<FriendRequest>> = repository.pendingRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentRequests: StateFlow<List<FriendRequest>> = repository.sentRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contacts and Firebase Synchronization states
    val isSyncingContacts = MutableStateFlow(false)
    val contactsSyncStatus = MutableStateFlow<String?>(null)
    val isSyncingWithFirebase = MutableStateFlow(false)
    val firebaseSyncStatus = MutableStateFlow<String?>(null)

    // LFG Lobbies list
    val lfgLobbies = MutableStateFlow<List<LfgLobby>>(listOf(
        LfgLobby(
            id = "lobby_1",
            gameName = "Valorant",
            title = "Ranked Gold push - Need 2! Duelists welcome",
            hostName = "GamerBob_99",
            maxPlayers = 5,
            joinedPlayers = listOf("GamerBob_99", "SpaceQueen"),
            voiceActive = true
        ),
        LfgLobby(
            id = "lobby_2",
            gameName = "Minecraft",
            title = "Chill Survival Bedwars! Cozy vibes",
            hostName = "NoobSlayer_X",
            maxPlayers = 4,
            joinedPlayers = listOf("NoobSlayer_X"),
            voiceActive = false
        ),
        LfgLobby(
            id = "lobby_3",
            gameName = "Elden Ring",
            title = "Boss Assist: Malenia! Bring high DPS",
            hostName = "LetMeSoloHer_Jr",
            maxPlayers = 3,
            joinedPlayers = listOf("LetMeSoloHer_Jr", "RetroKing"),
            voiceActive = true
        )
    ))

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
            
            // Seed the profile atomically without overwriting data saved by another startup action.
            repository.insertUserStatsIfMissing(UserStats())

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

            // Seed initial friends
            val currentFriends = repository.allFriends.first()
            if (currentFriends.isEmpty()) {
                repository.insertFriend(
                    Friend(
                        userId = "alice",
                        username = "Alice (Retro Gamer)",
                        avatarResName = "img_profile_me_1783282394572",
                        globalRank = 42,
                        wins = 32,
                        isOnline = true,
                        presence = "online"
                    )
                )
                repository.insertFriend(
                    Friend(
                        userId = "bob",
                        username = "Bob_Destroyer",
                        avatarResName = "img_profile_me_1783282394572",
                        globalRank = 110,
                        wins = 25,
                        isOnline = true,
                        presence = "in-game"
                    )
                )
                repository.insertFriend(
                    Friend(
                        userId = "dave",
                        username = "Dave_The_Noob",
                        avatarResName = "img_profile_me_1783282394572",
                        globalRank = 230,
                        wins = 2,
                        isOnline = false,
                        presence = "offline"
                    )
                )
            }

            // Seed initial incoming friend requests
            val currentRequests = repository.pendingRequests.first()
            if (currentRequests.isEmpty()) {
                repository.insertFriendRequest(
                    FriendRequest(
                        senderId = "spacequeen",
                        senderName = "SpaceQueen",
                        senderAvatar = "img_profile_me_1783282394572",
                        receiverId = "me",
                        status = "PENDING"
                    )
                )
                repository.insertFriendRequest(
                    FriendRequest(
                        senderId = "charlie",
                        senderName = "GamerCharlie",
                        senderAvatar = "img_profile_me_1783282394572",
                        receiverId = "me",
                        status = "PENDING"
                    )
                )
            }

            // Seed initial stories if empty
            val currentStories = repository.allStories.first()
            if (currentStories.isEmpty()) {
                repository.insertStory(
                    Story(
                        authorName = "Alice (Retro Gamer)",
                        authorAvatar = "img_profile_me_1783282394572",
                        textContent = "Warming up with Brick Breaker before tonight's highscore match! 🧱🙌",
                        timestamp = System.currentTimeMillis() - 3600000,
                        isViewed = false
                    )
                )
                repository.insertStory(
                    Story(
                        authorName = "SpaceQueen",
                        authorAvatar = "img_profile_me_1783282394572",
                        textContent = "Look at this amazing Space Shooter galaxy art background! 🌌🛸",
                        imageUrl = "img_retro_stream_thumbnail_1783282368689",
                        timestamp = System.currentTimeMillis() - 7200000,
                        isViewed = false
                    )
                )
                repository.insertStory(
                    Story(
                        authorName = "Bob_Destroyer",
                        authorAvatar = "img_profile_me_1783282394572",
                        textContent = "Rethinking my Tic-Tac-Toe strategies. Nobody beats the Destroyer! 😈",
                        timestamp = System.currentTimeMillis() - 10800000,
                        isViewed = true
                    )
                )
            }
        }

        // Periodic simulated comments/reactions when viewing a stream
        startSimulatedStreamActivity()
    }

    // --- STORIES OPERATIONS ---
    fun addStory(textContent: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val stats = userStats.value
            val story = Story(
                authorName = stats.username,
                authorAvatar = stats.avatarResName,
                textContent = textContent,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                isViewed = false
            )
            repository.insertStory(story)
        }
    }

    fun markStoryAsViewed(storyId: Int) {
        viewModelScope.launch {
            val currentStories = repository.allStories.first()
            val story = currentStories.find { it.id == storyId }
            if (story != null) {
                repository.updateStory(story.copy(isViewed = true))
            }
        }
    }

    // --- FRIENDS & REQUESTS OPERATIONS ---

    fun sendFriendRequest(targetUserId: String, targetUsername: String, avatar: String) {
        viewModelScope.launch {
            val request = FriendRequest(
                senderId = "me",
                senderName = userStats.value.username,
                senderAvatar = userStats.value.avatarResName,
                receiverId = targetUserId,
                status = "PENDING"
            )
            repository.insertFriendRequest(request)
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            repository.deleteFriendRequest(request.id)
            repository.insertFriend(
                Friend(
                    userId = request.senderId,
                    username = request.senderName,
                    avatarResName = request.senderAvatar,
                    globalRank = (10..500).random(),
                    wins = (5..50).random(),
                    isOnline = true,
                    presence = "online"
                )
            )

            // Send automated message in chat to start the connection
            val welcomeMsg = ChatMessage(
                chatId = request.senderId,
                senderName = "System",
                senderAvatar = "img_profile_me_1783282394572",
                content = "You are now friends with ${request.senderName}! Let's invite them to games or streams! 🎮📺",
                type = "TEXT"
            )
            repository.insertMessage(welcomeMsg)
        }
    }

    fun rejectFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            repository.deleteFriendRequest(request.id)
        }
    }

    fun removeFriend(userId: String) {
        viewModelScope.launch {
            repository.deleteFriend(userId)
        }
    }

    fun updateFriendPresence(userId: String, presence: String) {
        viewModelScope.launch {
            val friends = repository.allFriends.first()
            val friend = friends.find { it.userId == userId }
            if (friend != null) {
                val updated = friend.copy(
                    presence = presence,
                    isOnline = (presence != "offline")
                )
                repository.insertFriend(updated)
            }
        }
    }

    fun sendGameInviteToFriend(chatId: String, gameName: String) {
        viewModelScope.launch {
            val me = userStats.value
            val content = "Hey! I invite you to play **$gameName**! Click to join! 🎮"
            val message = ChatMessage(
                chatId = chatId,
                senderName = me.username,
                senderAvatar = me.avatarResName,
                content = content,
                type = "GAME_INVITE",
                gameType = if (gameName.contains("Tic-Tac-Toe", ignoreCase = true)) "TIC_TAC_TOE" else "ROCK_PAPER_SCI",
                gameStatus = "PENDING",
                gameData = ",,,,,,,,"
            )
            repository.insertMessage(message)
        }
    }

    fun sendStreamInviteToFriend(chatId: String, streamTitle: String) {
        viewModelScope.launch {
            val me = userStats.value
            val content = "Come join my live stream: **$streamTitle**! 📺"
            val message = ChatMessage(
                chatId = chatId,
                senderName = me.username,
                senderAvatar = me.avatarResName,
                content = content,
                type = "TEXT"
            )
            repository.insertMessage(message)
        }
    }

    fun transferCoinsToFriend(chatId: String, amount: Int) {
        viewModelScope.launch {
            val me = userStats.value
            if (me.coins >= amount) {
                // Deduct coins from user
                repository.insertUserStats(me.copy(coins = me.coins - amount))
                
                // Add message to chat
                val content = "Sent $amount Coins 🪙 to you!"
                val message = ChatMessage(
                    chatId = chatId,
                    senderName = me.username,
                    senderAvatar = me.avatarResName,
                    content = content,
                    type = "COINS_TRANSFER",
                    gameData = amount.toString()
                )
                repository.insertMessage(message)
                
                // Simulate receipt & reply from friend
                delay(1500)
                val replyContent = "Wow, thank you so much for the $amount Coins! 🪙 You're a legend! 💸🎮"
                val name = if (chatId == "alice") "Alice (Retro Gamer)" else if (chatId == "bob") "Bob_Destroyer" else chatId.replaceFirstChar { it.uppercase() }
                repository.insertMessage(
                    ChatMessage(
                        chatId = chatId,
                        senderName = name,
                        senderAvatar = "img_profile_me_1783282394572",
                        content = replyContent,
                        type = "TEXT"
                    )
                )
                com.example.util.NotificationHelper.showNotification(getApplication(), name, replyContent)
            }
        }
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
            } else if (type == "FILE") {
                delay(1500)
                val parts = content.split("|")
                val fileName = parts.getOrNull(0) ?: "file"
                simulateFileReply(chatId, fileName)
            } else if (type == "GAME_INVITE" && gameType == "CUSTOM_LINK") {
                delay(1800)
                val parts = gameData?.split("|") ?: emptyList()
                val gameName = parts.getOrNull(0) ?: "game"
                simulateCustomGameReply(chatId, gameName)
            }
        }
    }

    private suspend fun simulateFileReply(chatId: String, fileName: String) {
        val replyContent = "Nice! I received '$fileName'. Downloading and checking it out now! 📂✨"
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
        com.example.util.NotificationHelper.showNotification(getApplication(), name, replyContent)
    }

    private suspend fun simulateCustomGameReply(chatId: String, gameName: String) {
        val replyContent = "Awesome! A challenge on $gameName? Count me in! Clicking the invite link to join your lobby now... 🎮⚔️"
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
        com.example.util.NotificationHelper.showNotification(getApplication(), name, replyContent)
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

    fun completeSignUp(newUsername: String, accountType: String, accountValue: String) {
        viewModelScope.launch {
            val me = userStats.value
            val linkedAccount = when (accountType) {
                "PHONE" -> me.copy(authPhone = accountValue)
                "GMAIL" -> me.copy(authGmail = accountValue)
                "FACEBOOK" -> me.copy(authFacebook = accountValue)
                "X" -> me.copy(authX = accountValue)
                else -> me
            }
            repository.insertUserStats(linkedAccount.copy(username = newUsername))
        }
    }

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
                backupInterval = interval,
                backupDestination = destination
            ))
            syncWithFirebase()
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

    fun reactToMessage(messageId: Int, emoji: String, user: String = "WeGamer_Me") {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            val currentReactions = message.reactionsJson
            val updatedReactions = try {
                val reactionsList = mutableListOf<Pair<String, String>>()
                if (currentReactions.startsWith("[") && currentReactions.endsWith("]")) {
                    val raw = currentReactions.removeSurrounding("[", "]").trim()
                    if (raw.isNotEmpty()) {
                        val items = raw.split("},{")
                        for (item in items) {
                            val cleanItem = item.replace("{", "").replace("}", "")
                            val parts = cleanItem.split(",")
                            var u = ""
                            var em = ""
                            for (part in parts) {
                                val kv = part.split(":")
                                if (kv.size == 2) {
                                    val k = kv[0].trim().removeSurrounding("\"")
                                    val v = kv[1].trim().removeSurrounding("\"")
                                    if (k == "user") u = v
                                    if (k == "emoji") em = v
                                }
                            }
                            if (u.isNotEmpty() && em.isNotEmpty()) {
                                reactionsList.add(u to em)
                            }
                        }
                    }
                }
                val existingIndex = reactionsList.indexOfFirst { it.first == user }
                if (existingIndex >= 0) {
                    if (reactionsList[existingIndex].second == emoji) {
                        reactionsList.removeAt(existingIndex)
                    } else {
                        reactionsList[existingIndex] = Pair(user, emoji)
                    }
                } else {
                    reactionsList.add(Pair(user, emoji))
                }
                reactionsList.joinToString(prefix = "[", postfix = "]") {
                    "{\"user\":\"${it.first}\",\"emoji\":\"${it.second}\"}"
                }
            } catch (e: Exception) {
                "[{\"user\":\"$user\",\"emoji\":\"$emoji\"}]"
            }
            repository.updateMessage(message.copy(reactionsJson = updatedReactions))
        }
    }

    fun upgradeToPremium(tier: String) {
        viewModelScope.launch {
            val me = userStats.value
            val limit = when (tier) {
                "SILVER" -> 50L * 1024 * 1024 * 1024
                "GOLD" -> 100L * 1024 * 1024 * 1024
                else -> 15L * 1024 * 1024 * 1024
            }
            repository.insertUserStats(me.copy(
                isPremium = tier != "FREE",
                premiumTier = tier,
                storageLimitBytes = limit
            ))
        }
    }

    fun awardCoins(amount: Int) {
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(me.copy(coins = me.coins + amount))
        }
    }

    fun performDailyCheckIn() {
        viewModelScope.launch {
            val me = userStats.value
            val now = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000L
            val lastCheckIn = me.lastCheckInTime
            
            var currentStreak = me.dailyStreak
            if (lastCheckIn > 0) {
                val diff = now - lastCheckIn
                if (diff in oneDayMillis..(oneDayMillis * 2)) {
                    currentStreak += 1
                } else if (diff > oneDayMillis * 2) {
                    currentStreak = 1
                } else {
                    // Already checked in today
                    return@launch
                }
            } else {
                currentStreak = 1
            }
            
            val multiplier = minOf(5, currentStreak)
            val rewardCoins = 100 * multiplier
            
            repository.insertUserStats(
                me.copy(
                    dailyStreak = currentStreak,
                    lastCheckInTime = now,
                    coins = me.coins + rewardCoins
                )
            )
        }
    }

    fun enterReferralCode(code: String): Boolean {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isEmpty() || cleanCode == userStats.value.referralCode) {
            return false
        }
        viewModelScope.launch {
            val me = userStats.value
            repository.insertUserStats(
                me.copy(
                    referredByCode = cleanCode,
                    coins = me.coins + 500,
                    unlockedBorder = "GOLDEN"
                )
            )
            val referralMsg = ChatMessage(
                chatId = "alice",
                senderName = "System",
                senderAvatar = "img_profile_me_1783282394572",
                content = "Referral code '$cleanCode' activated! 🎉 Both of you earned 500 coins and you unlocked the Legendary Golden Profile Border! 🏆🟡",
                type = "TEXT"
            )
            repository.insertMessage(referralMsg)
        }
        return true
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                // Safe fallback
            }
            val me = userStats.value
            repository.insertUserStats(me.copy(authPhone = "", authGmail = "", authFacebook = ""))
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()
            } catch (e: Exception) {
                // Safe fallback
            }
            val me = userStats.value
            repository.insertUserStats(me.copy(authPhone = "", authGmail = "", authFacebook = "", username = "Gamer"))
        }
    }

    fun shareBragPost(gameTitle: String, scoreOrResult: String) {
        viewModelScope.launch {
            val me = userStats.value
            val content = "Just achieved an epic milestone! ⚔️ I conquered $gameTitle with a score/result of $scoreOrResult! Can anyone beat me? 🎮🏆 #WeGistVibe #RetroArcade"
            val newPost = Post(
                authorName = me.username,
                authorAvatar = me.avatarResName,
                content = content,
                imageUrl = "img_retro_stream_thumbnail_1783282368689",
                timestamp = System.currentTimeMillis()
            )
            repository.insertPost(newPost)
            awardCoins(50)
        }
    }

    fun claimQuest(questId: String, xpAward: Int, coinAward: Int) {
        viewModelScope.launch {
            val me = userStats.value
            val currentCompleted = try {
                val list = me.questsCompletedJson.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotEmpty() }
                    .toMutableList()
                if (!list.contains(questId)) {
                    list.add(questId)
                }
                list
            } catch (e: Exception) {
                mutableListOf(questId)
            }

            val newJson = currentCompleted.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
            
            var newXp = me.xp + xpAward
            var newLevel = me.level
            if (newXp >= 1000) {
                newXp -= 1000
                newLevel += 1
            }

            repository.insertUserStats(
                me.copy(
                    xp = newXp,
                    level = newLevel,
                    coins = me.coins + coinAward,
                    questsCompletedJson = newJson
                )
            )
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

    fun syncContacts() {
        viewModelScope.launch {
            isSyncingContacts.value = true
            contactsSyncStatus.value = "Scanning phone contacts..."
            delay(1200)

            val context = getApplication<Application>().applicationContext
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val deviceContacts = mutableListOf<Pair<String, String>>()

            if (hasPermission) {
                try {
                    val resolver = context.contentResolver
                    val cursor = resolver.query(
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        null
                    )
                    cursor?.use { c ->
                        val nameIndex = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (nameIndex != -1 && numberIndex != -1) {
                            while (c.moveToNext()) {
                                val dName = c.getString(nameIndex) ?: ""
                                val dNum = c.getString(numberIndex) ?: ""
                                if (dName.isNotEmpty() && dNum.isNotEmpty()) {
                                    deviceContacts.add(dName to dNum)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback simulated contacts for emulator, previews, and testing
            if (deviceContacts.isEmpty()) {
                deviceContacts.add("Charlie Gamer" to "+15551234567")
                deviceContacts.add("Retro Queen" to "+15559876543")
                deviceContacts.add("Shadow Hunter" to "+15555554444")
                deviceContacts.add("Gamer Mom" to "+15553331111")
                deviceContacts.add("NiteHawk" to "+14155552671")
            }

            // Identify existing WeGist users and sync them
            val discoveredUsers = listOf(
                Friend("charlie", "GamerCharlie", "img_profile_me_1783282394572", 84, 15, true, "online"),
                Friend("spacequeen", "SpaceQueen", "img_profile_me_1783282394572", 15, 68, true, "in-game"),
                Friend("nitehawk", "NiteHawk_99", "img_profile_me_1783282394572", 204, 8, false, "offline")
            )

            var count = 0
            val currentFriends = repository.allFriends.first()
            for (friend in discoveredUsers) {
                if (!currentFriends.any { it.userId == friend.userId }) {
                    repository.insertFriend(friend)
                    count++
                }
            }

            contactsSyncStatus.value = "Synced ${deviceContacts.size} contacts! Discovered $count new gaming friends on WeGist."
            isSyncingContacts.value = false
            delay(3500)
            contactsSyncStatus.value = null
        }
    }

    fun syncWithFirebase() {
        viewModelScope.launch {
            isSyncingWithFirebase.value = true
            firebaseSyncStatus.value = "Connecting to Firebase Realtime Database..."
            delay(1200)
            firebaseSyncStatus.value = "Uploading offline SQLite messages..."
            delay(1000)
            firebaseSyncStatus.value = "Synchronizing user profile metadata..."
            delay(800)

            val me = userStats.value
            repository.insertUserStats(me.copy(lastBackupTime = System.currentTimeMillis()))

            firebaseSyncStatus.value = "Firebase sync complete! ✓ Local Room SQLite & Firebase are in perfect harmony."
            isSyncingWithFirebase.value = false
            delay(3500)
            firebaseSyncStatus.value = null
        }
    }

    // --- LFG LOBBIES MANAGEMENT ---

    fun createLfgLobby(gameName: String, title: String, maxPlayers: Int) {
        val me = userStats.value.username
        val newLobby = LfgLobby(
            gameName = gameName,
            title = title,
            hostName = me,
            maxPlayers = maxPlayers,
            joinedPlayers = listOf(me),
            voiceActive = true
        )
        lfgLobbies.value = listOf(newLobby) + lfgLobbies.value

        // Post custom lobby invite in active chat if open
        viewModelScope.launch {
            val lobbyMsg = "📢 LFG LOBBY ACTIVE: **$gameName: $title** [Joined: 1/$maxPlayers]"
            activeChatId.value?.let { currentChatId ->
                sendMessage(
                    chatId = currentChatId,
                    content = lobbyMsg,
                    type = "GAME_INVITE",
                    gameType = "CUSTOM_LINK",
                    gameData = "$gameName Group Lobby|https://lobby.wegist.com/${newLobby.id}|Click below to join my gaming group instantly! ⚔️"
                )
            }
        }
    }

    fun joinLfgLobby(lobbyId: String) {
        val me = userStats.value.username
        lfgLobbies.value = lfgLobbies.value.map { lobby ->
            if (lobby.id == lobbyId) {
                if (!lobby.joinedPlayers.contains(me) && lobby.joinedPlayers.size < lobby.maxPlayers) {
                    lobby.copy(joinedPlayers = lobby.joinedPlayers + me)
                } else {
                    lobby
                }
            } else {
                lobby
            }
        }
    }

    // --- TURN-BASED MINI-GAMES PLAY LOGIC ---

    fun playTriviaAnswer(messageId: Int, selectedOptionIdx: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            if (message.gameStatus == "COMPLETED") return@launch

            val qIdx = message.id % 3
            val (question, options, correctIdx) = when (qIdx) {
                0 -> Triple(
                    "Which video game is officially the best-selling of all time?",
                    listOf("Tetris", "Grand Theft Auto V", "Minecraft"),
                    2
                )
                1 -> Triple(
                    "In what year was the original Nintendo Entertainment System (NES) released?",
                    listOf("1983", "1985", "1989"),
                    1
                )
                else -> Triple(
                    "What is the name of the main protagonist in the Halo franchise?",
                    listOf("Doomguy", "Master Chief", "Marcus Fenix"),
                    1
                )
            }

            val isCorrect = selectedOptionIdx == correctIdx
            val chosenOptionText = options.getOrElse(selectedOptionIdx) { "Unknown" }
            val correctOptionText = options.getOrElse(correctIdx) { "Unknown" }

            val resultText = if (isCorrect) {
                "Solved Trivia! Question: \"$question\"\nMy Answer: **$chosenOptionText** (CORRECT! 🎉 +50 🪙)"
            } else {
                "Failed Trivia! Question: \"$question\"\nMy Answer: **$chosenOptionText** (WRONG! Correct was $correctOptionText 🤖)"
            }

            val updatedMsg = message.copy(
                gameStatus = "COMPLETED",
                gameWinner = if (isCorrect) "me" else "opponent",
                gameData = "$selectedOptionIdx,$correctIdx",
                content = resultText
            )
            repository.updateMessage(updatedMsg)

            if (isCorrect) {
                awardCoins(50)
            }
        }
    }

    fun playChessMove(messageId: Int, fromIdx: Int, toIdx: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            val board = message.gameData?.split(",")?.toMutableList() ?: mutableListOf()
            if (board.size != 16 || message.gameStatus == "COMPLETED") return@launch

            val piece = board[fromIdx]
            board[fromIdx] = "."
            board[toIdx] = piece

            val updatedBoardData = board.joinToString(",")
            val updatedMsg = message.copy(
                gameData = updatedBoardData,
                gameStatus = "ACTIVE",
                content = "I made a chess move! ${piece} from square $fromIdx to $toIdx. Your turn!"
            )
            repository.updateMessage(updatedMsg)

            // Trigger AI response after delay
            delay(1500)
            triggerChessAIMove(updatedMsg, board)
        }
    }

    private suspend fun triggerChessAIMove(message: ChatMessage, board: MutableList<String>) {
        val blackPieces = listOf("♜", "♞", "♝", "♚")
        val blackIndices = board.indices.filter { board[it] in blackPieces }
        if (blackIndices.isNotEmpty()) {
            val fromIdx = blackIndices.random()
            val piece = board[fromIdx]
            val emptyOrWhiteIndices = board.indices.filter { board[it] == "." || board[it] in listOf("♖", "♘", "♗", "♔") }
            if (emptyOrWhiteIndices.isNotEmpty()) {
                val toIdx = emptyOrWhiteIndices.random()
                val captured = board[toIdx]
                board[fromIdx] = "."
                board[toIdx] = piece

                val updatedBoardData = board.joinToString(",")
                val text = if (captured != ".") {
                    "Alice captured your ${captured} with ${piece} at $toIdx! ⚔️ Your turn!"
                } else {
                    "Alice moved her chess piece ${piece} to square $toIdx. Your turn!"
                }

                val updatedMsg = message.copy(
                    gameData = updatedBoardData,
                    content = text
                )
                repository.updateMessage(updatedMsg)
            }
        }
    }

    fun playConnectFourMove(messageId: Int, col: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            val board = message.gameData?.split(",")?.toMutableList() ?: MutableList(25) { "" }
            if (message.gameStatus == "COMPLETED") return@launch

            // Find lowest empty slot in this column (row goes from 4 down to 0)
            var targetRow = -1
            for (r in 4 downTo 0) {
                val idx = r * 5 + col
                if (board[idx].isEmpty()) {
                    targetRow = r
                    break
                }
            }

            if (targetRow != -1) {
                val targetIdx = targetRow * 5 + col
                board[targetIdx] = "R" // Red (Me)
                val updatedBoardData = board.joinToString(",")

                val isUserWin = checkConnectFourWin(board, "R")
                val isDraw = !board.contains("") && !isUserWin

                val status = if (isUserWin || isDraw) "COMPLETED" else "ACTIVE"
                val winner = if (isUserWin) "me" else if (isDraw) "DRAW" else null

                val updatedMsg = message.copy(
                    gameStatus = status,
                    gameWinner = winner,
                    gameData = updatedBoardData,
                    content = if (isUserWin) "I connected four in a row! 🎉" else if (isDraw) "The Connect Four game is a draw!" else "I dropped a chip in col ${col + 1}. Your turn!"
                )
                repository.updateMessage(updatedMsg)

                if (isUserWin) {
                    awardCoins(120)
                    incrementWins()
                }

                if (status == "ACTIVE") {
                    delay(1500)
                    triggerConnectFourAIMove(updatedMsg, board)
                }
            }
        }
    }

    private suspend fun triggerConnectFourAIMove(message: ChatMessage, board: MutableList<String>) {
        val availableCols = (0..4).filter { col -> (0..4).any { r -> board[r * 5 + col].isEmpty() } }
        if (availableCols.isNotEmpty()) {
            val aiCol = availableCols.random()
            var targetRow = -1
            for (r in 4 downTo 0) {
                val idx = r * 5 + aiCol
                if (board[idx].isEmpty()) {
                    targetRow = r
                    break
                }
            }
            if (targetRow != -1) {
                val targetIdx = targetRow * 5 + aiCol
                board[targetIdx] = "Y" // Yellow (AI)
                val updatedBoardData = board.joinToString(",")

                val isAIWin = checkConnectFourWin(board, "Y")
                val isDraw = !board.contains("") && !isAIWin

                val status = if (isAIWin || isDraw) "COMPLETED" else "ACTIVE"
                val winner = if (isAIWin) "opponent" else if (isDraw) "DRAW" else null

                val updatedMsg = message.copy(
                    gameStatus = status,
                    gameWinner = winner,
                    gameData = updatedBoardData,
                    content = if (isAIWin) "Alice connected four in a row! 🤖" else if (isDraw) "The Connect Four game is a draw!" else "I made my move! Column ${aiCol + 1}."
                )
                repository.updateMessage(updatedMsg)
            }
        }
    }

    private fun checkConnectFourWin(board: List<String>, player: String): Boolean {
        // Horizontal
        for (row in 0..4) {
            for (col in 0..1) {
                val base = row * 5 + col
                if (board[base] == player && board[base+1] == player && board[base+2] == player && board[base+3] == player) return true
            }
        }
        // Vertical
        for (col in 0..4) {
            for (row in 0..1) {
                val base = row * 5 + col
                if (board[base] == player && board[base+5] == player && board[base+10] == player && board[base+15] == player) return true
            }
        }
        // Diagonal down-right
        for (row in 0..1) {
            for (col in 0..1) {
                val base = row * 5 + col
                if (board[base] == player && board[base+6] == player && board[base+12] == player && board[base+18] == player) return true
            }
        }
        // Diagonal down-left
        for (row in 0..1) {
            for (col in 2..4) {
                val base = row * 5 + col
                if (board[base] == player && board[base+4] == player && board[base+8] == player && board[base+12] == player) return true
            }
        }
        return false
    }

    fun playBattleshipMove(messageId: Int, cellIdx: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            if (message.gameStatus == "COMPLETED") return@launch

            val parts = message.gameData?.split("|")?.toMutableList() ?: return@launch
            if (parts.size < 4) return@launch

            val playerShots = parts[0].split(",").toMutableList()
            val aiShips = parts[1].split(",")
            val aiShots = parts[2].split(",").toMutableList()
            val playerShips = parts[3].split(",")

            if (playerShots[cellIdx] != ".") return@launch // Already shot here

            val isHit = aiShips.contains(cellIdx.toString())
            playerShots[cellIdx] = if (isHit) "H" else "M"

            val totalPlayerHits = playerShots.count { it == "H" }
            val isUserWin = totalPlayerHits >= aiShips.size

            parts[0] = playerShots.joinToString(",")

            val status = if (isUserWin) "COMPLETED" else "ACTIVE"
            val winner = if (isUserWin) "me" else null

            val resultText = if (isUserWin) {
                "I sank all AI ships and won the Battleship battle! 🎉 (+150 🪙)"
            } else if (isHit) {
                "Hit! I found a ship at sector $cellIdx! 💥 AI's turn..."
            } else {
                "Miss at sector $cellIdx. 💨 AI's turn..."
            }

            val updatedMsg = message.copy(
                gameStatus = status,
                gameWinner = winner,
                gameData = parts.joinToString("|"),
                content = resultText
            )
            repository.updateMessage(updatedMsg)

            if (isUserWin) {
                awardCoins(150)
                incrementWins()
            }

            if (status == "ACTIVE") {
                delay(1500)
                triggerBattleshipAIMove(updatedMsg, parts)
            }
        }
    }

    private suspend fun triggerBattleshipAIMove(message: ChatMessage, parts: MutableList<String>) {
        val aiShots = parts[2].split(",").toMutableList()
        val playerShips = parts[3].split(",")

        val availableTargetIndices = aiShots.indices.filter { aiShots[it] == "." }
        if (availableTargetIndices.isNotEmpty()) {
            val target = availableTargetIndices.random()
            val isHit = playerShips.contains(target.toString())
            aiShots[target] = if (isHit) "H" else "M"

            val totalAiHits = aiShots.count { it == "H" }
            val isAiWin = totalAiHits >= playerShips.size

            parts[2] = aiShots.joinToString(",")

            val status = if (isAiWin) "COMPLETED" else "ACTIVE"
            val winner = if (isAiWin) "opponent" else null

            val resultText = if (isAiWin) {
                "Alice sank all your ships! 🤖 You lost!"
            } else if (isHit) {
                "Alice launched an attack and HIT your ship at sector $target! 🔥 Your turn!"
            } else {
                "Alice missed her attack at sector $target. 💨 Your turn!"
            }

            val updatedMsg = message.copy(
                gameStatus = status,
                gameWinner = winner,
                gameData = parts.joinToString("|"),
                content = resultText
            )
            repository.updateMessage(updatedMsg)
        }
    }

    fun playDiceDuelMove(messageId: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            if (message.gameStatus == "COMPLETED") return@launch

            val dataParts = message.gameData?.split(",")?.map { it.toIntOrNull() ?: 0 } ?: listOf(0, 0, 0, 0, 0)
            var (userScore, oppScore, round, lastUser, lastOpp) = dataParts

            val userRoll = (1..6).random() + (1..6).random()
            userScore += userRoll
            round += 1

            // AI rolls right after
            val oppRoll = (1..6).random() + (1..6).random()
            oppScore += oppRoll

            val isLastRound = round >= 3
            val status = if (isLastRound) "COMPLETED" else "ACTIVE"
            val winner = if (isLastRound) {
                if (userScore > oppScore) "me" else if (oppScore > userScore) "opponent" else "DRAW"
            } else null

            val resultText = if (isLastRound) {
                if (userScore > oppScore) {
                    "Dice Duel over! My Roll: $userRoll, Alice's Roll: $oppRoll.\nFinal Score: **$userScore - $oppScore**. I WON! 🏆"
                } else if (oppScore > userScore) {
                    "Dice Duel over! My Roll: $userRoll, Alice's Roll: $oppRoll.\nFinal Score: **$userScore - $oppScore**. Alice won! 🤖"
                } else {
                    "Dice Duel over! My Roll: $userRoll, Alice's Roll: $oppRoll.\nFinal Score: **$userScore - $oppScore**. It's a DRAW! 🤝"
                }
            } else {
                "Round $round: I rolled $userRoll. Alice rolled $oppRoll.\nRunning Score: $userScore - $oppScore! Your turn to roll again!"
            }

            val updatedMsg = message.copy(
                gameStatus = status,
                gameWinner = winner,
                gameData = "$userScore,$oppScore,$round,$userRoll,$oppRoll",
                content = resultText
            )
            repository.updateMessage(updatedMsg)

            if (isLastRound && winner == "me") {
                awardCoins(100)
                incrementWins()
            }
        }
    }

    fun playMemoryMatchMove(messageId: Int, cardIdx: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            if (message.gameStatus == "COMPLETED") return@launch

            val parts = message.gameData?.split("|")?.toMutableList() ?: return@launch
            if (parts.size < 5) return@launch

            val cardsLayout = parts[0].split(",")
            val matchedArray = parts[1].split(",").toMutableList()
            var firstSel = parts[2].toIntOrNull() ?: -1
            var playerScore = parts[3].toIntOrNull() ?: 0
            var oppScore = parts[4].toIntOrNull() ?: 0

            if (matchedArray[cardIdx] == "M" || cardIdx == firstSel) return@launch

            if (firstSel == -1) {
                // First card selected
                firstSel = cardIdx
                parts[2] = firstSel.toString()

                val updatedMsg = message.copy(
                    gameData = parts.joinToString("|"),
                    content = "Flipped first card: ${cardsLayout[cardIdx]}! Flip second..."
                )
                repository.updateMessage(updatedMsg)
            } else {
                // Second card selected
                val firstCardVal = cardsLayout[firstSel]
                val secondCardVal = cardsLayout[cardIdx]

                if (firstCardVal == secondCardVal) {
                    // Match!
                    matchedArray[firstSel] = "M"
                    matchedArray[cardIdx] = "M"
                    playerScore += 1
                    firstSel = -1

                    val totalMatched = matchedArray.count { it == "M" }
                    val isGameOver = totalMatched >= 16
                    val status = if (isGameOver) "COMPLETED" else "ACTIVE"
                    val winner = if (isGameOver) {
                        if (playerScore > oppScore) "me" else if (oppScore > playerScore) "opponent" else "DRAW"
                    } else null

                    parts[1] = matchedArray.joinToString(",")
                    parts[2] = "-1"
                    parts[3] = playerScore.toString()

                    val resultText = if (isGameOver) {
                        "Memory Card match game completed! Matches Score: **$playerScore - $oppScore**. ${if (playerScore > oppScore) "I won! 🎉" else "Alice won! 🤖"}"
                    } else {
                        "Matched! Found a pair of $firstCardVal! Your score: $playerScore. Keep matching! 🧠"
                    }

                    val updatedMsg = message.copy(
                        gameStatus = status,
                        gameWinner = winner,
                        gameData = parts.joinToString("|"),
                        content = resultText
                    )
                    repository.updateMessage(updatedMsg)

                    if (isGameOver && winner == "me") {
                        awardCoins(120)
                        incrementWins()
                    }
                } else {
                    // Mismatch!
                    val wrongFirst = firstSel
                    val wrongSecond = cardIdx

                    // Display mismatch briefly then let AI play
                    parts[2] = "-1" // Reset selection

                    val updatedMsg = message.copy(
                        gameData = parts.joinToString("|"),
                        content = "Oops! ${cardsLayout[wrongFirst]} doesn't match ${cardsLayout[wrongSecond]}. AI's turn!"
                    )
                    repository.updateMessage(updatedMsg)

                    // Delay and AI turn
                    delay(1500)
                    triggerMemoryAIMove(updatedMsg, parts)
                }
            }
        }
    }

    private suspend fun triggerMemoryAIMove(message: ChatMessage, parts: MutableList<String>) {
        val cardsLayout = parts[0].split(",")
        val matchedArray = parts[1].split(",").toMutableList()
        var oppScore = parts[4].toIntOrNull() ?: 0
        val playerScore = parts[3].toIntOrNull() ?: 0

        val unmatchedIndices = matchedArray.indices.filter { matchedArray[it] == "." }
        if (unmatchedIndices.size >= 2) {
            // AI is smart: 40% chance of finding a pair, otherwise random selection
            var firstIdx = -1
            var secondIdx = -1

            if (Math.random() < 0.4) {
                // Find a match in available cards
                outer@ for (i in unmatchedIndices) {
                    for (j in unmatchedIndices) {
                        if (i != j && cardsLayout[i] == cardsLayout[j]) {
                            firstIdx = i
                            secondIdx = j
                            break@outer
                        }
                    }
                }
            }

            if (firstIdx == -1) {
                // Pick random
                val shuffled = unmatchedIndices.shuffled()
                firstIdx = shuffled[0]
                secondIdx = shuffled[1]
            }

            val firstCardVal = cardsLayout[firstIdx]
            val secondCardVal = cardsLayout[secondIdx]

            val text: String
            if (firstCardVal == secondCardVal) {
                matchedArray[firstIdx] = "M"
                matchedArray[secondIdx] = "M"
                oppScore += 1
                parts[1] = matchedArray.joinToString(",")
                parts[4] = oppScore.toString()

                val totalMatched = matchedArray.count { it == "M" }
                val isGameOver = totalMatched >= 16
                val status = if (isGameOver) "COMPLETED" else "ACTIVE"
                val winner = if (isGameOver) {
                    if (playerScore > oppScore) "me" else if (oppScore > playerScore) "opponent" else "DRAW"
                } else null

                text = if (isGameOver) {
                    "Alice matched $firstCardVal! Game completed. Matches: **$playerScore - $oppScore**. ${if (playerScore > oppScore) "I won! 🎉" else "Alice won! 🤖"}"
                } else {
                    "Alice matched $firstCardVal and $secondCardVal! Score: $playerScore - $oppScore. Your turn!"
                }

                val updatedMsg = message.copy(
                    gameStatus = status,
                    gameWinner = winner,
                    gameData = parts.joinToString("|"),
                    content = text
                )
                repository.updateMessage(updatedMsg)
            } else {
                text = "Alice tried flipping cards at $firstIdx and $secondIdx but they didn't match. Your turn!"
                val updatedMsg = message.copy(
                    gameData = parts.joinToString("|"),
                    content = text
                )
                repository.updateMessage(updatedMsg)
            }
        }
    }

    fun playNumberGuesserMove(messageId: Int, guess: Int) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            if (message.gameStatus == "COMPLETED") return@launch

            val parts = message.gameData?.split(",") ?: return@launch
            val secretVal = parts.getOrNull(0)?.toIntOrNull() ?: 25
            var attempts = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val maxAttempts = parts.getOrNull(2)?.toIntOrNull() ?: 6

            attempts += 1
            val isCorrect = guess == secretVal
            val isOutOfAttempts = attempts >= maxAttempts

            val feedback = when {
                isCorrect -> "Correct! You guessed it in $attempts attempts!"
                guess < secretVal -> "Guess $guess is Too Low! 📉"
                else -> "Guess $guess is Too High! 📈"
            }

            val status = if (isCorrect || isOutOfAttempts) "COMPLETED" else "ACTIVE"
            val winner = if (isCorrect) "me" else if (isOutOfAttempts) "opponent" else null

            val resultText = if (isCorrect) {
                "I solved the Guess the Number! The secret was **$secretVal**! 🎉 (+100 🪙)"
            } else if (isOutOfAttempts) {
                "Whoops, out of attempts! The correct number was **$secretVal**. Alice wins! 🤖"
            } else {
                "I guessed $guess. Feedback: $feedback. ${maxAttempts - attempts} attempts left!"
            }

            val updatedMsg = message.copy(
                gameStatus = status,
                gameWinner = winner,
                gameData = "$secretVal,$attempts,$maxAttempts,$feedback",
                content = resultText
            )
            repository.updateMessage(updatedMsg)

            if (isCorrect) {
                awardCoins(100)
                incrementWins()
            }
        }
    }

    fun playBlackjackMove(messageId: Int, action: String) {
        viewModelScope.launch {
            val messagesList = allMessages.value
            val message = messagesList.find { it.id == messageId } ?: return@launch
            if (message.gameStatus == "COMPLETED") return@launch

            val parts = message.gameData?.split("|")?.toMutableList() ?: return@launch
            val playerHand = parts[0].split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()
            val oppHand = parts[1].split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()

            if (action == "HIT") {
                val newCard = (1..11).random()
                playerHand.add(newCard)
                val playerScore = calculateBlackjackScore(playerHand)

                parts[0] = playerHand.joinToString(",")

                if (playerScore > 21) {
                    // Bust!
                    val updatedMsg = message.copy(
                        gameStatus = "COMPLETED",
                        gameWinner = "opponent",
                        gameData = parts.joinToString("|"),
                        content = "Bust! Drew a $newCard, Total Score is $playerScore. Alice wins! 🤖"
                    )
                    repository.updateMessage(updatedMsg)
                } else {
                    val updatedMsg = message.copy(
                        gameData = parts.joinToString("|"),
                        content = "You drew a $newCard. Your Hand: ${playerHand.joinToString(", ")} (Score: $playerScore). Hit or Stand?"
                    )
                    repository.updateMessage(updatedMsg)
                }
            } else if (action == "STAND") {
                // Dealer/Opponent hits until 17+
                var oppScore = calculateBlackjackScore(oppHand)
                while (oppScore < 17) {
                    val newCard = (1..11).random()
                    oppHand.add(newCard)
                    oppScore = calculateBlackjackScore(oppHand)
                }

                parts[1] = oppHand.joinToString(",")
                val playerScore = calculateBlackjackScore(playerHand)

                val winner = when {
                    oppScore > 21 -> "me"
                    playerScore > oppScore -> "me"
                    oppScore > playerScore -> "opponent"
                    else -> "DRAW"
                }

                val status = "COMPLETED"
                val resultText = when (winner) {
                    "me" -> "You Win! Your score: $playerScore, Alice's score: $oppScore ${if (oppScore > 21) "(Busted)" else ""}. 🎉 (+120 🪙)"
                    "opponent" -> "Alice wins! Your score: $playerScore, Alice's score: $oppScore. 🤖"
                    else -> "Dealer/Player Push! Both got $playerScore. 🤝"
                }

                val updatedMsg = message.copy(
                    gameStatus = status,
                    gameWinner = winner,
                    gameData = parts.joinToString("|"),
                    content = resultText
                )
                repository.updateMessage(updatedMsg)

                if (winner == "me") {
                    awardCoins(120)
                    incrementWins()
                }
            }
        }
    }

    private fun calculateBlackjackScore(hand: List<Int>): Int {
        var score = hand.sum()
        var aces = hand.count { it == 11 }
        while (score > 21 && aces > 0) {
            score -= 10
            aces -= 1
        }
        return score
    }
}

// Data definitions for Live Interaction UI
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
