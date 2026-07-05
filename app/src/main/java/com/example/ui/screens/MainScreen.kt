package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

    val restrictInvites by viewModel.restrictInvitesToFriends.collectAsStateWithLifecycle()
    val restrictStreams by viewModel.restrictStreamsToFriends.collectAsStateWithLifecycle()

    val streamComments by viewModel.streamComments.collectAsStateWithLifecycle()
    val floatingEmojis by viewModel.floatingEmojis.collectAsStateWithLifecycle()

    // Screen overlay Routing check
    when {
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
            ChatDetailScreen(
                chatId = activeChatId!!,
                messages = filteredMessages,
                userStats = userStats,
                onSendMessage = { content, type, gameType, gameData ->
                    viewModel.sendMessage(activeChatId!!, content, type, gameType, gameData)
                },
                onPlayTicTacToeMove = { msgId, cellIdx ->
                    viewModel.playTicTacToeMove(msgId, cellIdx)
                },
                onPlayRockPaperScissors = { msgId, choice ->
                    viewModel.playRockPaperScissors(msgId, choice)
                },
                onBack = { viewModel.activeChatId.value = null },
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
                    NavigationBar(
                        containerColor = CyberCard,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav_bar")
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
                },
                containerColor = CyberSlate,
                modifier = modifier
            ) { innerPadding ->
                val nestedModifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)

                when (selectedTab) {
                    "CHATS" -> {
                        ChatsListScreen(
                            messagesList = messagesList,
                            onChatSelected = { id -> viewModel.activeChatId.value = id },
                            onSearchClicked = { showGlobalSearch = true },
                            modifier = nestedModifier
                        )
                    }

                    "FEED" -> {
                        PostsAndReelsScreen(
                            posts = postsList,
                            reels = reelsList,
                            onAddPost = { text, image -> viewModel.createPost(text, image) },
                            onLikePost = { post -> viewModel.toggleLikePost(post) },
                            onAddPostComment = { post -> viewModel.addPostComment(post) },
                            onAddReel = { caption, tag, thumb -> viewModel.createReel(caption, tag, thumb) },
                            onLikeReel = { reel -> viewModel.toggleLikeReel(reel) },
                            onAddReelComment = { reel -> viewModel.addReelComment(reel) },
                            onViewReel = { reel -> viewModel.incrementReelViews(reel) },
                            onShareContent = { chatId, contentType, author, content, mediaUrl, note ->
                                viewModel.shareContentToChat(chatId, contentType, author, content, mediaUrl, note)
                            },
                            onSearchClicked = { showGlobalSearch = true },
                            modifier = nestedModifier
                        )
                    }

                    "LIVE" -> {
                        LiveStreamsScreen(
                            streamsList = streamsList,
                            onSelectStream = { id -> viewModel.selectStream(id) },
                            onStartBroadcast = { title, game -> viewModel.startUserBroadcast(title, game) },
                            onSearchClicked = { showGlobalSearch = true },
                            modifier = nestedModifier
                        )
                    }

                    "GAMES" -> {
                        MiniGamesScreen(
                            userStats = userStats,
                            onAwardCoins = { coins -> viewModel.awardCoins(coins) },
                            onSearchClicked = { showGlobalSearch = true },
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
                            modifier = nestedModifier
                        )
                    }
                }
            }
        }
    }
}
