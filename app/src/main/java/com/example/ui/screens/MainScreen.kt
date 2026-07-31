package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.NeonMint
import com.example.ui.viewmodel.WeGistViewModel

@Composable
fun MainScreen(
    viewModel: WeGistViewModel,
    modifier: Modifier = Modifier
) {
    // Current bottom navigation selected tab
    var selectedTab by remember { mutableStateOf("CHATS") }
    var showGlobalSearch by remember { mutableStateOf(false) }

    // Observe core states from ViewModel
    val activeChatId by viewModel.activeChatId.collectAsStateWithLifecycle()
    val activeStreamId by viewModel.activeStreamId.collectAsStateWithLifecycle()
    val isUserBroadcasting by viewModel.isUserBroadcasting.collectAsStateWithLifecycle()

    val messagesList by viewModel.allMessages.collectAsStateWithLifecycle()
    val streamsList by viewModel.activeStreams.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val postsList by viewModel.allPosts.collectAsStateWithLifecycle()
    val reelsList by viewModel.allReels.collectAsStateWithLifecycle()
    val storiesList by viewModel.allStories.collectAsStateWithLifecycle()
    val friendsList by viewModel.allFriends.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()

    val restrictInvites by viewModel.restrictInvitesToFriends.collectAsStateWithLifecycle()
    val restrictStreams by viewModel.restrictStreamsToFriends.collectAsStateWithLifecycle()

    val streamComments by viewModel.streamComments.collectAsStateWithLifecycle()
    val floatingEmojis by viewModel.floatingEmojis.collectAsStateWithLifecycle()

    val isSyncingContacts by viewModel.isSyncingContacts.collectAsStateWithLifecycle()
    val contactsSyncStatus by viewModel.contactsSyncStatus.collectAsStateWithLifecycle()

    // Observe active LFG lobbies
    val lfgLobbies by viewModel.lfgLobbies.collectAsStateWithLifecycle()

    // Screen overlay Routing check
    when {
        // Overlay 0: Initial Sign Up Check!
        userStats.authPhone.isEmpty() && userStats.authGmail.isEmpty() && userStats.authFacebook.isEmpty() -> {
            SignUpScreen(
                userStats = userStats,
                onSignUpComplete = { type, newUsername, value ->
                    viewModel.updateUsername(newUsername)
                    val mapType = when (type) {
                        "PHONE" -> "PHONE"
                        "EMAIL" -> "GMAIL"
                        "FACEBOOK" -> "FACEBOOK"
                        else -> "PHONE"
                    }
                    viewModel.linkAccount(mapType, value)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay 1: Active user is live broadcasting!
        isUserBroadcasting -> {
            val broadcastTitle by viewModel.broadcastTitle.collectAsStateWithLifecycle()
            val broadcastGame by viewModel.broadcastGameName.collectAsStateWithLifecycle()
            // Find user stream viewer count (computed dynamically)
            val userStream = streamsList.find { it.id == "stream_user" }
            val viewerCount = userStream?.viewerCount ?: 1

            LiveBroadcasterScreen(
                streamTitle = broadcastTitle,
                gameName = broadcastGame,
                viewerCount = viewerCount,
                comments = streamComments,
                onStopBroadcast = { viewModel.stopUserBroadcast() },
                onAwardCoins = { coins -> viewModel.awardCoins(coins) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay 2: User is spectating a live stream!
        activeStreamId != null -> {
            val activeStream = streamsList.find { it.id == activeStreamId }
            if (activeStream != null) {
                LiveStreamPlayerScreen(
                    stream = activeStream,
                    comments = streamComments,
                    floatingEmojis = floatingEmojis,
                    userCoins = userStats.coins,
                    onSendComment = { text -> viewModel.sendStreamComment(text) },
                    onTriggerReaction = { emoji -> viewModel.triggerFloatingReaction(emoji) },
                    onSendGift = { cost -> viewModel.sendVirtualGift(cost) },
                    onBack = { viewModel.selectStream(null) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Safe recovery
                LaunchedEffect(Unit) { viewModel.selectStream(null) }
            }
        }

        // Overlay 3: User is in a private message chat thread (allows inline gameplay)
        activeChatId != null -> {
            val filteredMessages = messagesList.filter { it.chatId == activeChatId }
            val currentMode by viewModel.appMode.collectAsStateWithLifecycle()
            ChatDetailScreen(
                chatId = activeChatId!!,
                messages = filteredMessages,
                userStats = userStats,
                onSendMessage = { content, type, gameType, gameData ->
                    if (type == "COINS_TRANSFER") {
                        viewModel.transferCoinsToFriend(activeChatId!!, gameData?.toIntOrNull() ?: 100)
                    } else {
                        viewModel.sendMessage(activeChatId!!, content, type, gameType, gameData)
                    }
                },
                onPlayTicTacToeMove = { msgId, cellIdx ->
                    viewModel.playTicTacToeMove(msgId, cellIdx)
                },
                onPlayRockPaperScissors = { msgId, choice ->
                    viewModel.playRockPaperScissors(msgId, choice)
                },
                onPlayTriviaAnswer = { msgId, optionIdx ->
                    viewModel.playTriviaAnswer(msgId, optionIdx)
                },
                onPlayChessMove = { msgId, fromIdx, toIdx ->
                    viewModel.playChessMove(msgId, fromIdx, toIdx)
                },
                onPlayConnectFourMove = { msgId, columnIdx ->
                    viewModel.playConnectFourMove(msgId, columnIdx)
                },
                onPlayBattleshipMove = { msgId, cellIdx ->
                    viewModel.playBattleshipMove(msgId, cellIdx)
                },
                onPlayDiceDuelMove = { msgId ->
                    viewModel.playDiceDuelMove(msgId)
                },
                onPlayMemoryMatchMove = { msgId, cardIdx ->
                    viewModel.playMemoryMatchMove(msgId, cardIdx)
                },
                onPlayNumberGuesserMove = { msgId, guess ->
                    viewModel.playNumberGuesserMove(msgId, guess)
                },
                onPlayBlackjackMove = { msgId, action ->
                    viewModel.playBlackjackMove(msgId, action)
                },
                onReactToMessage = { msgId, emoji ->
                    viewModel.reactToMessage(msgId, emoji)
                },
                onBack = { viewModel.activeChatId.value = null },
                appMode = currentMode,
                modifier = Modifier.fillMaxSize()
            )
        }

        showGlobalSearch -> {
            GlobalSearchScreen(
                posts = postsList,
                reels = reelsList,
                streams = streamsList,
                userStats = userStats,
                onNavigateToChat = { chatId ->
                    viewModel.activeChatId.value = chatId
                    showGlobalSearch = false
                },
                onNavigateToStream = { streamId ->
                    viewModel.selectStream(streamId)
                    showGlobalSearch = false
                },
                onNavigateToFeed = { post ->
                    selectedTab = "FEED"
                    showGlobalSearch = false
                },
                onBack = { showGlobalSearch = false },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Standard Bottom Navigation view layout
        else -> {
            Scaffold(
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberCard),
                        contentAlignment = Alignment.Center
                    ) {
                        NavigationBar(
                            containerColor = CyberCard,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .widthIn(max = 1000.dp)
                                .testTag("main_bottom_nav_bar")
                        ) {
                            // Tab 1: CHATS
                            NavigationBarItem(
                                selected = selectedTab == "CHATS",
                                onClick = { selectedTab = "CHATS" },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == "CHATS") Icons.Default.Forum else Icons.Outlined.Forum,
                                        contentDescription = "Chats"
                                    )
                                },
                                label = { Text("Chats", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonMint,
                                    indicatorColor = NeonMint,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_chats_btn")
                            )

                            // Tab 1.5: WEGIST FEED & CLIPS
                            NavigationBarItem(
                                selected = selectedTab == "FEED",
                                onClick = { selectedTab = "FEED" },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == "FEED") Icons.Default.Share else Icons.Outlined.Share,
                                        contentDescription = "Feed"
                                    )
                                },
                                label = { Text("Feed", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonMint,
                                    indicatorColor = NeonMint,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_feed_btn")
                            )

                            // Tab 2: LIVE STREAMS
                            NavigationBarItem(
                                selected = selectedTab == "LIVE",
                                onClick = { selectedTab = "LIVE" },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == "LIVE") Icons.Default.Sensors else Icons.Outlined.Sensors,
                                        contentDescription = "Streams"
                                    )
                                },
                                label = { Text("Streams", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonMint,
                                    indicatorColor = NeonMint,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_streams_btn")
                            )

                            // Tab 3: ARCADE MINI GAMES
                            NavigationBarItem(
                                selected = selectedTab == "GAMES",
                                onClick = { selectedTab = "GAMES" },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == "GAMES") Icons.Default.VideogameAsset else Icons.Outlined.VideogameAsset,
                                        contentDescription = "Arcade"
                                    )
                                },
                                label = { Text("Arcade", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonMint,
                                    indicatorColor = NeonMint,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_arcade_btn")
                            )

                            // Tab 4: PROFILE SETTINGS
                            NavigationBarItem(
                                selected = selectedTab == "PROFILE",
                                onClick = { selectedTab = "PROFILE" },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == "PROFILE") Icons.Default.Person else Icons.Outlined.Person,
                                        contentDescription = "Profile"
                                    )
                                },
                                label = { Text("Profile", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonMint,
                                    indicatorColor = NeonMint,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_profile_btn")
                            )
                        }
                    }
                },
                containerColor = CyberSlate,
                modifier = modifier
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val maxW = maxWidth
                    val nestedModifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (maxW > 840.dp) {
                                Modifier
                                    .widthIn(max = 1000.dp)
                                    .align(Alignment.TopCenter)
                            } else {
                                Modifier
                            }
                        )

                when (selectedTab) {
                    "CHATS" -> {
                        val currentMode by viewModel.appMode.collectAsStateWithLifecycle()
                        ChatsListScreen(
                            messagesList = messagesList,
                            friendsList = friendsList,
                            pendingRequests = pendingRequests,
                            userStats = userStats,
                            onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                            onRejectRequest = { viewModel.rejectFriendRequest(it) },
                            onRemoveFriend = { viewModel.removeFriend(it) },
                            onSendRequest = { id, name, avatar -> viewModel.sendFriendRequest(id, name, avatar) },
                            onInviteToGame = { chatId, gameName -> viewModel.sendGameInviteToFriend(chatId, gameName) },
                            onInviteToStream = { chatId, streamTitle -> viewModel.sendStreamInviteToFriend(chatId, streamTitle) },
                            onChatSelected = { id -> viewModel.activeChatId.value = id },
                            onSearchClicked = { showGlobalSearch = true },
                            onUpdateFriendPresence = { id, presence -> viewModel.updateFriendPresence(id, presence) },
                            onSendMessage = { chatId, content -> viewModel.sendMessage(chatId, content) },
                            onToggleTheme = { dark -> viewModel.setDarkMode(dark) },
                            isSyncingContacts = isSyncingContacts,
                            contactsSyncStatus = contactsSyncStatus,
                            onSyncContacts = { viewModel.syncContacts() },
                            appMode = currentMode,
                            onAppModeChanged = { viewModel.setAppMode(it) },
                            lfgLobbies = lfgLobbies,
                            onCreateLobby = { game, title, max -> viewModel.createLfgLobby(game, title, max) },
                            onJoinLobby = { lobbyId -> viewModel.joinLfgLobby(lobbyId) },
                            modifier = nestedModifier
                        )
                    }

                    "FEED" -> {
                        PostsAndReelsScreen(
                            posts = postsList,
                            reels = reelsList,
                            stories = storiesList,
                            onAddPost = { text, image -> viewModel.createPost(text, image) },
                            onLikePost = { post -> viewModel.toggleLikePost(post) },
                            onAddPostComment = { post -> viewModel.addPostComment(post) },
                            onAddReel = { caption, tag, thumb -> viewModel.createReel(caption, tag, thumb) },
                            onLikeReel = { reel -> viewModel.toggleLikeReel(reel) },
                            onAddReelComment = { reel -> viewModel.addReelComment(reel) },
                            onViewReel = { reel -> viewModel.incrementReelViews(reel) },
                            onAddStory = { text, image -> viewModel.addStory(text, image) },
                            onMarkStoryAsViewed = { id -> viewModel.markStoryAsViewed(id) },
                            onShareContent = { chatId, contentType, author, content, mediaUrl, note ->
                                viewModel.shareContentToChat(chatId, contentType, author, content, mediaUrl, note)
                            },
                            onSearchClicked = { showGlobalSearch = true },
                            isDarkMode = userStats.isDarkMode,
                            onToggleTheme = { dark -> viewModel.setDarkMode(dark) },
                            modifier = nestedModifier
                        )
                    }

                    "LIVE" -> {
                        LiveStreamsScreen(
                            streamsList = streamsList,
                            onSelectStream = { id -> viewModel.selectStream(id) },
                            onStartBroadcast = { title, game -> viewModel.startUserBroadcast(title, game) },
                            onSearchClicked = { showGlobalSearch = true },
                            isDarkMode = userStats.isDarkMode,
                            onToggleTheme = { dark -> viewModel.setDarkMode(dark) },
                            modifier = nestedModifier
                        )
                    }

                    "GAMES" -> {
                        MiniGamesScreen(
                            userStats = userStats,
                            friendsList = friendsList,
                            onAwardCoins = { coins -> viewModel.awardCoins(coins) },
                            onInviteFriend = { friendId, gameName -> viewModel.sendGameInviteToFriend(friendId, gameName) },
                            onSearchClicked = { showGlobalSearch = true },
                            onPerformDailyCheckIn = { viewModel.performDailyCheckIn() },
                            onShareBragPost = { game, result -> viewModel.shareBragPost(game, result) },
                            onToggleTheme = { dark -> viewModel.setDarkMode(dark) },
                            modifier = nestedModifier
                        )
                    }

                    "PROFILE" -> {
                        ProfileSettingsScreen(
                            userStats = userStats,
                            restrictInvites = restrictInvites,
                            restrictStreams = restrictStreams,
                            onUpdateUsername = { name -> viewModel.updateUsername(name) },
                            onUpdateRestrictInvites = { checked -> viewModel.restrictInvitesToFriends.value = checked },
                            onUpdateRestrictStreams = { checked -> viewModel.restrictStreamsToFriends.value = checked },
                            onAwardCoins = { coins -> viewModel.awardCoins(coins) },
                            onUpdateTheme = { dark -> viewModel.setDarkMode(dark) },
                            onUpdateWallpaper = { color, pattern -> viewModel.updateWallpaper(color, pattern) },
                            onUpdatePrivacySettings = { lastSeen, photo, about, reads, timer -> viewModel.updatePrivacySettings(lastSeen, photo, about, reads, timer) },
                            onPerformBackup = { interval, dest -> viewModel.performBackup(interval, dest) },
                            onLinkAccount = { type, value -> viewModel.linkAccount(type, value) },
                            onSearchClicked = { showGlobalSearch = true },
                            onClaimQuest = { questId, xp, coins -> viewModel.claimQuest(questId, xp, coins) },
                            onUpgradePremium = { tier -> viewModel.upgradeToPremium(tier) },
                            onEnterReferralCode = { code -> viewModel.enterReferralCode(code) },
                            onSignOut = { viewModel.signOut() },
                            onDeleteAccount = { viewModel.deleteAccount() },
                            modifier = nestedModifier
                        )
                    }
                }
            }
        }
        }
    }
}
