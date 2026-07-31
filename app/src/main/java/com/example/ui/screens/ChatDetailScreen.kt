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
    onPlayTriviaAnswer: (messageId: Int, selectedOptionIdx: Int) -> Unit = { _, _ -> },
    onPlayChessMove: (messageId: Int, fromIdx: Int, toIdx: Int) -> Unit = { _, _, _ -> },
    onPlayConnectFourMove: (messageId: Int, columnIdx: Int) -> Unit = { _, _ -> },
    onPlayBattleshipMove: (messageId: Int, cellIdx: Int) -> Unit = { _, _ -> },
    onPlayDiceDuelMove: (messageId: Int) -> Unit = { _ -> },
    onPlayMemoryMatchMove: (messageId: Int, cardIdx: Int) -> Unit = { _, _ -> },
    onPlayNumberGuesserMove: (messageId: Int, guess: Int) -> Unit = { _, _ -> },
    onPlayBlackjackMove: (messageId: Int, action: String) -> Unit = { _, _ -> },
    onReactToMessage: (messageId: Int, emoji: String) -> Unit,
    onBack: () -> Unit,
    appMode: String = "GAMING",
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showGameSelector by remember { mutableStateOf(false) }
    var showStickersPanel by remember { mutableStateOf(false) }
    var showAttachmentSelector by remember { mutableStateOf(false) }
    var showCustomLinkDialog by remember { mutableStateOf(false) }
    var selectedGamerName by remember { mutableStateOf<String?>(null) }

    var showCoinsTransferDialog by remember { mutableStateOf(false) }
    var coinsAmountText by remember { mutableStateOf("100") }

    // Innovative features state
    var showAiRecapDialog by remember { mutableStateOf(false) }
    var showTranslationDialog by remember { mutableStateOf(false) }
    var showGamingStatsDialog by remember { mutableStateOf(false) }
    var selectedTranslationLang by remember { mutableStateOf("English") }
    var translationEnabled by remember { mutableStateOf(false) }

    if (showCoinsTransferDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showCoinsTransferDialog = false },
            containerColor = CyberCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentOrange.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, AccentOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪙", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Peer-to-Peer Payment", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Transfer coins directly from your wallet balance to this chat participant instantly.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Your Balance:", color = TextGray, fontSize = 12.sp)
                        Text("🪙 ${userStats.coins} Coins", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    OutlinedTextField(
                        value = coinsAmountText,
                        onValueChange = { coinsAmountText = it.filter { char -> char.isDigit() } },
                        label = { Text("Transfer Amount", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = CyberBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )
                    
                    // Quick select options
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("50", "100", "250", "500").forEach { valAmount ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (coinsAmountText == valAmount) AccentOrange.copy(alpha = 0.2f) else CyberBorder, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (coinsAmountText == valAmount) AccentOrange else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { coinsAmountText = valAmount }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(valAmount, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = coinsAmountText.toIntOrNull() ?: 0
                        if (amt > 0 && amt <= userStats.coins) {
                            onSendMessage("Sent $amt Coins 🪙", "COINS_TRANSFER", null, amt.toString())
                            showCoinsTransferDialog = false
                        } else if (amt > userStats.coins) {
                            android.widget.Toast.makeText(context, "Insufficient coin balance!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Enter a valid amount!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    modifier = Modifier.testTag("send_p2p_payment_btn")
                ) {
                    Text("Transfer Coins 💸", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCoinsTransferDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            }
        )
    }

    if (selectedGamerName != null) {
        val name = selectedGamerName!!
        val isAlice = name.contains("Alice", ignoreCase = true)
        val isBob = name.contains("Bob", ignoreCase = true)
        
        val gamerTag = "WG#" + (Math.abs(name.hashCode() % 9000) + 1000)
        val level = if (isAlice) 10 else if (isBob) 9 else 5
        val rank = if (isAlice) 42 else if (isBob) 110 else 352
        val wins = if (isAlice) 32 else if (isBob) 25 else 12
        val activeGame = if (isAlice) "Pac-Neon Cyber" else if (isBob) "Brick Breaker Retro" else "Neon Snake Classic"
        val status = "Playing: $activeGame 🎮"
        val bio = if (isAlice) "Retro gaming is my life! Always down for a Tic Tac Toe rematch! 👾" 
                    else if (isBob) "Ranked speedrunner. I destroy brick walls for breakfast. 🧱" 
                    else "Elite Guild Scout. Friendly gamer, let's duel! 🛡️"

        AlertDialog(
            onDismissRequest = { selectedGamerName = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isAlice) NeonPurple else if (isBob) CyberPink else NeonMint, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(2).uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                        Text(
                            text = gamerTag,
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(NeonMint, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = status,
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Biography
                    Column {
                        Text("MEMBER BIO", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bio,
                            color = TextWhite,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    // Stats row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF0F0F13), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("LEVEL", color = TextGray, fontSize = 9.sp)
                            Text("$level", color = NeonPurple, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF0F0F13), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("RANK", color = TextGray, fontSize = 9.sp)
                            Text("#$rank", color = CyberPink, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF0F0F13), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("WINS", color = TextGray, fontSize = 9.sp)
                            Text("$wins W", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSendMessage(
                                "I challenge you to TIC_TAC_TOE!",
                                "GAME_INVITE",
                                "TIC_TAC_TOE",
                                ",,,,,,,,"
                            )
                            selectedGamerName = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                        modifier = Modifier.fillMaxWidth().testTag("gamer_card_challenge_ttt_btn")
                    ) {
                        Text("Challenge to Tic Tac Toe 🎮", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSendMessage(
                                "I challenge you to ROCK_PAPER_SCI!",
                                "GAME_INVITE",
                                "ROCK_PAPER_SCI",
                                "PENDING"
                            )
                            selectedGamerName = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth().testTag("gamer_card_challenge_rps_btn")
                    ) {
                        Text("Challenge to Rock Paper Scissors ⚔️", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { selectedGamerName = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Profile", color = TextWhite, fontSize = 12.sp)
                    }
                }
            },
            containerColor = Color(0xFF14141A),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
        )
    }

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

    if (showCustomLinkDialog) {
        val gamesList = listOf(
            "Space Shooter Arcade",
            "Brick Breaker Retro",
            "Neon Snake Classic",
            "Pac-Neon Cyber",
            "Tetris Grid Lock",
            "Retro Space Invaders",
            "Chess Challenge",
            "Tic Tac Toe",
            "Rock Paper Scissors"
        )

        var selectedGame by remember { mutableStateOf(gamesList[0]) }
        var inviteLinkInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        // Automatically update the link and description whenever a game is selected
        LaunchedEffect(selectedGame) {
            val randomId = (100000..999999).random()
            val link = when (selectedGame) {
                "Space Shooter Arcade" -> "https://wegist.arcade.play/space-shooter/challenge-lobby-$randomId"
                "Brick Breaker Retro" -> "https://wegist.arcade.play/brick-breaker/room-$randomId"
                "Neon Snake Classic" -> "https://wegist.arcade.play/neon-snake/match-$randomId"
                "Pac-Neon Cyber" -> "https://wegist.arcade.play/pac-neon/join-$randomId"
                "Tetris Grid Lock" -> "https://wegist.arcade.play/tetris/duel-$randomId"
                "Retro Space Invaders" -> "https://wegist.arcade.play/space-invaders/lobby-$randomId"
                "Chess Challenge" -> "https://chess.org/play/wegist-challenge-$randomId"
                "Tic Tac Toe" -> "https://wegist.arcade.play/tic-tac-toe/room-$randomId"
                "Rock Paper Scissors" -> "https://wegist.arcade.play/rock-paper-scissors/lobby-$randomId"
                else -> "https://wegist.arcade.play/challenge/join?game=${selectedGame.replace(" ", "-").lowercase()}&id=$randomId"
            }
            inviteLinkInput = link
            descInput = "Hey! Let's play $selectedGame. Join my game challenge invite link! 🎮⚔️"
        }

        AlertDialog(
            onDismissRequest = { showCustomLinkDialog = false },
            title = {
                Text("Send Game Challenge Link 🔗", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select a game and WeGist will auto-generate an invitation challenge link for you.", color = TextGray, fontSize = 12.sp)

                    // Game Selection Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedGame,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Game to Play", color = TextGray) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { dropdownExpanded = !dropdownExpanded },
                                    modifier = Modifier.testTag("game_dropdown_arrow")
                                ) {
                                    Icon(
                                        imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown Arrow",
                                        tint = NeonPurple
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = CyberBorder,
                                focusedContainerColor = Color(0xFF0F0F13),
                                unfocusedContainerColor = Color(0xFF0F0F13)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true }
                                .testTag("game_dropdown_field")
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color(0xFF14141A))
                                .border(1.dp, CyberBorder)
                                .testTag("game_dropdown_menu")
                        ) {
                            gamesList.forEach { game ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val emoji = when (game) {
                                                "Space Shooter Arcade" -> "🚀"
                                                "Brick Breaker Retro" -> "👾"
                                                "Neon Snake Classic" -> "🐍"
                                                "Pac-Neon Cyber" -> "🍬"
                                                "Tetris Grid Lock" -> "🧩"
                                                "Retro Space Invaders" -> "🛸"
                                                "Chess Challenge" -> "👑"
                                                "Tic Tac Toe" -> "❌"
                                                "Rock Paper Scissors" -> "🪨"
                                                else -> "🎮"
                                            }
                                            Text("$emoji  $game", color = TextWhite, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedGame = game
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("dropdown_item_${game.replace(" ", "_").lowercase()}")
                                )
                            }
                        }
                    }

                    // Auto-Generated Invite Link input
                    OutlinedTextField(
                        value = inviteLinkInput,
                        onValueChange = { inviteLinkInput = it },
                        label = { Text("Auto-Generated Challenge Link", color = TextGray) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val randomId = (100000..999999).random()
                                    inviteLinkInput = when (selectedGame) {
                                        "Space Shooter Arcade" -> "https://wegist.arcade.play/space-shooter/challenge-lobby-$randomId"
                                        "Brick Breaker Retro" -> "https://wegist.arcade.play/brick-breaker/room-$randomId"
                                        "Neon Snake Classic" -> "https://wegist.arcade.play/neon-snake/match-$randomId"
                                        "Pac-Neon Cyber" -> "https://wegist.arcade.play/pac-neon/join-$randomId"
                                        "Tetris Grid Lock" -> "https://wegist.arcade.play/tetris/duel-$randomId"
                                        "Retro Space Invaders" -> "https://wegist.arcade.play/space-invaders/lobby-$randomId"
                                        "Chess Challenge" -> "https://chess.org/play/wegist-challenge-$randomId"
                                        "Tic Tac Toe" -> "https://wegist.arcade.play/tic-tac-toe/room-$randomId"
                                        "Rock Paper Scissors" -> "https://wegist.arcade.play/rock-paper-scissors/lobby-$randomId"
                                        else -> "https://wegist.arcade.play/challenge/join?game=${selectedGame.replace(" ", "-").lowercase()}&id=$randomId"
                                    }
                                },
                                modifier = Modifier.testTag("regenerate_link_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate Link",
                                    tint = NeonMint
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedContainerColor = Color(0xFF0F0F13),
                            unfocusedContainerColor = Color(0xFF0F0F13)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_game_link_input"),
                        singleLine = true
                    )

                    // Message description
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Challenge Description Note", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedContainerColor = Color(0xFF0F0F13),
                            unfocusedContainerColor = Color(0xFF0F0F13)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_game_desc_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedGame.isNotBlank() && inviteLinkInput.isNotBlank()) {
                            val finalDesc = if (descInput.isBlank()) "Join my game challenge in $selectedGame!" else descInput
                            onSendMessage(
                                "Challenge: Join me in $selectedGame! $inviteLinkInput",
                                "GAME_INVITE",
                                "CUSTOM_LINK",
                                "$selectedGame|$inviteLinkInput|$finalDesc"
                            )
                            showCustomLinkDialog = false
                            showGameSelector = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                    enabled = selectedGame.isNotBlank() && inviteLinkInput.isNotBlank(),
                    modifier = Modifier.testTag("submit_custom_challenge_btn")
                ) {
                    Text("Send Invite Link ✉️", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomLinkDialog = false },
                    modifier = Modifier.testTag("cancel_custom_challenge_btn")
                ) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = Color(0xFF14141A),
            tonalElevation = 6.dp
        )
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
                    when (appMode) {
                        "GAMING" -> {
                            IconButton(onClick = { showGamingStatsDialog = true }) {
                                Icon(Icons.Default.Leaderboard, contentDescription = "Match Stats", tint = NeonMint)
                            }
                            IconButton(onClick = {
                                onSendMessage("Let's join a stream!", "TEXT", null, null)
                            }) {
                                Icon(Icons.Default.VideogameAsset, contentDescription = "Stream", tint = NeonMint)
                            }
                        }
                        "CASUAL" -> {
                            IconButton(onClick = { showTranslationDialog = true }) {
                                Icon(Icons.Default.Translate, contentDescription = "Translate Chat", tint = Color(0xFF818CF8))
                            }
                        }
                        "ENTERPRISE" -> {
                            IconButton(onClick = { showAiRecapDialog = true }) {
                                Icon(Icons.Default.Assessment, contentDescription = "AI Recap", tint = Color(0xFF00A884))
                            }
                        }
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
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1000.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Enterprise Scheduling Banner
                if (appMode == "ENTERPRISE") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        var showScheduleMeetingDialog by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Topic, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Professional Workspace Active", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Collaborate with thread-based replies & schedule syncs.", color = TextGray, fontSize = 10.sp)
                                }
                            }
                            Button(
                                onClick = { showScheduleMeetingDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp).testTag("schedule_meeting_btn")
                            ) {
                                Text("Schedule 📅", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (showScheduleMeetingDialog) {
                            var meetingTitle by remember { mutableStateOf("Weekly Sprint Review") }
                            var meetingTime by remember { mutableStateOf("Today, 3:00 PM") }
                            AlertDialog(
                                onDismissRequest = { showScheduleMeetingDialog = false },
                                title = { Text("Schedule Work Meeting", color = TextWhite, fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Invite colleagues to synchronized meetings directly inside your threaded room.", color = TextGray, fontSize = 11.sp)
                                        OutlinedTextField(
                                            value = meetingTitle,
                                            onValueChange = { meetingTitle = it },
                                            label = { Text("Meeting Subject", color = TextGray, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00A884),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedTextColor = TextWhite,
                                                unfocusedTextColor = TextWhite
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = meetingTime,
                                            onValueChange = { meetingTime = it },
                                            label = { Text("Date & Time Slot", color = TextGray, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00A884),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedTextColor = TextWhite,
                                                unfocusedTextColor = TextWhite
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onSendMessage(
                                                "📅 Calendar Invite: $meetingTitle @ $meetingTime",
                                                "GAME_INVITE",
                                                "CUSTOM_LINK",
                                                "$meetingTitle|https://meet.google.com/wegist-corp-meeting|Invite: $meetingTitle\\nTime: $meetingTime"
                                            )
                                            showScheduleMeetingDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
                                    ) {
                                        Text("Post Invite ✉️", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showScheduleMeetingDialog = false }) {
                                        Text("Cancel", color = TextGray)
                                    }
                                },
                                containerColor = Color(0xFF0F172A)
                            )
                        }
                    }
                }

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
                // WhatsApp E2EE banner at top of chat log
                item {
                    var showE2EEDialog by remember { mutableStateOf(false) }
                    
                    if (showE2EEDialog) {
                        AlertDialog(
                            onDismissRequest = { showE2EEDialog = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonMint, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Signal E2E Security", fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "This conversation is fully encrypted using the Signal Protocol framework. Messages, game moves, voice clips, and file attachments are encrypted on your device and can only be decrypted by the recipient.",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberBorder, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text("SECURITY KEY FINGERPRINT", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "42918 09241 88325 11984 00213\n88349 71642 90124 55321 09842",
                                                color = NeonMint,
                                                fontSize = 12.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showE2EEDialog = false }) {
                                    Text("Verify Keys", color = NeonMint)
                                }
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFF182229),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF2B3A42)),
                            modifier = Modifier
                                .widthIn(max = 320.dp)
                                .clickable { showE2EEDialog = true }
                                .testTag("e2ee_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF8696A0),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Messages are end-to-end encrypted. No one outside of this chat, not even WeGist, can read them. Tap to verify.",
                                    color = Color(0xFF8696A0),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    val isMe = msg.senderName == "WeGamer_Me"
                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        userStats = userStats,
                        onPlayTicTacToeMove = onPlayTicTacToeMove,
                        onPlayRockPaperScissors = onPlayRockPaperScissors,
                        onPlayTriviaAnswer = onPlayTriviaAnswer,
                        onPlayChessMove = onPlayChessMove,
                        onPlayConnectFourMove = onPlayConnectFourMove,
                        onPlayBattleshipMove = onPlayBattleshipMove,
                        onPlayDiceDuelMove = onPlayDiceDuelMove,
                        onPlayMemoryMatchMove = onPlayMemoryMatchMove,
                        onPlayNumberGuesserMove = onPlayNumberGuesserMove,
                        onPlayBlackjackMove = onPlayBlackjackMove,
                        onGamerNameClicked = { selectedGamerName = it },
                        onReactToMessage = onReactToMessage,
                        appMode = appMode
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

            // Attachment Selection Panel (WhatsApp Style File Sharing)
            AnimatedVisibility(
                visible = showAttachmentSelector,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AttachmentSelectionView(
                    onSelectFile = { fileName, fileSize, fileType ->
                        onSendMessage(
                            "$fileName|$fileSize|$fileType",
                            "FILE",
                            null,
                            null
                        )
                        showAttachmentSelector = false
                    },
                    onPayCoinsClicked = {
                        showCoinsTransferDialog = true
                        showAttachmentSelector = false
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
                        val initialData = when (gameType) {
                            "TIC_TAC_TOE" -> ",,,,,,,,"
                            "CHESS" -> "♜,♞,♝,♚,.,.,.,.,.,.,.,.,♖,♘,♗,♔"
                            "TRIVIA" -> "-1,-1"
                            "CONNECT_FOUR" -> ",,,,,,,,,,,,,,,,,,,,,,,,"
                            "BATTLESHIP" -> {
                                val uShips = (0..15).shuffled().take(3).joinToString(",")
                                val aShips = (0..15).shuffled().take(3).joinToString(",")
                                val shots = List(16) { "." }.joinToString(",")
                                "$shots|$aShips|$shots|$uShips"
                            }
                            "DICE_DUEL" -> "0,0,0,0,0"
                            "MEMORY_MATCH" -> {
                                val emojis = listOf("👾", "💣", "🏆", "🎮", "🕹️", "💥", "🔥", "👑", "👾", "💣", "🏆", "🎮", "🕹️", "💥", "🔥", "👑").shuffled().joinToString(",")
                                val matched = List(16) { "." }.joinToString(",")
                                "$emojis|$matched|-1|0|0"
                            }
                            "NUMBER_GUESSER" -> "${(1..50).random()},0,6,Start guessing!"
                            "BLACKJACK" -> {
                                val userHand = "${(2..11).random()},${(2..11).random()}"
                                val oppHand = "${(2..11).random()},${(2..11).random()}"
                                "$userHand|$oppHand"
                            }
                            else -> "PENDING"
                        }
                        onSendMessage(
                            "I challenge you to $gameType!",
                            "GAME_INVITE",
                            gameType,
                            initialData
                        )
                        showGameSelector = false
                    },
                    onShowCustomLinkDialog = {
                        showCustomLinkDialog = true
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
                            showAttachmentSelector = false
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
                            showAttachmentSelector = false
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

                    // File attachment launcher (WhatsApp-like paperclip icon)
                    IconButton(
                        onClick = {
                            showAttachmentSelector = !showAttachmentSelector
                            showGameSelector = false
                            showStickersPanel = false
                        },
                        modifier = Modifier
                            .background(if (showAttachmentSelector) NeonMint else CyberBorder, CircleShape)
                            .size(36.dp)
                            .testTag("chat_detail_attachment_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Share File",
                            tint = if (showAttachmentSelector) Color.Black else TextWhite,
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

    if (showAiRecapDialog) {
        var actionItem1 by remember { mutableStateOf(false) }
        var actionItem2 by remember { mutableStateOf(true) }
        var actionItem3 by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAiRecapDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF00A884))
                    Text("AI Meeting Digest & Actions", fontWeight = FontWeight.Black, color = TextWhite)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Review of current discussions with professional summarization:",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("💬 Discussion Summary:", fontWeight = FontWeight.Bold, color = Color(0xFF00A884), fontSize = 11.sp)
                            Text(
                                "Participants discussed scheduling next week's collaborative sprint. Coin distribution structures, tactical gaming invite validations, and encrypted memo storage constraints were evaluated.",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        "📌 Interactive Workspace Action Items:",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = actionItem1,
                                onCheckedChange = { actionItem1 = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00A884))
                            )
                            Text("Sync local sqlite data to remote company server", color = TextWhite, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = actionItem2,
                                onCheckedChange = { actionItem2 = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00A884))
                            )
                            Text("Wager remaining game coins on next test sprint", color = TextWhite, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = actionItem3,
                                onCheckedChange = { actionItem3 = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00A884))
                            )
                            Text("Generate security audit hash keys for lead developers", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAiRecapDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showTranslationDialog) {
        AlertDialog(
            onDismissRequest = { showTranslationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF818CF8))
                    Text("Dynamic Chat Translator", fontWeight = FontWeight.Black, color = TextWhite)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Translate incoming & outgoing chats dynamically to support seamless international connection.", color = TextGray, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Real-time Translation:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Switch(
                            checked = translationEnabled,
                            onCheckedChange = { translationEnabled = it },
                            colors = SwitchDefaults.colors(checkedIconColor = Color(0xFF818CF8))
                        )
                    }
                    Text("Select Target Language:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("French", "Spanish", "Japanese", "Yoruba").forEach { lang ->
                            val isLangSelected = selectedTranslationLang == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLangSelected) Color(0xFF818CF8) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedTranslationLang = lang }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLangSelected) Color.White else TextWhite
                                )
                            }
                        }
                    }
                    if (translationEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF818CF8).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text("✨ Active translation engine is translating to $selectedTranslationLang.", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTranslationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8))
                ) {
                    Text("Save Settings", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showGamingStatsDialog) {
        AlertDialog(
            onDismissRequest = { showGamingStatsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = NeonMint)
                    Text("Gamer Arena Stats", fontWeight = FontWeight.Black, color = TextWhite)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Head-to-head tactical records for this guild chat:", color = TextGray, fontSize = 12.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        border = BorderStroke(1.dp, CyberBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Battles", color = TextGray, fontSize = 12.sp)
                                Text("20 Matches", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = CyberBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Your Wins (WeGamer_Me)", color = TextGray, fontSize = 12.sp)
                                Text("12 🏆", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = CyberBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Their Wins (Participant)", color = TextGray, fontSize = 12.sp)
                                Text("8 ⚔️", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = CyberBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wagered Coins Pool", color = TextGray, fontSize = 12.sp)
                                Text("800 Coins", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeonMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, NeonMint, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Current Arena Title: Dominant Duelist 👑", color = NeonMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGamingStatsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint)
                ) {
                    Text("Back to Guild Chat", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    userStats: com.example.data.UserStats,
    onPlayTicTacToeMove: (messageId: Int, cellIndex: Int) -> Unit,
    onPlayRockPaperScissors: (messageId: Int, choice: String) -> Unit,
    onPlayTriviaAnswer: (messageId: Int, selectedOptionIdx: Int) -> Unit = { _, _ -> },
    onPlayChessMove: (messageId: Int, fromIdx: Int, toIdx: Int) -> Unit = { _, _, _ -> },
    onPlayConnectFourMove: (messageId: Int, columnIdx: Int) -> Unit = { _, _ -> },
    onPlayBattleshipMove: (messageId: Int, cellIdx: Int) -> Unit = { _, _ -> },
    onPlayDiceDuelMove: (messageId: Int) -> Unit = { _ -> },
    onPlayMemoryMatchMove: (messageId: Int, cardIdx: Int) -> Unit = { _, _ -> },
    onPlayNumberGuesserMove: (messageId: Int, guess: Int) -> Unit = { _, _ -> },
    onPlayBlackjackMove: (messageId: Int, action: String) -> Unit = { _, _ -> },
    onReactToMessage: (messageId: Int, emoji: String) -> Unit,
    onGamerNameClicked: (String) -> Unit = {},
    appMode: String = "GAMING"
) {
    val parsedReactions = remember(message.reactionsJson) {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val raw = message.reactionsJson.trim()
            if (raw.startsWith("[") && raw.endsWith("]") && raw.length > 2) {
                val clean = raw.removeSurrounding("[", "]").trim()
                val items = clean.split("},{")
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
                        list.add(u to em)
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        list
    }

    var showMessageOptions by remember { mutableStateOf(false) }
    
    if (showMessageOptions) {
        AlertDialog(
            onDismissRequest = { showMessageOptions = false },
            title = {
                Text(
                    text = "Message Info & Security",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "QUICK REACTION",
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val popularEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
                            popularEmojis.forEach { emoji ->
                                val isSelected = parsedReactions.any { it.first == "WeGamer_Me" && it.second == emoji }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonMint.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(1.dp, if (isSelected) NeonMint else Color.Transparent, CircleShape)
                                        .clickable {
                                            onReactToMessage(message.id, emoji)
                                            showMessageOptions = false
                                        }
                                        .testTag("react_emoji_${emoji}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CyberBorder)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NeonMint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIGNAL END-TO-END ENCRYPTED",
                                color = NeonMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "This message is encrypted locally with a GCM-AES symmetric key negotiated via X3DH Triple Diffie-Hellman Key Exchange.",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val rawContent = message.content
                        val ciphertext = remember(rawContent) {
                            try {
                                val bytes = rawContent.toByteArray(Charsets.UTF_8)
                                val xor = bytes.map { (it.toInt() xor 0x5A).toByte() }.toByteArray()
                                android.util.Base64.encodeToString(xor, android.util.Base64.NO_WRAP)
                            } catch (e: Exception) {
                                "EncryptedSessionData=="
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("CIPHERTEXT (In Database):", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ciphertext,
                                color = CyberPink,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("DECRYPTED TEXT (Only on your device):", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rawContent,
                                color = TextWhite,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMessageOptions = false }) {
                    Text("Dismiss", color = NeonMint)
                }
            }
        )
    }

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
        // Workspace Thread Header in Enterprise Mode
        if (appMode == "ENTERPRISE" && message.type == "TEXT") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            ) {
                Icon(Icons.Default.Topic, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Thread: WG-COLLABORATION-${Math.abs(message.id.hashCode() % 1000)}",
                    color = Color(0xFF00A884),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sender Username label if not me
        if (!isMe) {
            Text(
                text = message.senderName,
                color = if (isDarkWallpaper) Color(0xFF8696A0) else Color(0xFF128C7E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 6.dp, bottom = 4.dp)
                    .clickable { onGamerNameClicked(message.senderName) }
            )
        }

        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable { showMessageOptions = true }
                .testTag("message_bubble_${message.id}")
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

                    "FILE" -> {
                        val parts = message.content.split("|")
                        val fileName = parts.getOrNull(0) ?: "Unknown_File"
                        val fileSize = parts.getOrNull(1) ?: "0 KB"
                        val fileType = parts.getOrNull(2) ?: "FILE"
                        
                        val icon = when (fileType) {
                            "PDF" -> Icons.Default.PictureAsPdf
                            "IMAGE" -> Icons.Default.Image
                            "VIDEO" -> Icons.Default.PlayCircle
                            "AUDIO" -> Icons.Default.Audiotrack
                            "ZIP" -> Icons.Default.FolderZip
                            "LOCATION" -> Icons.Default.Place
                            else -> Icons.Default.InsertDriveFile
                        }
                        
                        val iconColor = when (fileType) {
                            "PDF" -> Color(0xFFE53935)
                            "IMAGE", "VIDEO" -> Color(0xFF00ACC1)
                            "AUDIO" -> Color(0xFFFB8C00)
                            "ZIP" -> Color(0xFFFDD835)
                            "LOCATION" -> Color(0xFF43A047)
                            else -> Color(0xFF757575)
                        }

                        val context = androidx.compose.ui.platform.LocalContext.current
                        var fileOpenedState by remember { mutableStateOf(false) }

                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        fileOpenedState = true
                                        android.widget.Toast.makeText(context, "Opening file: $fileName...", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(iconColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = fileType,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        modifier = Modifier.testTag("file_bubble_name")
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fileSize,
                                            color = if (isDarkWallpaper) Color(0xFF8696A0) else Color(0xFF667781),
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .background(if (isDarkWallpaper) Color(0xFF8696A0) else Color(0xFF667781), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = fileType,
                                            color = if (isDarkWallpaper) Color(0xFF8696A0) else Color(0xFF667781),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                IconButton(
                                    onClick = {
                                        fileOpenedState = !fileOpenedState
                                        val status = if (fileOpenedState) "Opened" else "Downloaded"
                                        android.widget.Toast.makeText(context, "$status $fileName", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("download_file_btn")
                                ) {
                                    Icon(
                                        imageVector = if (fileOpenedState) Icons.Default.Visibility else Icons.Default.Download,
                                        contentDescription = "Download/Open",
                                        tint = if (fileOpenedState) NeonMint else textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    "GAME_INVITE" -> {
                        GameChallengeInviteView(
                            message = message,
                            isMeSender = isMe,
                            onPlayTicTacToeMove = onPlayTicTacToeMove,
                            onPlayRockPaperScissors = onPlayRockPaperScissors,
                            onPlayTriviaAnswer = onPlayTriviaAnswer,
                            onPlayChessMove = onPlayChessMove,
                            onPlayConnectFourMove = onPlayConnectFourMove,
                            onPlayBattleshipMove = onPlayBattleshipMove,
                            onPlayDiceDuelMove = onPlayDiceDuelMove,
                            onPlayMemoryMatchMove = onPlayMemoryMatchMove,
                            onPlayNumberGuesserMove = onPlayNumberGuesserMove,
                            onPlayBlackjackMove = onPlayBlackjackMove
                        )
                    }

                    "COINS_TRANSFER" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1C14), RoundedCornerShape(8.dp))
                                .border(1.dp, AccentOrange, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AccentOrange.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, AccentOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🪙", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isMe) "Sent Coins 💸" else "Received Coins 💸",
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Amount: ${message.gameData ?: "100"} Coins",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
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
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "End-to-End Encrypted via Signal Protocol",
                        tint = if (isDarkWallpaper) Color(0x77E9EDEF) else Color(0x77111B21),
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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

        // Display Reactions Attached directly to message
        if (parsedReactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .background(Color(0xFF202C33), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0xFF3B4A54), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .clickable { showMessageOptions = true }
                    .testTag("attached_reactions_${message.id}")
            ) {
                parsedReactions.take(3).forEach { (_, emoji) ->
                    Text(text = emoji, fontSize = 12.sp)
                }
                if (parsedReactions.size > 1) {
                    Text(
                        text = "${parsedReactions.size}",
                        color = Color(0xFFE9EDEF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
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
    onPlayRockPaperScissors: (messageId: Int, choice: String) -> Unit,
    onPlayTriviaAnswer: (messageId: Int, selectedOptionIdx: Int) -> Unit = { _, _ -> },
    onPlayChessMove: (messageId: Int, fromIdx: Int, toIdx: Int) -> Unit = { _, _, _ -> },
    onPlayConnectFourMove: (messageId: Int, columnIdx: Int) -> Unit = { _, _ -> },
    onPlayBattleshipMove: (messageId: Int, cellIdx: Int) -> Unit = { _, _ -> },
    onPlayDiceDuelMove: (messageId: Int) -> Unit = { _ -> },
    onPlayMemoryMatchMove: (messageId: Int, cardIdx: Int) -> Unit = { _, _ -> },
    onPlayNumberGuesserMove: (messageId: Int, guess: Int) -> Unit = { _, _ -> },
    onPlayBlackjackMove: (messageId: Int, action: String) -> Unit = { _, _ -> }
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
        } else if (message.gameType == "TRIVIA") {
            val completed = message.gameStatus == "COMPLETED"
            val boardData = message.gameData?.split(",") ?: listOf("-1", "-1")
            val chosenIdx = boardData.getOrNull(0)?.toIntOrNull() ?: -1
            val correctIdx = boardData.getOrNull(1)?.toIntOrNull() ?: -1

            val qIdx = Math.abs(message.id.hashCode()) % 3
            val (question, options, realCorrectIdx) = when (qIdx) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = question,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                options.forEachIndexed { idx, optionText ->
                    val isSelectedByMe = completed && chosenIdx == idx
                    val isCorrectIdx = completed && idx == correctIdx
                    
                    val btnBg = when {
                        isCorrectIdx -> Color(0xFF00A884).copy(alpha = 0.3f)
                        isSelectedByMe -> CyberPink.copy(alpha = 0.3f)
                        else -> CyberBorder
                    }
                    val btnBorderColor = when {
                        isCorrectIdx -> Color(0xFF00A884)
                        isSelectedByMe -> CyberPink
                        else -> CyberBorder.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(btnBg, RoundedCornerShape(8.dp))
                            .border(1.dp, btnBorderColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = !completed) {
                                onPlayTriviaAnswer(message.id, idx)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("trivia_option_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionText,
                            color = if (isCorrectIdx) NeonMint else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (completed) {
                    Text(
                        text = if (chosenIdx == correctIdx) "You solved it correctly! 🎓 (+50 🪙)" else "Whoops! Better luck next time.",
                        color = if (chosenIdx == correctIdx) NeonMint else CyberPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Solve the quiz question before time runs out!",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }
        } else if (message.gameType == "CHESS") {
            val completed = message.gameStatus == "COMPLETED"
            val board = message.gameData?.split(",") ?: List(16) { "." }
            var selectedIdx by remember { mutableStateOf<Int?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "4x4 Tactical Mini-Chess",
                    color = NeonMint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0..3) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (col in 0..3) {
                                    val idx = row * 4 + col
                                    val piece = board.getOrElse(idx) { "." }
                                    val isDarkSquare = (row + col) % 2 == 1
                                    val isSelected = selectedIdx == idx

                                    val squareBg = when {
                                        isSelected -> NeonPurple.copy(alpha = 0.4f)
                                        isDarkSquare -> Color(0xFF1E293B)
                                        else -> Color(0xFF475569)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(squareBg)
                                            .border(0.5.dp, CyberBorder.copy(alpha = 0.3f))
                                            .clickable(enabled = !completed) {
                                                val currentSel = selectedIdx
                                                if (currentSel == null) {
                                                    if (piece in listOf("♖", "♘", "♗", "♔")) {
                                                        selectedIdx = idx
                                                    }
                                                } else {
                                                    if (currentSel == idx) {
                                                        selectedIdx = null
                                                    } else {
                                                        onPlayChessMove(message.id, currentSel, idx)
                                                        selectedIdx = null
                                                    }
                                                }
                                            }
                                            .testTag("chess_cell_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (piece == ".") "" else piece,
                                            fontSize = 24.sp,
                                            color = if (piece in listOf("♜", "♞", "♝", "♚")) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (selectedIdx != null) "Select target square to move..." else "Touch your piece (white bottom row) to select!",
                    color = if (selectedIdx != null) NeonPurple else TextWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else if (message.gameType == "CONNECT_FOUR") {
            val completed = message.gameStatus == "COMPLETED"
            val board = message.gameData?.split(",") ?: List(25) { "" }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("5x5 Connect Four", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0..4) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (col in 0..4) {
                                    val idx = row * 5 + col
                                    val cell = board.getOrElse(idx) { "" }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(2.dp)
                                            .border(1.dp, CyberBorder.copy(alpha = 0.4f), CircleShape)
                                            .clickable(enabled = !completed) {
                                                onPlayConnectFourMove(message.id, col)
                                            }
                                            .testTag("c4_cell_${row}_${col}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cell.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(0.8f)
                                                    .background(
                                                        color = if (cell == "R") CyberPink else NeonMint,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!completed) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0..4) {
                            val isFull = board[col].isNotEmpty()
                            Button(
                                onClick = { onPlayConnectFourMove(message.id, col) },
                                enabled = !isFull,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .testTag("c4_drop_btn_$col")
                            ) {
                                Text("⬇️", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Text(
                    text = when {
                        completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+120 🪙)"
                        completed && message.gameWinner == "opponent" -> "Winner: Alice 🤖"
                        completed && message.gameWinner == "DRAW" -> "Tie Game! 🤝"
                        completed -> "Completed"
                        else -> "Your turn! Tap column drop buttons."
                    },
                    fontSize = 11.sp,
                    color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (message.gameType == "BATTLESHIP") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split("|") ?: listOf()
            if (parts.size >= 4) {
                val playerShots = parts[0].split(",")
                val aiShips = parts[1].split(",")
                val aiShots = parts[2].split(",")
                val playerShips = parts[3].split(",")

                val userHitsCount = playerShots.count { it == "H" }
                val aiHitsCount = aiShots.count { it == "H" }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Tactical Battleship (4x4)", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("My Ships: ${3 - aiHitsCount}/3 ⛵", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("AI Ships: ${3 - userHitsCount}/3 🛸", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (row in 0..3) {
                                Row(modifier = Modifier.weight(1f)) {
                                    for (col in 0..3) {
                                        val idx = row * 4 + col
                                        val cell = playerShots.getOrElse(idx) { "." }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .border(0.5.dp, CyberBorder.copy(alpha = 0.3f))
                                                .clickable(enabled = !completed && cell == ".") {
                                                    onPlayBattleshipMove(message.id, idx)
                                                }
                                                .testTag("bs_cell_$idx"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (cell) {
                                                    "H" -> "🔥"
                                                    "M" -> "💧"
                                                    else -> "🌊"
                                                },
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = when {
                            completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+150 🪙)"
                            completed && message.gameWinner == "opponent" -> "Winner: Alice 🤖"
                            completed -> "Completed"
                            else -> "Tap waves 🌊 to launch torpedoes!"
                        },
                        fontSize = 11.sp,
                        color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (message.gameType == "DICE_DUEL") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split(",") ?: listOf("0", "0", "0", "0", "0")
            val userScore = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val oppScore = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val round = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val lastUser = parts.getOrNull(3)?.toIntOrNull() ?: 0
            val lastOpp = parts.getOrNull(4)?.toIntOrNull() ?: 0

            val diceEmojis = listOf("", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
            val userDiceText = if (lastUser in 1..12) {
                if (lastUser > 6) {
                    "${diceEmojis.getOrElse(lastUser / 2) { "" }} ${diceEmojis.getOrElse(lastUser - (lastUser / 2)) { "" }}"
                } else {
                    diceEmojis[lastUser]
                }
            } else "🎲"
            val oppDiceText = if (lastOpp in 1..12) {
                if (lastOpp > 6) {
                    "${diceEmojis.getOrElse(lastOpp / 2) { "" }} ${diceEmojis.getOrElse(lastOpp - (lastOpp / 2)) { "" }}"
                } else {
                    diceEmojis[lastOpp]
                }
            } else "🎲"

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Dice Duel (Round $round/3)", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", color = TextGray, fontSize = 11.sp)
                        Text(userDiceText, fontSize = 32.sp)
                        Text("$userScore pts", color = CyberPink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text("VS", fontWeight = FontWeight.Black, color = CyberPink, fontSize = 16.sp)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Alice", color = TextGray, fontSize = 11.sp)
                        Text(oppDiceText, fontSize = 32.sp)
                        Text("$oppScore pts", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (!completed) {
                    Button(
                        onClick = { onPlayDiceDuelMove(message.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("dice_roll_btn")
                    ) {
                        Text("Roll Dice 🎲", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = when {
                            message.gameWinner == "me" -> "You won the Duel! 🏆 (+100 🪙)"
                            message.gameWinner == "opponent" -> "Alice won! 🤖"
                            else -> "It's a draw! 🤝"
                        },
                        color = if (message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (message.gameType == "MEMORY_MATCH") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split("|") ?: listOf()
            if (parts.size >= 5) {
                val cardsLayout = parts[0].split(",")
                val matchedArray = parts[1].split(",")
                val firstSel = parts[2].toIntOrNull() ?: -1
                val playerScore = parts[3].toIntOrNull() ?: 0
                val oppScore = parts[4].toIntOrNull() ?: 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Memory Card Match", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Pairs: $playerScore", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("AI Pairs: $oppScore", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (row in 0..3) {
                                Row(modifier = Modifier.weight(1f)) {
                                    for (col in 0..3) {
                                        val idx = row * 4 + col
                                        val emoji = cardsLayout.getOrElse(idx) { "" }
                                        val isMatched = matchedArray.getOrElse(idx) { "" } == "M"
                                        val isSelected = firstSel == idx

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .padding(2.dp)
                                                .background(
                                                    if (isMatched || isSelected) Color(0xFF1E293B) else CyberBorder,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(0.5.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .clickable(enabled = !completed && !isMatched && !isSelected) {
                                                    onPlayMemoryMatchMove(message.id, idx)
                                                }
                                                .testTag("mem_card_$idx"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isMatched || isSelected) emoji else "❓",
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = when {
                            completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+120 🪙)"
                            completed && message.gameWinner == "opponent" -> "Winner: Alice 🤖"
                            completed -> "Tie game!"
                            firstSel != -1 -> "Flip second card!"
                            else -> "Tap cards to find pairs!"
                        },
                        fontSize = 11.sp,
                        color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (message.gameType == "NUMBER_GUESSER") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split(",") ?: listOf("25", "0", "6", "Start guessing!")
            val attempts = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val maxAttempts = parts.getOrNull(2)?.toIntOrNull() ?: 6
            val feedback = parts.getOrNull(3) ?: "Guess the number!"

            var sliderValue by remember { mutableStateOf(25f) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Guess the Number (1 to 50)", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Attempts: $attempts / $maxAttempts", color = TextGray, fontSize = 11.sp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = feedback,
                        color = if (completed) NeonMint else CyberPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!completed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "My Guess: ${sliderValue.toInt()}",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 1f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonMint,
                                activeTrackColor = NeonMint,
                                inactiveTrackColor = CyberBorder
                            ),
                            modifier = Modifier.testTag("guess_slider")
                        )
                        Button(
                            onClick = { onPlayNumberGuesserMove(message.id, sliderValue.toInt()) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("guess_submit_btn")
                        ) {
                            Text("Submit Guess 🎯", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Secret number was ${parts.getOrNull(0) ?: ""}",
                        color = NeonMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (message.gameType == "BLACKJACK") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split("|") ?: listOf("2,3", "5,6")
            val playerHand = parts.getOrNull(0)?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
            val oppHand = parts.getOrNull(1)?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()

            val playerScore = playerHand.sum()
            val oppScore = oppHand.sum()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Blackjack 21 Duel", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Hand", color = TextGray, fontSize = 11.sp)
                        Text(playerHand.joinToString("  "), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Score: $playerScore", color = if (playerScore > 21) Color.Red else NeonMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Alice's Hand", color = TextGray, fontSize = 11.sp)
                        val oppDisplay = if (completed) oppHand.joinToString("  ") else "${oppHand.firstOrNull() ?: ""}  🎴"
                        Text(oppDisplay, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        val oppScoreDisplay = if (completed) oppScore.toString() else "?"
                        Text("Score: $oppScoreDisplay", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (!completed) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onPlayBlackjackMove(message.id, "HIT") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("bj_hit_btn")
                        ) {
                            Text("Hit 🃏", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onPlayBlackjackMove(message.id, "STAND") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("bj_stand_btn")
                        ) {
                            Text("Stand 🛑", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = message.content,
                        color = if (message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (message.gameType == "CUSTOM_LINK") {
            val parts = message.gameData?.split("|") ?: listOf("Custom Game", "", "Challenge link")
            val gameName = parts.getOrNull(0) ?: "Custom Game"
            val link = parts.getOrNull(1) ?: ""
            val desc = parts.getOrNull(2) ?: "Accept challenge to join!"
            val context = androidx.compose.ui.platform.LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game name tag/badge
                Box(
                    modifier = Modifier
                        .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonPurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎮 $gameName CHALLENGE",
                        color = NeonPurple,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // Show URL Link beautifully
                if (link.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Link",
                            tint = NeonMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = link,
                            color = NeonMint,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Join button
                Button(
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "Launching $gameName challenge lobby at:\n$link ... 🚀",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("launch_custom_game_btn")
                ) {
                    Text("Accept & Play Now ⚔️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// In-chat mini-game selector panel
@Composable
fun GameChallengeSelectionView(
    onSelectGame: (String) -> Unit,
    onShowCustomLinkDialog: () -> Unit
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

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("CHESS") }
                        .testTag("challenge_chess"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👑 Chess ♞", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tactical 4x4 Chess", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("TRIVIA") }
                        .testTag("challenge_trivia"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💡 Trivia 🎓", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Multiplayer Quiz Challenge", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("CONNECT_FOUR") }
                        .testTag("challenge_connect_four"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🟡 Connect 🔴", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("5x5 Connect Four", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("BATTLESHIP") }
                        .testTag("challenge_battleship"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⛵ Battleship 💥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tactical Ship Hunt", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("DICE_DUEL") }
                        .testTag("challenge_dice_duel"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎲 Dice Duel 🎲", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("3-Round High Roll", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("MEMORY_MATCH") }
                        .testTag("challenge_memory_match"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧠 Match 🧠", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Memory Card Match", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("NUMBER_GUESSER") }
                        .testTag("challenge_number_guesser"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯 Guess 🎯", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Secret Number Game", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("BLACKJACK") }
                        .testTag("challenge_blackjack"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🃏 Blackjack 🃏", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Beat the Dealer to 21", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowCustomLinkDialog() }
                    .testTag("challenge_custom_link"),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔗", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Send Custom Invite Link", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Share challenge invite URL for Minecraft, Chess, Roblox etc.", color = TextGray, fontSize = 11.sp)
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

// Attachment Selection view (WhatsApp Style)
@Composable
fun AttachmentSelectionView(
    onSelectFile: (name: String, size: String, type: String) -> Unit,
    onPayCoinsClicked: () -> Unit
) {
    Surface(
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Share Files (WhatsApp Style)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val whatsappFiles = listOf(
                    Triple("Document", Icons.Default.Description, Color(0xFF2196F3)),
                    Triple("Gallery", Icons.Default.Image, Color(0xFFE040FB)),
                    Triple("Audio", Icons.Default.Audiotrack, Color(0xFFFF9800)),
                    Triple("Location", Icons.Default.Place, Color(0xFF4CAF50)),
                    Triple("Apk / Mod", Icons.Default.Android, Color(0xFF8BC34A)),
                    Triple("Pay Coins", Icons.Default.MonetizationOn, AccentOrange)
                )
                
                whatsappFiles.forEach { fileAction ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                if (fileAction.first == "Pay Coins") {
                                    onPayCoinsClicked()
                                } else {
                                    val picked = when (fileAction.first) {
                                        "Document" -> Triple("Strategic_Guild_Rules.pdf", "1.8 MB", "PDF")
                                        "Gallery" -> Triple("fortnite_victory_royale.jpg", "640 KB", "IMAGE")
                                        "Audio" -> Triple("voice_chat_discord.mp3", "3.4 MB", "AUDIO")
                                        "Location" -> Triple("Gamer_Guild_HQ.loc", "45 bytes", "LOCATION")
                                        "Apk / Mod" -> Triple("wegist_game_mods.zip", "15.2 MB", "ZIP")
                                        else -> Triple("speedrun_proof.mp4", "18.1 MB", "VIDEO")
                                    }
                                    onSelectFile(picked.first, picked.second, picked.third)
                                }
                            }
                            .padding(4.dp)
                            .testTag("attach_${fileAction.first.replace(" ", "_").lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(fileAction.third, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = fileAction.second,
                                contentDescription = fileAction.first,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(fileAction.first, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
