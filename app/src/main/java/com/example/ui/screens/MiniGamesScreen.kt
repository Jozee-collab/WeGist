package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStats
import com.example.data.Friend
import com.example.ui.components.BrickBreakerGame
import com.example.ui.components.SpaceShooterGame
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GameCategory(val displayName: String, val icon: String) {
    ALL("All Games", "🔥"),
    GAME_PIGEON("GamePigeon", "🐦"),
    CLASSIC("Classic & Casual", "🎲"),
    PUZZLE("Puzzle & Word", "🧩"),
    RACING("Racing & Action", "🏎️"),
    PARTY("Social & Party", "🃏")
}

data class GameModel(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val category: GameCategory,
    val color: Color,
    val isGamePigeon: Boolean = false,
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
    onPerformDailyCheckIn: () -> Unit = {},
    onShareBragPost: (String, String) -> Unit = { _, _ -> },
    onToggleTheme: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedGameKey by remember { mutableStateOf<String?>(null) } // "SPACE" or "BRICK" for canvas engine
    var selectedCategory by remember { mutableStateOf(GameCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var leaderboardTab by remember { mutableStateOf("Global") } // Global or Friends

    // Active Game Modal State
    var activeModalGame by remember { mutableStateOf<GameModel?>(null) }

    // Master List of Games
    val allGames = remember {
        listOf(
            // GAMEPIGEON CLASSIC & CASUAL
            GameModel("8_ball_pool", "8‑Ball Pool", "Classic cue sport table duel with solid & stripe balls, angle aiming, and bank shots.", "🎱", GameCategory.CLASSIC, Color(0xFF3B82F6), true, "8_ball_pool"),
            GameModel("mini_golf", "Mini Golf", "Aim your putter, measure power, and sink hole-in-ones across neon mini golf courses.", "⛳", GameCategory.CLASSIC, Color(0xFF10B981), true, "mini_golf"),
            GameModel("basketball_shootout", "Basketball Shootout", "Swish hoops against the clock or challenge friends in a 3-point shootout.", "🏀", GameCategory.CLASSIC, Color(0xFFF97316), true, "basketball_shootout"),
            GameModel("cup_pong", "Cup Pong", "Toss ping-pong balls into cups across the table with perfect angle & arc trajectory.", "🥤", GameCategory.CLASSIC, Color(0xFFEF4444), true, "cup_pong"),
            GameModel("darts", "Darts", "Hit 501 or Bullseye in precision dartboard throws with wind velocity simulation.", "🎯", GameCategory.CLASSIC, Color(0xFF8B5CF6), true, "darts"),
            GameModel("archery", "Archery", "Pull back the bowstring and hit target rings considering wind speed and gravity.", "🏹", GameCategory.CLASSIC, Color(0xFFEC4899), true, "archery"),
            GameModel("mancala", "Mancala", "Traditional stone-sowing board game. Strategically capture seeds in your store.", "🪵", GameCategory.CLASSIC, Color(0xFFD97706), true, "mancala"),
            GameModel("checkers", "Checkers", "Jump opponent pieces and king your red or black draughts on an 8x8 grid.", "🔴", GameCategory.CLASSIC, Color(0xFFDC2626), true, "checkers"),
            GameModel("chess", "Chess", "Strategic checkmate battle with tactical opening moves and timer clocks.", "♟️", GameCategory.CLASSIC, Color(0xFF6B7280), true, "chess"),
            GameModel("four_in_a_row", "Four in a Row", "Connect four colored discs vertically, horizontally, or diagonally before your rival.", "🟡", GameCategory.CLASSIC, Color(0xFFEAB308), true, "four_in_a_row"),

            // PUZZLE & WORD GAMES
            GameModel("word_hunt", "Word Hunt", "Compete to find words in a 4x4 letter grid by swiping adjacent letters.", "🔍", GameCategory.PUZZLE, Color(0xFF06B6D4), true, "word_hunt"),
            GameModel("anagrams", "Anagrams", "Rearrange 6 scrambled letters to form valid target words before time expires.", "🔤", GameCategory.PUZZLE, Color(0xFFA855F7), true, "anagrams"),
            GameModel("crossword", "Crossword Puzzles", "Bite‑sized daily crossword challenges with clue hints and grid answers.", "📰", GameCategory.PUZZLE, Color(0xFF14B8A6), false, "crossword"),
            GameModel("trivia_crack", "Trivia Crack", "Quick trivia battles across Science, Pop Culture, History, and Sports.", "🧠", GameCategory.PUZZLE, Color(0xFFF43F5E), false, "trivia_crack"),

            // RACING & ACTION
            GameModel("mini_car_racing", "Mini Car Racing", "Turn‑based racing challenges, steering around track obstacles and hazards.", "🏎️", GameCategory.RACING, Color(0xFFEF4444), false, "mini_car_racing"),
            GameModel("tank_battle", "Tank Battle", "Strategic duels with friends, aiming turret cannons with bouncing shells.", "🛡️", GameCategory.RACING, Color(0xFF84CC16), false, "tank_battle"),
            GameModel("paintball", "Paintball", "Fast shooting mini‑game with splatter targets and reaction timing.", "🎨", GameCategory.RACING, Color(0xFF3B82F6), false, "paintball"),
            GameModel("space_shooter", "Space Shooter Arcade", "Protect orbital borders by blasting space rocks and alien ships with pulse lasers!", "🚀", GameCategory.RACING, NeonMint, false, "space_shooter"),
            GameModel("brick_breaker", "Brick Breaker Retro", "Smash stacks of glowing bricks with bouncy neon physical balls.", "👾", GameCategory.CLASSIC, NeonPurple, false, "brick_breaker"),

            // SOCIAL & PARTY
            GameModel("truth_or_dare", "Truth or Dare", "Automated prompts in chat with interactive spin wheel or choice generator.", "🎭", GameCategory.PARTY, Color(0xFFF59E0B), false, "truth_or_dare"),
            GameModel("would_you_rather", "Would You Rather", "Fun dilemmas with instant voting and community percentage statistics.", "⚖️", GameCategory.PARTY, Color(0xFF6366F1), false, "would_you_rather"),
            GameModel("draw_something", "Draw Something", "Sketch and guess game with interactive drawing canvas, brush sizes, and guess box.", "✏️", GameCategory.PARTY, Color(0xFF10B981), false, "draw_something")
        )
    }

    // Filtered Games
    val filteredGames = remember(selectedCategory, searchQuery) {
        allGames.filter { game ->
            val matchesCategory = when (selectedCategory) {
                GameCategory.ALL -> true
                GameCategory.GAME_PIGEON -> game.isGamePigeon
                else -> game.category == selectedCategory
            }
            val matchesSearch = searchQuery.isBlank() ||
                    game.title.contains(searchQuery, ignoreCase = true) ||
                    game.desc.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "WeGist Arcade",
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NeonMint.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, NeonMint)
                        ) {
                            Text(
                                "GamePigeon Included 🐦",
                                color = NeonMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
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
                // Interactive Canvas Engine Games (Space Shooter or Brick Breaker)
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
                            text = if (selectedGameKey == "space_shooter") "PLAYING SPACE SHOOTER 🚀" else "PLAYING BRICK BREAKER 👾",
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

                    if (selectedGameKey == "space_shooter") {
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    // Quick stats balance & streak
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

                    // Daily Streak Card
                    item {
                        val lastCheckIn = userStats.lastCheckInTime
                        val now = System.currentTimeMillis()
                        val alreadyCheckedIn = (now - lastCheckIn) < 24 * 60 * 60 * 1000L && lastCheckIn > 0

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (alreadyCheckedIn) Color(0xFF131B15) else Color(0xFF2B1C13)
                            ),
                            border = BorderStroke(1.dp, if (alreadyCheckedIn) NeonMint else AccentOrange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("streak_check_in_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (alreadyCheckedIn) NeonMint.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f),
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                if (alreadyCheckedIn) NeonMint else AccentOrange,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (alreadyCheckedIn) "🔥" else "⚡", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Daily Play Streak: ${userStats.dailyStreak} Days",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (alreadyCheckedIn) "Streak Active! Multiplier: x${minOf(5, userStats.dailyStreak)}" else "Tap to check-in & keep streak alive!",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (!alreadyCheckedIn) {
                                            onPerformDailyCheckIn()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (alreadyCheckedIn) NeonMint.copy(alpha = 0.2f) else AccentOrange,
                                        contentColor = if (alreadyCheckedIn) NeonMint else Color.Black
                                    ),
                                    enabled = !alreadyCheckedIn,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp).testTag("checkin_streak_btn")
                                ) {
                                    Text(
                                        text = if (alreadyCheckedIn) "Active" else "Check In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // HERO FEATURED BANNER: GamePigeon iMessage Bundle Hub
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            border = BorderStroke(1.5.dp, Color(0xFF818CF8)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = GameCategory.GAME_PIGEON }
                                .testTag("game_pigeon_hero_banner")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🐦", fontSize = 32.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "GamePigeon Arcade Bundle",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 17.sp
                                            )
                                            Text(
                                                "Most Popular iMessage Casual Games",
                                                color = Color(0xFFC7D2FE),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFF4F46E5),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "12 GAMES",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    "Enjoy 8-Ball Pool, Cup Pong, Basketball, Mini Golf, Darts, Archery, Mancala, Checkers, Chess, Four in a Row, Word Hunt & Anagrams inside chats!",
                                    color = Color(0xFFE0E7FF),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(listOf("🎱 Pool", "⛳ Golf", "🏀 Hoops", "🥤 Cup Pong", "🎯 Darts", "🏹 Archery", "🔍 Word Hunt", "🔤 Anagrams")) { chip ->
                                        Surface(
                                            color = Color(0xFF312E81),
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFF6366F1))
                                        ) {
                                            Text(
                                                chip,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search input & Category Tabs
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search 23+ mini-games...", color = TextGray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonMint) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextGray)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonMint,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("game_search_input")
                        )
                    }

                    // Category Selector Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(GameCategory.values()) { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            "${category.icon} ${category.displayName}",
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonMint,
                                        selectedLabelColor = Color.Black,
                                        containerColor = CyberCard,
                                        labelColor = TextGray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = NeonMint,
                                        borderColor = CyberBorder
                                    ),
                                    modifier = Modifier.testTag("category_chip_${category.name}")
                                )
                            }
                        }
                    }

                    // Category Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedCategory.displayName} (${filteredGames.size})",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Game Items List
                    items(filteredGames, key = { it.id }) { game ->
                        GameItemCard(
                            game = game,
                            onClick = {
                                if (game.id == "space_shooter" || game.id == "brick_breaker") {
                                    selectedGameKey = game.id
                                } else {
                                    activeModalGame = game
                                }
                            }
                        )
                    }

                    // Leaderboards Section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Arcade Champions Leaderboard 🏆", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
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

                    // Leaderboard Rows
                    val boardItems = if (leaderboardTab == "Global") {
                        listOf(
                            LeaderboardEntry("Alice (8-Ball Pro)", 5850, "🥇"),
                            LeaderboardEntry("Bob_PoolMaster", 4980, "🥈"),
                            LeaderboardEntry("ProGamer99", 4220, "🥉"),
                            LeaderboardEntry(userStats.username, userStats.wins * 150 + 500, "4th")
                        )
                    } else {
                        listOf(
                            LeaderboardEntry("Alice (8-Ball Pro)", 5850, "🥇"),
                            LeaderboardEntry(userStats.username, userStats.wins * 150 + 500, "🥈"),
                            LeaderboardEntry("Bob_PoolMaster", 4980, "🥉")
                        )
                    }

                    itemsIndexed(boardItems) { index, entry ->
                        LeaderboardRow(index = index, entry = entry)
                    }
                }
            }
        }
    }

    // ACTIVE INTERACTIVE GAME DIALOG / SHEET
    if (activeModalGame != null) {
        val game = activeModalGame!!
        InteractiveGameDialog(
            game = game,
            friendsList = friendsList,
            userStats = userStats,
            onAwardCoins = onAwardCoins,
            onInviteFriend = onInviteFriend,
            onShareBragPost = onShareBragPost,
            onDismiss = { activeModalGame = null }
        )
    }
}

