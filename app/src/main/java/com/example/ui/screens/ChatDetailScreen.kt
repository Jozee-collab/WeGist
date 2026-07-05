package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    messages: List<ChatMessage>,
    userStats: com.example.data.UserStats,
    onSendMessage: (content: String, type: String, gameType: String?, gameData: String?) -> Unit,
    onPlayTicTacToeMove: (messageId: Int, cellIndex: Int) -> Unit,
    onPlayRockPaperScissors: (messageId: Int, choice: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showGameSelector by remember { mutableStateOf(false) }
    var showStickersPanel by remember { mutableStateOf(false) }

    // Scroll to bottom on load or new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val chatName = when (chatId) {
        "alice" -> "Alice (Retro Gamer)"
        "bob" -> "Bob_Destroyer"
        else -> "Gaming Guild"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                            contentDescription = chatName,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, NeonMint, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(chatName, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Active Now", color = NeonMint, fontSize = 11.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSendMessage("Let's join a stream!", "TEXT", null, null)
                    }) {
                        Icon(Icons.Default.VideogameAsset, contentDescription = "Stream", tint = NeonMint)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberCard)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        val wallpaperBgColor = when (userStats.chatWallpaperColor) {
            "CHARCOAL" -> Color(0xFF1F2C33)
            "MINT" -> Color(0xFFECE5DD)
            "PURPLE" -> Color(0xFF1A1226)
            "PINK" -> Color(0xFF2C1322)
            "INDIGO" -> Color(0xFF10192A)
            else -> Color(0xFF0B141A) // Teal/Dark default
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(wallpaperBgColor)
                .padding(innerPadding)
        ) {
            if (userStats.chatWallpaperPattern == "DOODLE") {
                // Draw a gorgeous retro-cyber doodle matrix pattern
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotRadius = 1.5.dp.toPx()
                    val stepX = 40.dp.toPx()
                    val stepY = 40.dp.toPx()
                    val patternColor = Color.White.copy(alpha = 0.04f)
                    
                    var x = 20.dp.toPx()
                    while (x < size.width) {
                        var y = 20.dp.toPx()
                        while (y < size.height) {
                            if (((x / stepX).toInt() + (y / stepY).toInt()) % 2 == 0) {
                                drawCircle(
                                    color = patternColor,
                                    radius = dotRadius,
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            } else {
                                drawLine(
                                    color = patternColor,
                                    start = androidx.compose.ui.geometry.Offset(x - 3.dp.toPx(), y),
                                    end = androidx.compose.ui.geometry.Offset(x + 3.dp.toPx(), y),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = patternColor,
                                    start = androidx.compose.ui.geometry.Offset(x, y - 3.dp.toPx()),
                                    end = androidx.compose.ui.geometry.Offset(x, y + 3.dp.toPx()),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            y += stepY
                        }
                        x += stepX
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderName == "WeGamer_Me"
                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        userStats = userStats,
                        onPlayTicTacToeMove = onPlayTicTacToeMove,
                        onPlayRockPaperScissors = onPlayRockPaperScissors
                    )
                }
            }

            // Stickers Panel
            AnimatedVisibility(
                visible = showStickersPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                StickersSelectionView(
                    onSelectSticker = { sticker ->
                        onSendMessage(sticker, "STICKER", null, null)
                        showStickersPanel = false
                    }
                )
            }

            // Game Selector Panel
            AnimatedVisibility(
                visible = showGameSelector,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GameChallengeSelectionView(
                    onSelectGame = { gameType ->
                        val initialData = if (gameType == "TIC_TAC_TOE") ",,,,,,,," else "PENDING"
                        onSendMessage(
                            "I challenge you to $gameType!",
                            "GAME_INVITE",
                            gameType,
                            initialData
                        )
                        showGameSelector = false
                    }
                )
            }

            // Input Bar
            Surface(
                color = CyberCard,
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .navigationBarsPadding(), // Support safe margin above physical navigators
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Game Launcher button (+)
                    IconButton(
                        onClick = {
                            showGameSelector = !showGameSelector
                            showStickersPanel = false
                        },
                        modifier = Modifier
                            .background(if (showGameSelector) NeonPurple else CyberBorder, CircleShape)
                            .size(36.dp)
                            .testTag("chat_detail_games_btn")
                    ) {
                        Icon(
                            imageVector = if (showGameSelector) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Challenge Game",
                            tint = if (showGameSelector) Color.White else NeonMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Stickers launcher
                    IconButton(
                        onClick = {
                            showStickersPanel = !showStickersPanel
                            showGameSelector = false
                        },
                        modifier = Modifier
                            .background(if (showStickersPanel) NeonMint else CyberBorder, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Stickers",
                            tint = if (showStickersPanel) Color.Black else TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Field input
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Send gaming chat...", color = TextGray, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = Color(0xFF0F0F13),
                            unfocusedContainerColor = Color(0xFF0F0F13)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 100.dp)
                            .testTag("chat_detail_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (inputText.isEmpty()) {
                        // Voice recording button mock
                        IconButton(
                            onClick = {
                                onSendMessage("Voice Note", "VOICE", null, null)
                            },
                            modifier = Modifier
                                .background(CyberBorder, CircleShape)
                                .size(36.dp)
                                .testTag("chat_detail_voice_btn")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice note", tint = TextWhite, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        // Send button
                        IconButton(
                            onClick = {
                                onSendMessage(inputText, "TEXT", null, null)
                                inputText = ""
                            },
                            modifier = Modifier
                                .background(NeonMint, CircleShape)
                                .size(36.dp)
                                .testTag("chat_detail_send_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    userStats: com.example.data.UserStats,
    onPlayTicTacToeMove: (messageId: Int, cellIndex: Int) -> Unit,
    onPlayRockPaperScissors: (messageId: Int, choice: String) -> Unit
) {
    val isDarkWallpaper = userStats.chatWallpaperColor in listOf("CHARCOAL", "PURPLE", "PINK", "INDIGO") || userStats.chatWallpaperColor.isEmpty()
    val bubbleColor = if (isMe) {
        if (isDarkWallpaper) Color(0xFF005C4B) else Color(0xFFD9FDD3)
    } else {
        if (isDarkWallpaper) Color(0xFF202C33) else Color(0xFFFFFFFF)
    }
    val textColor = if (isDarkWallpaper) Color(0xFFE9EDEF) else Color(0xFF111B21)
    val align = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe) {
        RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
    } else {
        RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        // Sender Username label if not me
        if (!isMe) {
            Text(
                text = message.senderName,
                color = if (isDarkWallpaper) Color(0xFF8696A0) else Color(0xFF128C7E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )
        }

        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                when (message.type) {
                    "TEXT" -> {
                        Text(text = message.content, color = textColor, fontSize = 14.sp)
                    }

                    "STICKER" -> {
                        Text(
                            text = message.content,
                            fontSize = 60.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "VOICE" -> {
                        VoiceWaveformPlayer()
                    }

                    "MEDIA" -> {
                        Column {
                            Icon(Icons.Default.Link, contentDescription = "Link", tint = NeonMint)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(message.content, color = NeonMint, fontSize = 13.sp, modifier = Modifier.clickable { })
                        }
                    }

                    "GAME_INVITE" -> {
                        GameChallengeInviteView(
                            message = message,
                            isMeSender = isMe,
                            onPlayTicTacToeMove = onPlayTicTacToeMove,
                            onPlayRockPaperScissors = onPlayRockPaperScissors
                        )
                    }

                    "REPOST" -> {
                        val isPost = message.gameType == "POST"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            // Header badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPost) Icons.Default.Forum else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = NeonMint,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPost) "SHARED GIST" else "SHARED CLIP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonMint,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Shared Content Inner Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberSlate.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                             ) {
                                Column {
                                    Text(
                                        text = "@${message.gameStatus ?: "Unknown"}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NeonPurple
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = message.content,
                                        fontSize = 13.sp,
                                        color = TextWhite,
                                        lineHeight = 18.sp
                                    )

                                    if (message.gameData != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val imageRes = when (message.gameData) {
                                            "img_retro_stream_thumbnail_1783282368689" -> R.drawable.img_retro_stream_thumbnail_1783282368689
                                            "reel_retro_arcade" -> R.drawable.img_retro_stream_thumbnail_1783282368689
                                            else -> R.drawable.img_puzzle_stream_thumbnail_1783282381092
                                        }
                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = "Shared Content Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Aligned bottom row containing timestamp and delivery checkmarks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        color = if (isDarkWallpaper) Color(0x99E9EDEF) else Color(0x99111B21),
                        fontSize = 9.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(3.dp))
                        val isRead = userStats.privacyReadReceipts
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = if (isRead) "Read" else "Delivered",
                            tint = if (isRead) Color(0xFF53BDEB) else Color(0xFF8696A0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// Custom simulated audio wave controller
@Composable
fun VoiceWaveformPlayer() {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    if (isPlaying) {
        LaunchedEffect(Unit) {
            while (progress < 1.0f) {
                delay(100)
                progress += 0.05f
            }
            isPlaying = false
            progress = 0f
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(200.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .background(NeonMint, CircleShape)
                .size(30.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        // Wave representation lines
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heights = listOf(14, 25, 10, 32, 18, 28, 12, 22, 15, 30, 8, 20)
            heights.forEachIndexed { idx, h ->
                val fraction = idx.toFloat() / heights.size
                val active = fraction < progress
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(h.dp)
                        .background(if (active) NeonMint else TextGray, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

// Custom UI rendering in-chat casual game board states
@Composable
fun GameChallengeInviteView(
    message: ChatMessage,
    isMeSender: Boolean,
    onPlayTicTacToeMove: (messageId: Int, cellIndex: Int) -> Unit,
    onPlayRockPaperScissors: (messageId: Int, choice: String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.VideogameAsset, contentDescription = "Game", tint = NeonMint)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${message.gameType} Challenge",
                fontWeight = FontWeight.Bold,
                color = NeonMint,
                fontSize = 15.sp
            )
        }

        if (message.gameType == "TIC_TAC_TOE") {
            val boardData = message.gameData?.split(",") ?: List(9) { "" }
            val completed = message.gameStatus == "COMPLETED"

            // 3x3 Grid
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0..2) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (col in 0..2) {
                                val idx = row * 3 + col
                                val cell = boardData.getOrElse(idx) { "" }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, CyberBorder)
                                        .clickable(enabled = !completed && cell.isEmpty()) {
                                            onPlayTicTacToeMove(message.id, idx)
                                        }
                                        .testTag("tic_tac_toe_cell_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell,
                                        color = if (cell == "X") NeonMint else if (cell == "O") NeonPurple else Color.Transparent,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text outcome
            Text(
                text = when {
                    completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+100 🪙)"
                    completed && message.gameWinner == "opponent" -> "Winner: Opponent 🤖"
                    completed && message.gameWinner == "DRAW" -> "Tie Game! 🤝"
                    completed -> "Completed"
                    else -> "Your turn: Make a move! 🕹️"
                },
                fontSize = 12.sp,
                color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                fontWeight = FontWeight.Bold
            )

        } else if (message.gameType == "ROCK_PAPER_SCI") {
            val completed = message.gameStatus == "COMPLETED"

            if (!completed) {
                // Rock Paper Scissors Choices
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    val choices = listOf("ROCK" to "🪨", "PAPER" to "📄", "SCISSORS" to "✂️")
                    choices.forEach { (type, emoji) ->
                        Button(
                            onClick = { onPlayRockPaperScissors(message.id, type) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("rps_btn_$type")
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
                Text("Pick Rock, Paper, or Scissors to match!", color = TextGray, fontSize = 11.sp)
            } else {
                // Show completed choice results
                val parts = message.gameData?.split(",") ?: listOf("?", "?")
                val userCh = parts.getOrNull(0) ?: "ROCK"
                val oppCh = parts.getOrNull(1) ?: "ROCK"

                val userEmoji = if (userCh == "ROCK") "🪨" else if (userCh == "PAPER") "📄" else "✂️"
                val oppEmoji = if (oppCh == "ROCK") "🪨" else if (oppCh == "PAPER") "📄" else "✂️"

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", color = TextGray, fontSize = 11.sp)
                        Text(userEmoji, fontSize = 28.sp)
                    }
                    Text("VS", fontWeight = FontWeight.Bold, color = CyberPink)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent", color = TextGray, fontSize = 11.sp)
                        Text(oppEmoji, fontSize = 28.sp)
                    }
                }
                Text(message.content, fontSize = 12.sp, textAlign = TextAlign.Center, color = NeonMint, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// In-chat mini-game selector panel
@Composable
fun GameChallengeSelectionView(
    onSelectGame: (String) -> Unit
) {
    Surface(
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Send In-Chat Casual Challenge", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("TIC_TAC_TOE") }
                        .testTag("challenge_ttt"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("❌ Tic Tac Toe ⭕", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Traditional 3-in-a-row", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("ROCK_PAPER_SCI") }
                        .testTag("challenge_rps"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪨📄✂️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rock, Paper, Scissors", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// Sticker Selection panel
@Composable
fun StickersSelectionView(
    onSelectSticker: (String) -> Unit
) {
    Surface(
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gaming Stickers", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val stickers = listOf("👾", "💣", "🏆", "🎮", "🕹️", "💥", "🔥", "👑")
                stickers.forEach { sticker ->
                    Text(
                        text = sticker,
                        fontSize = 36.sp,
                        modifier = Modifier
                            .clickable { onSelectSticker(sticker) }
                            .padding(4.dp)
                            .testTag("sticker_$sticker")
                    )
                }
            }
        }
    }
}
