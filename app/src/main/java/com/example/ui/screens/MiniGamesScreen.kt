package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStats
import com.example.data.Friend
import com.example.ui.components.BrickBreakerGame
import com.example.ui.components.SpaceShooterGame
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class SimulatedGame(
    val title: String,
    val desc: String,
    val icon: String,
    val color: Color,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    userStats: UserStats,
    friendsList: List<Friend>,
    onAwardCoins: (coins: Int) -> Unit,
    onInviteFriend: (String, String) -> Unit,
    onSearchClicked: () -> Unit,
    onToggleTheme: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedGameKey by remember { mutableStateOf<String?>(null) } // null means showing game list
    var leaderboardTab by remember { mutableStateOf("Global") } // Global or Friends

    // Simulated game states
    var selectedSimulatedGame by remember { mutableStateOf<SimulatedGame?>(null) }
    var showMatchPlaySim by remember { mutableStateOf(false) }
    var playScore by remember { mutableStateOf(0) }
    var playTimerSeconds by remember { mutableStateOf(5) }
    var isPlayFinished by remember { mutableStateOf(false) }

    LaunchedEffect(showMatchPlaySim) {
        if (showMatchPlaySim) {
            playScore = 0
            playTimerSeconds = 5
            isPlayFinished = false
            while (playTimerSeconds > 0) {
                delay(1000)
                playTimerSeconds--
            }
            isPlayFinished = true
            val earnedCoins = playScore / 10
            if (earnedCoins > 0) {
                onAwardCoins(earnedCoins)
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "WeGist Arcade",
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onToggleTheme(!userStats.isDarkMode) },
                        modifier = Modifier.testTag("arcade_theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (userStats.isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = "Toggle Theme",
                            tint = TextWhite
                        )
                    }
                    IconButton(
                        onClick = onSearchClicked,
                        modifier = Modifier.testTag("arcade_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Global Search",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = CyberSlate)
            )
        },
        containerColor = CyberSlate,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedGameKey != null) {
                // Play active game
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedGameKey == "SPACE") "PLAYING SPACE SHOOTER" else "PLAYING BRICK BREAKER",
                            color = NeonMint,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { selectedGameKey = null },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("exit_game_btn")
                        ) {
                            Text("Exit Game", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedGameKey == "SPACE") {
                        SpaceShooterGame(
                            onGameComplete = { score ->
                                val rewardedCoins = score / 10
                                if (rewardedCoins > 0) onAwardCoins(rewardedCoins)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        BrickBreakerGame(
                            onGameComplete = { score ->
                                val rewardedCoins = score / 10
                                if (rewardedCoins > 0) onAwardCoins(rewardedCoins)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Game Marketplace Selector
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    // Quick stats balance
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCard),
                            border = BorderStroke(1.dp, CyberBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Arcade Token Balance", color = TextGray, fontSize = 12.sp)
                                    Text("🪙 ${userStats.coins} Coins", color = AccentOrange, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Matches Won", color = TextGray, fontSize = 12.sp)
                                    Text("🏆 ${userStats.wins} Wins", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    // Games deck list
                    item {
                        Text("Arcade Game Store", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }

                    item {
                        GameStoreItemCard(
                            title = "Space Shooter Arcade",
                            desc = "Protect orbital borders by blasting space rocks and alien ships with neon pulse lasers!",
                            icon = "🚀",
                            color = NeonMint,
                            onClick = { selectedGameKey = "SPACE" },
                            tag = "space_shooter"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Brick Breaker Retro",
                            desc = "Smash stacks of glowing bricks with bouncy neon physical balls. Accelerates as you break blocks!",
                            icon = "👾",
                            color = NeonPurple,
                            onClick = { selectedGameKey = "BRICK" },
                            tag = "brick_breaker"
                        )
                    }

                    // Add 10 more games
                    item {
                        GameStoreItemCard(
                            title = "Neon Snake Classic",
                            desc = "Navigate your cybernetic snake through neon grids to eat pixel pellets and grow indefinitely.",
                            icon = "🐍",
                            color = Color(0xFF00FFCC),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Neon Snake Classic",
                                    desc = "Navigate your cybernetic snake through neon grids to eat pixel pellets and grow indefinitely.",
                                    icon = "🐍",
                                    color = Color(0xFF00FFCC),
                                    tag = "neon_snake"
                                )
                            },
                            tag = "neon_snake"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Pac-Neon Cyber",
                            desc = "Munch on glowing dot energy while escaping colorful AI ghosts in a neon cyberpunk maze.",
                            icon = "🍬",
                            color = Color(0xFFFF0055),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Pac-Neon Cyber",
                                    desc = "Munch on glowing dot energy while escaping colorful AI ghosts in a neon cyberpunk maze.",
                                    icon = "🍬",
                                    color = Color(0xFFFF0055),
                                    tag = "pac_neon"
                                )
                            },
                            tag = "pac_neon"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Tetris Grid Lock",
                            desc = "Stack neon multi-shaped blocks to clear horizontal lines. Speeds up as the matrix fills!",
                            icon = "🧩",
                            color = Color(0xFFFF9900),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Tetris Grid Lock",
                                    desc = "Stack neon multi-shaped blocks to clear horizontal lines. Speeds up as the matrix fills!",
                                    icon = "🧩",
                                    color = Color(0xFFFF9900),
                                    tag = "tetris_grid"
                                )
                            },
                            tag = "tetris_grid"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Retro Space Invaders",
                            desc = "Defend your ship against incoming waves of alien pixel invaders descending from outer space.",
                            icon = "🛸",
                            color = Color(0xFF00E5FF),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Retro Space Invaders",
                                    desc = "Defend your ship against incoming waves of alien pixel invaders descending from outer space.",
                                    icon = "🛸",
                                    color = Color(0xFF00E5FF),
                                    tag = "space_invaders"
                                )
                            },
                            tag = "space_invaders"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Cyber Chess Master",
                            desc = "Engage in competitive galactic board game matches with retro cyber visuals and real-time tactics.",
                            icon = "♟️",
                            color = Color(0xFFE0E0E0),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Cyber Chess Master",
                                    desc = "Engage in competitive galactic board game matches with retro cyber visuals and real-time tactics.",
                                    icon = "♟️",
                                    color = Color(0xFFE0E0E0),
                                    tag = "cyber_chess"
                                )
                            },
                            tag = "cyber_chess"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Flappy Retro Drone",
                            desc = "Steer a glowing nanite drone through high-voltage neon barriers. Don't touch the laser walls!",
                            icon = "🚁",
                            color = Color(0xFFD4FF00),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Flappy Retro Drone",
                                    desc = "Steer a glowing nanite drone through high-voltage neon barriers. Don't touch the laser walls!",
                                    icon = "🚁",
                                    color = Color(0xFFD4FF00),
                                    tag = "flappy_drone"
                                )
                            },
                            tag = "flappy_drone"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Matrix Runner 2099",
                            desc = "Infinite vertical run inside a glowing computer core. Dodge falling code bits and grab speed-boosts!",
                            icon = "🏃",
                            color = Color(0xFF00FF66),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Matrix Runner 2099",
                                    desc = "Infinite vertical run inside a glowing computer core. Dodge falling code bits and grab speed-boosts!",
                                    icon = "🏃",
                                    color = Color(0xFF00FF66),
                                    tag = "matrix_runner"
                                )
                            },
                            tag = "matrix_runner"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Word Neon Speller",
                            desc = "Test your vocabulary speed! Rearrange neon letters to decode cyber terms and unlock massive combos.",
                            icon = "🔤",
                            color = Color(0xFFFF00FF),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Word Neon Speller",
                                    desc = "Test your vocabulary speed! Rearrange neon letters to decode cyber terms and unlock massive combos.",
                                    icon = "🔤",
                                    color = Color(0xFFFF00FF),
                                    tag = "word_neon"
                                )
                            },
                            tag = "word_neon"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Cyber Poker Duel",
                            desc = "Play arcade rules Texas Hold'em against retro AI masters in the neon backroom saloon.",
                            icon = "🃏",
                            color = Color(0xFFFFCC00),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Cyber Poker Duel",
                                    desc = "Play arcade rules Texas Hold'em against retro AI masters in the neon backroom saloon.",
                                    icon = "🃏",
                                    color = Color(0xFFFFCC00),
                                    tag = "cyber_poker"
                                )
                            },
                            tag = "cyber_poker"
                        )
                    }

                    item {
                        GameStoreItemCard(
                            title = "Bumper Cars Grid",
                            desc = "Drive a glowing neon vehicle, smash your opponents out of the matrix arena, and be the last one standing!",
                            icon = "🚗",
                            color = Color(0xFFFF3333),
                            onClick = {
                                selectedSimulatedGame = SimulatedGame(
                                    title = "Bumper Cars Grid",
                                    desc = "Drive a glowing neon vehicle, smash your opponents out of the matrix arena, and be the last one standing!",
                                    icon = "🚗",
                                    color = Color(0xFFFF3333),
                                    tag = "bumper_cars"
                                )
                            },
                            tag = "bumper_cars"
                        )
                    }

                    // Leaderboard section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("WeGist Leaderboards", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            // Tab switcher
                            Row(
                                modifier = Modifier
                                    .background(CyberBorder, RoundedCornerShape(8.dp))
                                    .padding(2.dp)
                            ) {
                                listOf("Global", "Friends").forEach { tab ->
                                    val active = leaderboardTab == tab
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (active) NeonPurple else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { leaderboardTab = tab }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(tab, color = if (active) Color.White else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Leaderboard items
                    val boardItems = if (leaderboardTab == "Global") {
                        listOf(
                            LeaderboardEntry("Alice (Retro Gamer)", 4850, "🥇"),
                            LeaderboardEntry("Bob_Destroyer", 3980, "🥈"),
                            LeaderboardEntry("ProGamer99", 3520, "🥉"),
                            LeaderboardEntry(userStats.username, userStats.wins * 150 + 400, "4th")
                        )
                    } else {
                        listOf(
                            LeaderboardEntry("Alice (Retro Gamer)", 4850, "🥇"),
                            LeaderboardEntry(userStats.username, userStats.wins * 150 + 400, "🥈"),
                            LeaderboardEntry("Bob_Destroyer", 3980, "🥉")
                        )
                    }

                    itemsIndexed(boardItems) { index, entry ->
                        LeaderboardRow(index = index, entry = entry)
                    }
                }
            }
        }
    }

    // SIMULATED GAME & CHALLENGE DIALOG
    if (selectedSimulatedGame != null) {
        val game = selectedSimulatedGame!!
        AlertDialog(
            onDismissRequest = {
                selectedSimulatedGame = null
                showMatchPlaySim = false
            },
            containerColor = CyberCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(game.color.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, game.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(game.icon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(game.title, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(game.desc, color = TextGray, fontSize = 13.sp)

                    Divider(color = CyberBorder, thickness = 1.dp)

                    if (!showMatchPlaySim) {
                        // Regular View: Info, Play Sim, or Challenge Friend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Best High Score:", color = TextGray, fontSize = 12.sp)
                            Text("🏆 ${(1000..9900).random()} pts", color = AccentOrange, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        Button(
                            onClick = { showMatchPlaySim = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.fillMaxWidth().testTag("play_sim_match_${game.tag}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Play Quick Match 🚀 (Earn Coins!)", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Challenge Friends", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        if (friendsList.isEmpty()) {
                            Text("No friends connected yet. Head to Chats to add gaming friends!", color = TextGray, fontSize = 12.sp)
                        } else {
                            friendsList.forEach { friend ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberSlate),
                                    border = BorderStroke(0.5.dp, CyberBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(friend.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Button(
                                            onClick = {
                                                onInviteFriend(friend.userId, game.title)
                                                selectedSimulatedGame = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.testTag("challenge_friend_${friend.userId}_${game.tag}")
                                        ) {
                                            Text("Challenge ⚔️", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // PLAY SIMULATOR ACTIVE CHIP-TAPPER MATCH!
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isPlayFinished) "MATCH COMPLETED!" else "TAP BUTTON AS FAST AS POSSIBLE!",
                                color = if (isPlayFinished) NeonMint else AccentOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time Remaining", color = TextGray, fontSize = 11.sp)
                                    Text("$playTimerSeconds s", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Simulated Score", color = TextGray, fontSize = 11.sp)
                                    Text("$playScore pts", color = game.color, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                            }

                            if (!isPlayFinished) {
                                Button(
                                    onClick = { playScore += 120 },
                                    colors = ButtonDefaults.buttonColors(containerColor = game.color),
                                    shape = CircleShape,
                                    modifier = Modifier.size(96.dp).testTag("sim_tap_btn")
                                ) {
                                    Text("TAP! ⚡", color = if (game.color == NeonMint) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            } else {
                                val earned = playScore / 10
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberSlate),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Rewarded Coins", color = TextGray, fontSize = 12.sp)
                                        Text("🪙 +$earned Coins", color = AccentOrange, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        showMatchPlaySim = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("claim_sim_coins_btn")
                                ) {
                                    Text("Claim Rewards & Exit 🏆", color = TextWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedSimulatedGame = null
                        showMatchPlaySim = false
                    }
                ) {
                    Text("Close", color = TextWhite)
                }
            }
        )
    }
}

@Composable
fun GameStoreItemCard(
    title: String,
    desc: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("arcade_store_card_$tag")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, color = TextGray, fontSize = 12.sp, maxLines = 2)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("PLAY 🎮", color = if (color == NeonMint) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    index: Int,
    entry: LeaderboardEntry
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberCard, RoundedCornerShape(10.dp))
            .border(0.5.dp, CyberBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(CyberBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.rankTag, color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(entry.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = "Points", tint = AccentOrange, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${entry.score} pts", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

data class LeaderboardEntry(
    val name: String,
    val score: Int,
    val rankTag: String
)