@Composable
fun GameItemCard(
    game: GameModel,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("game_card_${game.tag}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(game.color.copy(alpha = 0.15f), CircleShape)
                    .border(1.5.dp, game.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(game.icon, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(game.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (game.isGamePigeon) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF4F46E5).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF818CF8))
                        ) {
                            Text(
                                "GamePigeon 🐦",
                                color = Color(0xFFA5B4FC),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(game.desc, color = TextGray, fontSize = 12.sp, maxLines = 2, lineHeight = 15.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(game.color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "PLAY 🎮",
                    color = if (game.color == NeonMint || game.color == Color(0xFFEAB308)) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// INTERACTIVE DIALOG WITH CUSTOM MINI-GAME ENGINES
@Composable
fun InteractiveGameDialog(
    game: GameModel,
    friendsList: List<Friend>,
    userStats: UserStats,
    onAwardCoins: (Int) -> Unit,
    onInviteFriend: (String, String) -> Unit,
    onShareBragPost: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var gameScore by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var gameCompleted by remember { mutableStateOf(false) }
    var bragged by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberCard,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(game.color.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, game.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(game.icon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(game.title, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text(game.category.displayName, color = game.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(game.desc, color = TextGray, fontSize = 12.sp)

                HorizontalDivider(color = CyberBorder, thickness = 1.dp)

                // Render specific interactive component according to game ID!
                when (game.id) {
                    "would_you_rather" -> WouldYouRatherGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "truth_or_dare" -> TruthOrDareGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "draw_something" -> DrawSomethingGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "word_hunt" -> WordHuntGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "anagrams" -> AnagramsGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "trivia_crack" -> TriviaCrackGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "basketball_shootout" -> BasketballGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "cup_pong" -> CupPongGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "8_ball_pool" -> Pool8BallGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "mini_golf" -> MiniGolfGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "darts" -> DartsGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "archery" -> ArcheryGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    "four_in_a_row" -> FourInARowGameView(onScore = { score -> gameScore += score; gameCompleted = true })
                    else -> GenericArcadeGameView(game = game, onScore = { score -> gameScore = score; gameCompleted = true })
                }

                if (gameCompleted) {
                    val earnedCoins = maxOf(10, gameScore / 10)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSlate),
                        border = BorderStroke(1.dp, NeonMint),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Match Completed! 🎉", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Score: $gameScore pts • Reward: 🪙 +$earnedCoins Coins", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!bragged) {
                                    onShareBragPost(game.title, "$gameScore pts")
                                    onAwardCoins(50)
                                    bragged = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("brag_game_score_btn")
                        ) {
                            Text(if (bragged) "Bragged ✓ (+50 🪙)" else "Brag Feed 📣 (+50🪙)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onAwardCoins(earnedCoins)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("claim_game_reward_btn")
                        ) {
                            Text("Claim 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Challenge Friends Section
                Spacer(modifier = Modifier.height(4.dp))
                Text("Challenge Friends in Chat ⚔️", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                if (friendsList.isEmpty()) {
                    Text("No friends online yet. Open Chats to invite friends!", color = TextGray, fontSize = 11.sp)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friendsList) { friend ->
                            Surface(
                                color = CyberSlate,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, CyberBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(friend.username, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            onInviteFriend(friend.userId, game.title)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp).testTag("challenge_friend_${friend.userId}")
                                    ) {
                                        Text("Invite", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextGray)
            }
        }
    )
}

// 1. WOULD YOU RATHER GAME ENGINE
@Composable
fun WouldYouRatherGameView(onScore: (Int) -> Unit) {
    var votedOption by remember { mutableStateOf<Int?>(null) }
    val optA = "Have infinite Arcade Coins 🪙"
    val optB = "Never lose a Mini-Game duel 🏆"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Make your choice & see live community percentages:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Surface(
            color = if (votedOption == 1) NeonMint.copy(alpha = 0.2f) else CyberSlate,
            border = BorderStroke(1.dp, if (votedOption == 1) NeonMint else CyberBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().clickable {
                if (votedOption == null) {
                    votedOption = 1
                    onScore(350)
                }
            }
        ) {
            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("A: $optA", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (votedOption != null) {
                    Text("68%", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        Surface(
            color = if (votedOption == 2) NeonPurple.copy(alpha = 0.2f) else CyberSlate,
            border = BorderStroke(1.dp, if (votedOption == 2) NeonPurple else CyberBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().clickable {
                if (votedOption == null) {
                    votedOption = 2
                    onScore(350)
                }
            }
        ) {
            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("B: $optB", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (votedOption != null) {
                    Text("32%", color = NeonPurple, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

// 2. TRUTH OR DARE GAME ENGINE
@Composable
fun TruthOrDareGameView(onScore: (Int) -> Unit) {
    var prompt by remember { mutableStateOf("Select Truth or Dare to generate prompt!") }
    var mode by remember { mutableStateOf("") }

    val truths = listOf(
        "What is the funniest message you ever received in WeGist?",
        "Who is your favorite gaming buddy in the app?",
        "What mini-game are you most addicted to?"
    )
    val dares = listOf(
        "Send a 5-second voice note speaking like a robot in your chat!",
        "Brag your highest score to the public social feed right now!",
        "Challenge 3 friends to an 8-Ball Pool match!"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = CyberSlate,
            border = BorderStroke(1.dp, CyberBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text(prompt, color = TextWhite, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(10.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    mode = "TRUTH"
                    prompt = truths.random()
                    onScore(300)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("TRUTH 💡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    mode = "DARE"
                    prompt = dares.random()
                    onScore(400)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("DARE 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3. DRAW SOMETHING CANVAS ENGINE
@Composable
fun DrawSomethingGameView(onScore: (Int) -> Unit) {
    var paths = remember { mutableStateListOf<Path>() }
    var currentPath = remember { Path() }
    var selectedColor by remember { mutableStateOf(NeonMint) }
    var wordToDraw = remember { listOf("Cyber Dragon 🐉", "Pizza 🍕", "Space Rocket 🚀", "Guitar 🎸").random() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Secret Word: $wordToDraw", color = AccentOrange, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Button(
                onClick = {
                    paths.clear()
                    onScore(450)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Done / Clear", fontSize = 10.sp, color = TextWhite)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            paths.add(newPath)
                        },
                        onDrag = { change, _ ->
                            paths.lastOrNull()?.lineTo(change.position.x, change.position.y)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { p ->
                    drawPath(path = p, color = selectedColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
                }
            }
        }
    }
}

// 4. WORD HUNT GAME ENGINE
@Composable
fun WordHuntGameView(onScore: (Int) -> Unit) {
    val grid = remember {
        listOf(
            listOf('C', 'A', 'T', 'S'),
            listOf('O', 'P', 'E', 'N'),
            listOf('O', 'V', 'E', 'R'),
            listOf('L', 'I', 'F', 'E')
        )
    }
    var currentGuess by remember { mutableStateOf("") }
    var foundWords = remember { mutableStateListOf<String>() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tap letters to form words (e.g. CATS, OPEN, OVER, LIFE):", color = TextWhite, fontSize = 11.sp)

        Surface(color = CyberSlate, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Word: $currentGuess", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(6.dp))
        }

        LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(140.dp)) {
            items(16) { idx ->
                val r = idx / 4
                val c = idx % 4
                val letter = grid[r][c]
                Surface(
                    color = CyberSlate,
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable {
                        currentGuess += letter
                    }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text("$letter", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentGuess = "" }, colors = ButtonDefaults.buttonColors(containerColor = CyberBorder), modifier = Modifier.weight(1f)) {
                Text("Clear", fontSize = 11.sp)
            }
            Button(onClick = {
                if (currentGuess.length >= 3 && !foundWords.contains(currentGuess)) {
                    foundWords.add(currentGuess)
                    onScore(foundWords.size * 200)
                }
                currentGuess = ""
            }, colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black), modifier = Modifier.weight(1f)) {
                Text("Submit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 5. ANAGRAMS GAME ENGINE
@Composable
fun AnagramsGameView(onScore: (Int) -> Unit) {
    val scrambled = remember { listOf('R', 'E', 'A', 'C', 'T', 'S') }
    var wordInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Rearrange letters to spell words:", color = TextWhite, fontSize = 11.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            scrambled.forEach { l ->
                Surface(
                    color = Color(0xFFA855F7),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { wordInput += l }
                ) {
                    Text("$l", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }

        Surface(color = CyberSlate, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
            Text(if (wordInput.isEmpty()) "Tap tiles above..." else wordInput, color = NeonMint, fontWeight = FontWeight.Black, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
        }

        Button(onClick = {
            if (wordInput.length >= 3) {
                onScore(wordInput.length * 100)
            }
            wordInput = ""
        }, colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black), modifier = Modifier.fillMaxWidth()) {
            Text("Submit Word 🔤", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// 6. TRIVIA CRACK GAME ENGINE
@Composable
fun TriviaCrackGameView(onScore: (Int) -> Unit) {
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    val q = "Which planet is known as the Red Planet?"
    val opts = listOf("Venus", "Mars", "Jupiter", "Saturn")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Q: $q", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)

        opts.forEachIndexed { i, opt ->
            val isCorrect = i == 1
            val isSelected = selectedIdx == i

            Surface(
                color = when {
                    selectedIdx != null && isCorrect -> NeonMint.copy(alpha = 0.3f)
                    selectedIdx != null && isSelected && !isCorrect -> Color.Red.copy(alpha = 0.3f)
                    else -> CyberSlate
                },
                border = BorderStroke(1.dp, if (isSelected) NeonMint else CyberBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    if (selectedIdx == null) {
                        selectedIdx = i
                        if (isCorrect) onScore(500) else onScore(100)
                    }
                }
            ) {
                Text("${i + 1}. $opt", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

// 7. BASKETBALL SHOOTOUT ENGINE
@Composable
fun BasketballGameView(onScore: (Int) -> Unit) {
    var meterPos by remember { mutableStateOf(0.5f) }
    var shotsMade by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        var dir = 0.05f
        while (shotsMade < 3) {
            delay(30)
            meterPos += dir
            if (meterPos > 0.95f || meterPos < 0.05f) dir = -dir
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Swish Hoops! Tap SHOOT when indicator is in green center!", color = TextWhite, fontSize = 11.sp)

        LinearProgressIndicator(
            progress = meterPos,
            color = if (meterPos in 0.4f..0.6f) NeonMint else AccentOrange,
            trackColor = CyberSlate,
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
        )

        Button(
            onClick = {
                if (meterPos in 0.38f..0.62f) {
                    shotsMade++
                    onScore(shotsMade * 300)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SHOOT BASKETBALL 🏀 (Made: $shotsMade/3)", fontWeight = FontWeight.Bold)
        }
    }
}

// 8. CUP PONG ENGINE
@Composable
fun CupPongGameView(onScore: (Int) -> Unit) {
    var power by remember { mutableStateOf(0.5f) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Adjust trajectory slider & toss pong ball into cups:", color = TextWhite, fontSize = 11.sp)

        Slider(value = power, onValueChange = { power = it }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red))

        Button(
            onClick = {
                val score = if (power in 0.45f..0.55f) 600 else 150
                onScore(score)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("TOSS PONG BALL 🥤", fontWeight = FontWeight.Bold)
        }
    }
}

// 9. 8-BALL POOL ENGINE
@Composable
fun Pool8BallGameView(onScore: (Int) -> Unit) {
    var angle by remember { mutableStateOf(45f) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Set cue stick angle & strike cue ball:", color = TextWhite, fontSize = 11.sp)

        Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..90f, colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6)))

        Text("Cue Angle: ${angle.toInt()}°", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Button(
            onClick = { onScore(480) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("STRIKE CUE BALL 🎱", fontWeight = FontWeight.Bold)
        }
    }
}

// 10. MINI GOLF ENGINE
@Composable
fun MiniGolfGameView(onScore: (Int) -> Unit) {
    var power by remember { mutableStateOf(0.5f) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Putter Power Control:", color = TextWhite, fontSize = 11.sp)

        Slider(value = power, onValueChange = { power = it }, valueRange = 0.1f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981)))

        Button(
            onClick = { onScore(520) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PUTT GOLF BALL ⛳", fontWeight = FontWeight.Bold)
        }
    }
}

// 11. DARTS ENGINE
@Composable
fun DartsGameView(onScore: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Throw dart at the precision dartboard:", color = TextWhite, fontSize = 11.sp)

        Button(
            onClick = { onScore(501) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("THROW DART 🎯 (Bullseye!)", fontWeight = FontWeight.Bold)
        }
    }
}

// 12. ARCHERY ENGINE
@Composable
fun ArcheryGameView(onScore: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Wind: 2.4 mph West • Pull bowstring back:", color = TextWhite, fontSize = 11.sp)

        Button(
            onClick = { onScore(450) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RELEASE ARROW 🏹", fontWeight = FontWeight.Bold)
        }
    }
}

// 13. FOUR IN A ROW GRID ENGINE
@Composable
fun FourInARowGameView(onScore: (Int) -> Unit) {
    var board by remember { mutableStateOf(List(6) { MutableList(7) { 0 } }) }
    var turn by remember { mutableStateOf(1) } // 1 for Red, 2 for Yellow

    Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Connect 4 discs in a row to win:", color = TextWhite, fontSize = 11.sp)

        for (r in 0..5) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0..6) {
                    val cell = board[r][c]
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                when (cell) {
                                    1 -> Color.Red
                                    2 -> Color.Yellow
                                    else -> CyberSlate
                                },
                                CircleShape
                            )
                            .border(1.dp, CyberBorder, CircleShape)
                            .clickable {
                                // Drop disc in column c
                                for (rowIdx in 5 downTo 0) {
                                    if (board[rowIdx][c] == 0) {
                                        board[rowIdx][c] = turn
                                        turn = if (turn == 1) 2 else 1
                                        onScore(400)
                                        break
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}

// GENERIC ARCADE SIMULATION
@Composable
fun GenericArcadeGameView(game: GameModel, onScore: (Int) -> Unit) {
    var playScore by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tap button repeatedly to score points in ${game.title}!", color = TextWhite, fontSize = 12.sp)

        Button(
            onClick = {
                playScore += 100
                onScore(playScore)
            },
            colors = ButtonDefaults.buttonColors(containerColor = game.color),
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Text("PLAY! ⚡", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }

        Text("Current Score: $playScore pts", color = game.color, fontWeight = FontWeight.Black, fontSize = 14.sp)
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
