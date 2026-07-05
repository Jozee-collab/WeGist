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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
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
import com.example.ui.components.BrickBreakerGame
import com.example.ui.components.SpaceShooterGame
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    userStats: UserStats,
    onAwardCoins: (coins: Int) -> Unit,
    onSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGameKey by remember { mutableStateOf<String?>(null) } // null means showing game list
    var leaderboardTab by remember { mutableStateOf("Global") } // Global or Friends

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
