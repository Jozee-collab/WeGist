package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.games.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * App-wide ViewModel for the WeGist demo.
 *
 * NOTE: WeGist is a local-first demo. Everything that looks like network
 * activity (opponent replies, stream viewers, friend discovery, cloud backup)
 * is SIMULATED locally in this build — see the individual functions marked
 * "SIMULATED" below. In-chat game rules live in the pure [com.example.games]
 * engines; this class only persists results and awards coins/XP.
 */
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

    // LFG Lobbies list (SIMULATED: demo lobbies, local only)
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
        // Seed demo data if the local database is empty
        viewModelScope.launch {
            DatabaseSeeder.seedIfEmpty(repository)
        }

        // SIMULATED: periodic fake comments/reactions when viewing a stream
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
                status = GameStatuses.PENDING
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
                    presence = Presence.ONLINE
                )
            )

            // Send automated message in chat to start the connection
            val welcomeMsg = ChatMessage(
                chatId = request.senderId,
                senderName = "System",
                senderAvatar = "img_profile_me_1783282394572",
                content = "You are now friends with ${request.senderName}! Let's invite them to games or streams! 🎮📺",
                type = ChatTypes.TEXT
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
                    isOnline = (presence != Presence.OFFLINE)
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
                type = ChatTypes.GAME_INVITE,
                gameType = if (gameName.contains("Tic-Tac-Toe", ignoreCase = true)) GameTypes.TIC_TAC_TOE else GameTypes.ROCK_PAPER_SCI,
                gameStatus = GameStatuses.PENDING,
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
                type = ChatTypes.TEXT
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
                    type = ChatTypes.COINS_TRANSFER,
                    gameData = amount.toString()
                )
                repository.insertMessage(message)

                // SIMULATED: receipt & reply from friend
                delay(1500)
                val replyContent = "Wow, thank you so much for the $amount Coins! 🪙 You're a legend! 💸🎮"
                val name = if (chatId == "alice") "Alice (Retro Gamer)" else if (chatId == "bob") "Bob_Destroyer" else chatId.replaceFirstChar { it.uppercase() }
                repository.insertMessage(
                    ChatMessage(
                        chatId = chatId,
                        senderName = name,
                        senderAvatar = "img_profile_me_1783282394572",
                        content = replyContent,
                        type = ChatTypes.TEXT
                    )
                )
                com.example.util.NotificationHelper.showNotification(getApplication(), name, replyContent)
            }
        }
    }

    // --- MESSAGING ACTIONS ---

    fun sendMessage(chatId: String, content: String, type: String = ChatTypes.TEXT, gameType: String? = null, gameData: String? = null) {
        viewModelScope.launch {
            val me = userStats.value
            val message = ChatMessage(
                chatId = chatId,
                senderName = me.username,
                senderAvatar = me.avatarResName,
                content = content,
                type = type,
                gameType = gameType,
                gameStatus = if (gameType != null) GameStatuses.PENDING else null,
                gameData = gameData
            )
            repository.insertMessage(message)

            // SIMULATED: scripted "AI opponent" replies for plain text, files and
            // custom game links. There is no real network peer in this demo.
            if (type == ChatTypes.TEXT) {
                delay(1200)
                simulateOpponentReply(chatId, content)
            } else if (type == ChatTypes.FILE) {
                delay(1500)
                val parts = content.split("|")
                val fileName = parts.getOrNull(0) ?: "file"
                simulateFileReply(chatId, fileName)
            } else if (type == ChatTypes.GAME_INVITE && gameType == GameTypes.CUSTOM_LINK) {
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
                type = ChatTypes.TEXT
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
                type = ChatTypes.TEXT
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
                type = ChatTypes.TEXT
            )
        )
        // Fire local notification
        com.example.util.NotificationHelper.showNotification(getApplication(), name, replyContent)
    }

    // --- IN-CHAT TURN-BASED GAMES ---
    // All rules live in the pure com.example.games engines; the ViewModel only
    // persists the resulting message state and awards coins / wins.

    /** Merges an engine result into a ChatMessage. Null fields stay unchanged. */
    private fun applyGameUpdate(message: ChatMessage, update: GameUpdate): ChatMessage = message.copy(
        gameStatus = update.gameStatus ?: message.gameStatus,
        gameWinner = update.gameWinner ?: message.gameWinner,
        gameData = update.gameData,
        content = update.content
    )

    private suspend fun runAiFollowUp(afterMessage: ChatMessage, aiUpdate: GameUpdate?) {
        if (aiUpdate != null) {
            repository.updateMessage(applyGameUpdate(afterMessage, aiUpdate))
        }
    }

    fun playTicTacToeMove(messageId: Int, index: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = TicTacToeEngine.applyPlayerMove(message.gameData, message.gameStatus, index) ?: return@launch
            val updatedMsg = applyGameUpdate(message, update)
            repository.updateMessage(updatedMsg)

            if (update.gameWinner == Winners.ME) {
                awardCoins(100)
                incrementWins()
            }

            if (update.gameStatus == GameStatuses.ACTIVE) {
                delay(1500)
                runAiFollowUp(updatedMsg, TicTacToeEngine.applyAiMove(updatedMsg.gameData))
            }
        }
    }

    fun playRockPaperScissors(messageId: Int, userChoice: String) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = RockPaperScissorsEngine.play(userChoice)
            repository.updateMessage(applyGameUpdate(message, update))

            if (update.gameWinner == Winners.ME) {
                awardCoins(80)
                incrementWins()
            }
        }
    }

    fun playTriviaAnswer(messageId: Int, selectedOptionIdx: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = TriviaEngine.answer(messageId, message.gameStatus, selectedOptionIdx) ?: return@launch
            repository.updateMessage(applyGameUpdate(message, update))

            if (update.gameWinner == Winners.ME) {
                awardCoins(50)
            }
        }
    }

    fun playChessMove(messageId: Int, fromIdx: Int, toIdx: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = ChessEngine.applyPlayerMove(message.gameData, message.gameStatus, fromIdx, toIdx) ?: return@launch
            val updatedMsg = applyGameUpdate(message, update)
            repository.updateMessage(updatedMsg)

            if (update.needsAiFollowUp) {
                delay(1500)
                runAiFollowUp(updatedMsg, ChessEngine.applyAiMove(updatedMsg.gameData))
            }
        }
    }

    fun playConnectFourMove(messageId: Int, col: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = ConnectFourEngine.applyPlayerMove(message.gameData, message.gameStatus, col) ?: return@launch
            val updatedMsg = applyGameUpdate(message, update)
            repository.updateMessage(updatedMsg)

            if (update.gameWinner == Winners.ME) {
                awardCoins(120)
                incrementWins()
            }

            if (update.gameStatus == GameStatuses.ACTIVE) {
                delay(1500)
                runAiFollowUp(updatedMsg, ConnectFourEngine.applyAiMove(updatedMsg.gameData))
            }
        }
    }

    fun playBattleshipMove(messageId: Int, cellIdx: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = BattleshipEngine.applyPlayerMove(message.gameData, message.gameStatus, cellIdx) ?: return@launch
            val updatedMsg = applyGameUpdate(message, update)
            repository.updateMessage(updatedMsg)

            if (update.gameWinner == Winners.ME) {
                awardCoins(150)
                incrementWins()
            }

            if (update.gameStatus == GameStatuses.ACTIVE) {
                delay(1500)
                runAiFollowUp(updatedMsg, BattleshipEngine.applyAiMove(updatedMsg.gameData))
            }
        }
    }

    fun playDiceDuelMove(messageId: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = DiceDuelEngine.roll(message.gameData, message.gameStatus) ?: return@launch
            repository.updateMessage(applyGameUpdate(message, update))

            if (update.gameWinner == Winners.ME) {
                awardCoins(100)
                incrementWins()
            }
        }
    }

    fun playMemoryMatchMove(messageId: Int, cardIdx: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = MemoryMatchEngine.applyPlayerMove(message.gameData, message.gameStatus, cardIdx) ?: return@launch
            val updatedMsg = applyGameUpdate(message, update)
            repository.updateMessage(updatedMsg)

            if (update.gameWinner == Winners.ME) {
                awardCoins(120)
                incrementWins()
            }

            if (update.needsAiFollowUp) {
                delay(1500)
                runAiFollowUp(updatedMsg, MemoryMatchEngine.applyAiMove(updatedMsg.gameData))
            }
        }
    }

    fun playNumberGuesserMove(messageId: Int, guess: Int) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = NumberGuessEngine.guess(message.gameData, message.gameStatus, guess) ?: return@launch
            repository.updateMessage(applyGameUpdate(message, update))

            if (update.gameWinner == Winners.ME) {
                awardCoins(100)
                incrementWins()
            }
        }
    }

    fun playBlackjackMove(messageId: Int, action: String) {
        viewModelScope.launch {
            val message = allMessages.value.find { it.id == messageId } ?: return@launch
            val update = BlackjackEngine.play(message.gameData, message.gameStatus, action) ?: return@launch
            repository.updateMessage(applyGameUpdate(message, update))

            if (update.gameWinner == Winners.ME) {
                awardCoins(120)
                incrementWins()
            }
        }
    }

    // --- LIVE STREAMING HUB ACTIONS (SIMULATED) ---

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
            // Seed a few initial comments (SIMULATED)
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

            // SIMULATED: viewer count grows periodically (no real viewers)
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
                type = ChatTypes.TEXT
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
                // Firebase not configured (no google-services.json) — local demo mode.
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
                // Firebase not configured (no google-services.json) — local demo mode.
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
                    type = ChatTypes.TEXT
                )
                repository.insertMessage(noteMsg)
            }

            awardCoins(30) // bonus reward for sharing with friends!

            // SIMULATED: scripted reply from the friend
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
                type = ChatTypes.TEXT
            )
        )
    }

    /**
     * SIMULATED contact sync: really reads the device contact list (when the
     * READ_CONTACTS permission is granted), but "discovered WeGist users" are
     * hardcoded demo friends — there is no discovery backend yet.
     */
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
            var usedFallbackContacts = false

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

            // SIMULATED fallback contacts for emulator, previews, and testing
            if (deviceContacts.isEmpty()) {
                usedFallbackContacts = true
                deviceContacts.add("Charlie Gamer" to "+15551234567")
                deviceContacts.add("Retro Queen" to "+15559876543")
                deviceContacts.add("Shadow Hunter" to "+15555554444")
                deviceContacts.add("Gamer Mom" to "+15553331111")
                deviceContacts.add("NiteHawk" to "+14155552671")
            }

            // SIMULATED: these "discovered users" are demo friends, not real lookups
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

            contactsSyncStatus.value = if (usedFallbackContacts) {
                "Demo mode: used sample contacts (no permission or empty list). Added $count sample gaming friends. Real discovery comes with the online backend."
            } else {
                "Scanned ${deviceContacts.size} device contacts. Friend discovery is simulated in this demo — added $count sample gamers."
            }
            isSyncingContacts.value = false
            delay(3500)
            contactsSyncStatus.value = null
        }
    }

    /**
     * SIMULATED cloud sync: there is no cloud backend wired up yet. This only
     * updates the local "last backup" timestamp and tells the user honestly
     * whether Firebase is configured.
     */
    fun syncWithFirebase() {
        viewModelScope.launch {
            isSyncingWithFirebase.value = true

            val firebaseConfigured = try {
                com.google.firebase.FirebaseApp.getApps(getApplication()).isNotEmpty()
            } catch (e: Exception) {
                false
            }

            firebaseSyncStatus.value = if (firebaseConfigured) {
                "Connecting to Firebase..."
            } else {
                "No cloud backend configured — running in local demo mode."
            }
            delay(1000)
            firebaseSyncStatus.value = "Saving local profile metadata..."
            delay(700)

            val me = userStats.value
            repository.insertUserStats(me.copy(lastBackupTime = System.currentTimeMillis()))

            firebaseSyncStatus.value = if (firebaseConfigured) {
                "Local snapshot saved ✓ (demo: cloud upload is not implemented yet)"
            } else {
                "Local snapshot saved ✓ Your data stays on this device."
            }
            isSyncingWithFirebase.value = false
            delay(3500)
            firebaseSyncStatus.value = null
        }
    }

    // --- LFG LOBBIES MANAGEMENT (SIMULATED: local list only) ---

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
                    type = ChatTypes.GAME_INVITE,
                    gameType = GameTypes.CUSTOM_LINK,
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
}
