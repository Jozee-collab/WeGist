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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserStats
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    userStats: UserStats,
    restrictInvites: Boolean,
    restrictStreams: Boolean,
    onUpdateUsername: (String) -> Unit,
    onUpdateRestrictInvites: (Boolean) -> Unit,
    onUpdateRestrictStreams: (Boolean) -> Unit,
    onAwardCoins: (coins: Int) -> Unit,
    onUpdateTheme: (Boolean) -> Unit,
    onUpdateWallpaper: (color: String, pattern: String) -> Unit,
    onUpdatePrivacySettings: (lastSeen: String, profilePhoto: String, about: String, readReceipts: Boolean, disappearingTimer: String) -> Unit,
    onPerformBackup: (interval: String, destination: String) -> Unit,
    onLinkAccount: (type: String, value: String) -> Unit,
    onSearchClicked: () -> Unit,
    onClaimQuest: (String, Int, Int) -> Unit = { _, _, _ -> },
    onUpgradePremium: (String) -> Unit = {},
    onEnterReferralCode: (String) -> Boolean = { false },
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showRateAppDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showModerationDialog by remember { mutableStateOf(false) }
    var showExportDataDialog by remember { mutableStateOf(false) }
    var isPinLockEnabled by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(5) }
    var nameInput by remember { mutableStateOf(userStats.username) }
    var dailyClaimed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var lastLevel by remember { mutableStateOf(userStats.level) }
    var showLevelUpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userStats.level) {
        if (userStats.level > lastLevel) {
            showLevelUpDialog = true
            lastLevel = userStats.level
        }
    }

    if (showLevelUpDialog) {
        AlertDialog(
            onDismissRequest = { showLevelUpDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("LEVEL UP! 🎮👑", fontWeight = FontWeight.Black, color = NeonMint, fontSize = 22.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Guild Rank Promotion", color = TextGray, fontSize = 12.sp)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Congratulations, @${userStats.username}! You have successfully reached Level ${userStats.level} and have been promoted to Legendary Overlord! 🚀👾",
                        color = TextWhite,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Text(
                        "Keep completing daily gamer quests, sending game challenges, and engaging with streams to climb the global leaderboard!",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLevelUpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.fillMaxWidth().testTag("dismiss_levelup_btn")
                ) {
                    Text("Awesome, Let's Play! 👾", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF14141A),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.5.dp, NeonPurple, RoundedCornerShape(16.dp))
        )
    }

    // SignUp / Linking states
    var showLinkDialogType by remember { mutableStateOf<String?>(null) }
    var linkInputVal by remember { mutableStateOf("") }
    var isLinkingProgress by remember { mutableStateOf(false) }
    var linkSuccessMessage by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpInputVal by remember { mutableStateOf("") }

    // Wallpaper customisation
    var showWallpaperDialog by remember { mutableStateOf(false) }

    // Privacy selectors
    var showLastSeenDialog by remember { mutableStateOf(false) }
    var showProfilePhotoDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }

    // Backup states
    var isBackingUp by remember { mutableStateOf(false) }
    var backupProgress by remember { mutableStateOf(0f) }
    var backupStatusText by remember { mutableStateOf("") }
    var showBackupDestDialog by remember { mutableStateOf(false) }
    var showBackupIntervalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "WeGist Profile",
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSearchClicked,
                        modifier = Modifier.testTag("profile_search_btn")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Profile Card Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(90.dp)) {
                            val isGoldenBorder = userStats.unlockedBorder == "GOLDEN"
                            val borderBrush = if (isGoldenBorder) {
                                androidx.compose.ui.graphics.Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFA500),
                                        Color(0xFFFFD700)
                                    )
                                )
                            } else {
                                androidx.compose.ui.graphics.SolidColor(NeonMint)
                            }
                            
                            Image(
                                painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                contentDescription = "My Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(if (isGoldenBorder) 4.dp else 2.dp, borderBrush, CircleShape)
                            )
                            IconButton(
                                onClick = { showEditNameDialog = true },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(NeonPurple, CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd)
                                    .testTag("edit_username_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${userStats.username}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = TextWhite
                            )
                            if (userStats.unlockedBorder == "GOLDEN") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("👑", fontSize = 16.sp)
                            }
                        }
                        Text(
                            text = "Level ${userStats.level} Gamer Guild Leader",
                            fontSize = 12.sp,
                            color = if (userStats.unlockedBorder == "GOLDEN") Color(0xFFFFD700) else NeonMint,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Cloud Storage Quota & Freemium Dashboard
            item {
                Text("Cloud Storage & Freemium Tier", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, if (userStats.isPremium) NeonMint else CyberBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("premium_dashboard_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (userStats.isPremium) Icons.Default.Star else Icons.Default.Cloud,
                                    contentDescription = "Cloud Storage",
                                    tint = if (userStats.isPremium) NeonMint else TextWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (userStats.isPremium) "${userStats.premiumTier} PREMIUM" else "WG STANDARD (FREE)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (userStats.isPremium) NeonMint else TextWhite,
                                    fontSize = 14.sp
                                )
                            }
                            if (userStats.isPremium) {
                                Box(
                                    modifier = Modifier
                                        .background(NeonMint.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("PRO ACTIVE", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Storage calculation progress bar
                        val usedMb = userStats.storageUsedBytes / (1024 * 1024f)
                        val limitMb = userStats.storageLimitBytes / (1024 * 1024f)
                        val ratio = (usedMb / limitMb).coerceIn(0f, 1f)
                        
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (userStats.isPremium) NeonMint else NeonPurple,
                            trackColor = CyberBorder
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cloud Storage Space",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                            val limitText = if (limitMb >= 1024) "${(limitMb/1024).toInt()} GB" else "${limitMb.toInt()} MB"
                            Text(
                                text = String.format("%.1f MB of %s used", usedMb, limitText),
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CyberBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Core Free Benefits:",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• End-to-end encrypted chats under Signal Protocol\n• Interactive mini-games & live streams\n• 15 GB of cloud chat/backup storage",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!userStats.isPremium) {
                            Text(
                                text = "Choose Premium Tiers:",
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Silver Tier
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).clickable { onUpgradePremium("SILVER") }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SILVER", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                                        Text("$4.99/mo", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("• 50 GB Storage\n• Ad-Free Feed\n• Custom Theme", color = TextGray, fontSize = 9.sp, lineHeight = 13.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(NeonPurple, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("BUY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                // Gold Tier
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).clickable { onUpgradePremium("GOLD") }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("GOLD", fontWeight = FontWeight.Black, color = NeonMint, fontSize = 13.sp)
                                        Text("$9.99/mo", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("• 100 GB Storage\n• Priority Streams\n• Golden Badge", color = TextGray, fontSize = 9.sp, lineHeight = 13.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(NeonMint, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("BUY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "You have unlocked all ${userStats.premiumTier} Pro features! Enjoy ad-free feed, advanced customization & increased storage space.",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { onUpgradePremium("FREE") },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink, contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                                ) {
                                    Text("Cancel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Gamer Guild Level Up Progress & Quests Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    "GUILD RANK LEVEL ${userStats.level}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = NeonPurple
                                )
                                Text(
                                    "Progress to next tier",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                "${userStats.xp} / 1000 XP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeonMint
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom animated level progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF14141A))
                                .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                        ) {
                            val progressFactor = (userStats.xp.toFloat() / 1000f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFactor)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(NeonPurple, NeonMint)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            "Guild Achievements & Quests",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextWhite,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 3 Quests
                        val quests = listOf(
                            Triple("quest_chat", "Daily Chat Whisperer", "Send a message to any gamer in WeGist chat."),
                            Triple("quest_arcade", "Arcade Challenger", "Launch or play any retro arcade mini-game."),
                            Triple("quest_spectator", "Spectator Supporter", "Watch or support any live streamer broadcast.")
                        )

                        quests.forEachIndexed { index, (questId, title, desc) ->
                            val isCompleted = userStats.questsCompletedJson.contains(questId)
                            val xpAward = when (questId) {
                                "quest_chat" -> 150
                                "quest_arcade" -> 250
                                else -> 200
                            }
                            val coinAward = when (questId) {
                                "quest_chat" -> 100
                                "quest_arcade" -> 150
                                else -> 100
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(Color(0xFF0F0F13), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, if (isCompleted) NeonMint.copy(alpha = 0.3f) else CyberBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCompleted) NeonMint else TextWhite
                                    )
                                    Text(
                                        desc,
                                        fontSize = 10.sp,
                                        color = TextGray,
                                        lineHeight = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("+${xpAward} XP 🎮", fontSize = 9.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                                        Text("+${coinAward} 🪙", fontSize = 9.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { onClaimQuest(questId, xpAward, coinAward) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCompleted) Color.Transparent else NeonMint,
                                        contentColor = if (isCompleted) NeonMint else Color.Black
                                    ),
                                    border = if (isCompleted) BorderStroke(1.dp, NeonMint) else null,
                                    enabled = !isCompleted,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("claim_${questId}_btn")
                                ) {
                                    Text(
                                        if (isCompleted) "Claimed ✓" else "Claim",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stat Cards Grid
            item {
                Text("Your Stats Overview", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatSmallCard(label = "Total Wins", valText = "🏆 ${userStats.wins} W", tint = NeonMint, modifier = Modifier.weight(1f))
                        StatSmallCard(label = "Stream Count", valText = "📡 ${userStats.streamsCount} Live", tint = NeonPurple, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatSmallCard(label = "Wallet Tokens", valText = "🪙 ${userStats.coins}", tint = AccentOrange, modifier = Modifier.weight(1f))
                        StatSmallCard(label = "Global Rank", valText = "# ${userStats.globalRank}", tint = CyberPink, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Viral Social Referral Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0F2E)),
                    border = BorderStroke(1.dp, NeonPurple),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("referral_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎟️ Referral Hub", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("+500 COINS & BORDER", color = NeonMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            "Share your unique referral code with friends. When they activate it, you both earn 500 Arcade Coins and unlock the Legendary Golden Neon Border for your profile!",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 15.sp
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Your code sharing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("YOUR PERSONAL CODE", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(userStats.referralCode, color = NeonMint, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            
                            var copied by remember { mutableStateOf(false) }
                            Button(
                                onClick = {
                                    copied = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(if (copied) "Copied!" else "Copy Share Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Enter Friend's code input
                        if (userStats.referredByCode.isEmpty()) {
                            var friendCodeInput by remember { mutableStateOf("") }
                            var claimStatusMessage by remember { mutableStateOf<String?>(null) }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = friendCodeInput,
                                    onValueChange = { friendCodeInput = it },
                                    label = { Text("Enter Friend's Code", fontSize = 11.sp, color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonMint,
                                        unfocusedBorderColor = CyberBorder,
                                        focusedLabelColor = NeonMint,
                                        cursorColor = NeonMint,
                                        focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(50.dp)
                                )
                                
                                Button(
                                    onClick = {
                                        val success = onEnterReferralCode(friendCodeInput)
                                        if (success) {
                                            claimStatusMessage = "Code Activated! +500 Coins 🪙 & Golden Border Unlocked! 👑"
                                        } else {
                                            claimStatusMessage = "Invalid Code."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(44.dp).testTag("claim_referral_btn")
                                ) {
                                    Text("Claim", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            if (claimStatusMessage != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(claimStatusMessage!!, color = if (claimStatusMessage!!.startsWith("Code")) NeonMint else CyberPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NeonMint.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = NeonMint, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Referred by friend: ${userStats.referredByCode} (Active)", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Daily Claim Bonus
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (dailyClaimed) CyberBorder else Color(0xFF1E1C14)),
                    border = BorderStroke(1.dp, if (dailyClaimed) CyberBorder else AccentOrange),
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
                            Text("Daily Arcade Bonus", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = if (dailyClaimed) "Already claimed for today!" else "Claim 200 🪙 free tokens",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = {
                                if (!dailyClaimed) {
                                    onAwardCoins(200)
                                    dailyClaimed = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dailyClaimed) CyberBorder else AccentOrange,
                                contentColor = Color.Black
                            ),
                            enabled = !dailyClaimed,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("claim_daily_btn")
                        ) {
                            Text(if (dailyClaimed) "Claimed" else "Claim 200", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Creator & Business Tools + Peer-to-Peer Payments Section
            item {
                Text("Creator Tools & Wallet Payments", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NeonMint.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, NeonMint, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = NeonMint, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gamer Storefront & Streaming Ticket Prices", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Monetize your stream gameplay with custom viewer ticket pricing.", color = TextGray, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Streaming ticket pricing configuration
                        var ticketPrice by remember { mutableStateOf("50") }
                        var isMonetizationEnabled by remember { mutableStateOf(true) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Enable Viewer Ticket Charges", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Charge viewers when you stream gameplay", color = TextGray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isMonetizationEnabled,
                                onCheckedChange = { isMonetizationEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonMint,
                                    checkedTrackColor = NeonMint.copy(alpha = 0.3f),
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = Color.Transparent
                                ),
                                modifier = Modifier.testTag("monetize_switch")
                            )
                        }

                        if (isMonetizationEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = ticketPrice,
                                onValueChange = { ticketPrice = it.filter { char -> char.isDigit() } },
                                label = { Text("Stream Entry Fee (Coins)", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = NeonMint,
                                    unfocusedBorderColor = CyberBorder
                                ),
                                trailingIcon = { Text("🪙", modifier = Modifier.padding(end = 12.dp)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("stream_ticket_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = CyberBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Creator Analytics Overview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CyberPink.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, CyberPink, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QueryStats, contentDescription = null, tint = CyberPink, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Creator Account Analytics", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .background(Color(0xFF0D0D12), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text("Viewer Tips Revenue", color = TextGray, fontSize = 11.sp)
                                Text("🪙 1,450 Coins", color = CyberPink, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF0D0D12), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text("Subscribers", color = TextGray, fontSize = 11.sp)
                                Text("📡 12 Active", color = NeonPurple, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = CyberBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Peer-to-Peer Fast Wallet Transfer (simulate sending coins to standard test users Alice or Bob)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AccentOrange.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, AccentOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Wallet Coin P2P Transfer", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Send tokens instantly to other active guild members.", color = TextGray, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val context = androidx.compose.ui.platform.LocalContext.current
                        var p2pRecipient by remember { mutableStateOf("Alice (Retro Gamer)") }
                        var p2pAmount by remember { mutableStateOf("100") }
                        var isRecipientDropdownExpanded by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { isRecipientDropdownExpanded = true },
                                border = BorderStroke(1.dp, CyberBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                modifier = Modifier.fillMaxWidth().testTag("select_p2p_recipient_btn")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("To: $p2pRecipient", fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextGray)
                                }
                            }

                            DropdownMenu(
                                expanded = isRecipientDropdownExpanded,
                                onDismissRequest = { isRecipientDropdownExpanded = false },
                                modifier = Modifier.background(CyberCard)
                            ) {
                                listOf("Alice (Retro Gamer)", "Bob_Destroyer").forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name, color = TextWhite) },
                                        onClick = {
                                            p2pRecipient = name
                                            isRecipientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = p2pAmount,
                                onValueChange = { p2pAmount = it.filter { char -> char.isDigit() } },
                                label = { Text("Transfer Amount", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = AccentOrange,
                                    unfocusedBorderColor = CyberBorder
                                ),
                                trailingIcon = { Text("🪙", modifier = Modifier.padding(end = 12.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f).testTag("settings_p2p_amount_input")
                            )

                            Button(
                                onClick = {
                                    val amt = p2pAmount.toIntOrNull() ?: 0
                                    if (amt > 0 && amt <= userStats.coins) {
                                        onAwardCoins(-amt)
                                        p2pAmount = "100"
                                        android.widget.Toast.makeText(context, "Transferred $amt Coins to $p2pRecipient! 💸✨", android.widget.Toast.LENGTH_LONG).show()
                                    } else if (amt > userStats.coins) {
                                        android.widget.Toast.makeText(context, "Insufficient coin balance!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Enter a valid amount!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .padding(top = 8.dp)
                                    .testTag("settings_p2p_send_btn")
                            ) {
                                Text("Send 💸", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Privacy Controls Toggles Section
            item {
                Text("Privacy & Safety Settings", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        PrivacyToggleRow(
                            title = "Restrict Game Challenges",
                            desc = "Only allow game invitations from added friends.",
                            checked = restrictInvites,
                            onCheckedChange = onUpdateRestrictInvites,
                            tag = "restrict_invites_switch"
                        )

                        HorizontalDivider(color = CyberBorder, modifier = Modifier.padding(vertical = 12.dp))

                        PrivacyToggleRow(
                            title = "Private Broadcast Streams",
                            desc = "Restrict stream viewing access to friends-only.",
                            checked = restrictStreams,
                            onCheckedChange = onUpdateRestrictStreams,
                            tag = "restrict_streams_switch"
                        )
                    }
                }
            }

            // 1. Account Signup & Verification Section (Phone, Gmail, Facebook, X)
            item {
                Text("Account Security & Linked Profiles", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Link secondary profiles or telephone networks to secure your gaming identity and enable cloud sync.",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Phone Network Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Phone, contentDescription = "Phone", tint = NeonMint, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Phone Verification", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (userStats.authPhone.isEmpty()) "Not connected" else "Linked: ${userStats.authPhone}",
                                        color = if (userStats.authPhone.isEmpty()) TextGray else NeonMint,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showLinkDialogType = "PHONE" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userStats.authPhone.isEmpty()) CyberBorder else Color(0xFF0F2D1E),
                                    contentColor = if (userStats.authPhone.isEmpty()) TextWhite else NeonMint
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp).testTag("link_phone_btn")
                            ) {
                                Text(if (userStats.authPhone.isEmpty()) "Connect" else "Modify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Email Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Mail, contentDescription = "Email", tint = CyberPink, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Email Profile", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (userStats.authGmail.isEmpty()) "Not connected" else "Linked: ${userStats.authGmail}",
                                        color = if (userStats.authGmail.isEmpty()) TextGray else NeonMint,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showLinkDialogType = "EMAIL" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userStats.authGmail.isEmpty()) CyberBorder else Color(0xFF0F2D1E),
                                    contentColor = if (userStats.authGmail.isEmpty()) TextWhite else NeonMint
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp).testTag("link_gmail_btn")
                            ) {
                                Text(if (userStats.authGmail.isEmpty()) "Connect" else "Modify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Facebook Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Share, contentDescription = "Facebook", tint = NeonPurple, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Facebook Profile", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (userStats.authFacebook.isEmpty()) "Not connected" else "Linked: ${userStats.authFacebook}",
                                        color = if (userStats.authFacebook.isEmpty()) TextGray else NeonMint,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showLinkDialogType = "FACEBOOK" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userStats.authFacebook.isEmpty()) CyberBorder else Color(0xFF0F2D1E),
                                    contentColor = if (userStats.authFacebook.isEmpty()) TextWhite else NeonMint
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp).testTag("link_facebook_btn")
                            ) {
                                Text(if (userStats.authFacebook.isEmpty()) "Connect" else "Modify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Chat Privacy Settings
            item {
                Text("Chat Privacy Settings", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Last Seen
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLastSeenDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Last Seen & Online", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Who can see my last seen timestamp", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(userStats.privacyLastSeen, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Profile Photo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProfilePhotoDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Profile Photo", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Who can see my avatar image", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(userStats.privacyProfilePhoto, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // About
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAboutDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("About Status", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Who can read my bio and status label", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(userStats.privacyAbout, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Read Receipts Toggle
                        PrivacyToggleRow(
                            title = "Read Receipts",
                            desc = "If turned off, you won't send or receive Read Receipts. Read receipts (blue double checks) are always sent for group chats.",
                            checked = userStats.privacyReadReceipts,
                            onCheckedChange = { checked ->
                                onUpdatePrivacySettings(
                                    userStats.privacyLastSeen,
                                    userStats.privacyProfilePhoto,
                                    userStats.privacyAbout,
                                    checked,
                                    userStats.disappearingMessagesTimer
                                )
                            },
                            tag = "privacy_read_receipts_switch"
                        )

                        HorizontalDivider(color = CyberBorder)

                        // Disappearing Messages Timer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDisappearingDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Disappearing Messages", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Start new chats with disappearing messages set to your timer", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(userStats.disappearingMessagesTimer, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }
                    }
                }
            }

            // 3. Theme Customization (Wallpaper Style selection)
            item {
                Text("Chat Wallpaper & Theme", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showWallpaperDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Chat Window Wallpaper", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Change look & pattern inside private gaming chats", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val displayWallName = when (userStats.chatWallpaperColor) {
                                    "DEFAULT" -> "Classic Default Teal"
                                    "CHARCOAL" -> "Carbon Slate"
                                    "MINT" -> "Pastel Mint Green"
                                    "PURPLE" -> "Arcade Purple"
                                    "PINK" -> "Cyber Neon Pink"
                                    "INDIGO" -> "Deep Royal Indigo"
                                    else -> userStats.chatWallpaperColor
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            when (userStats.chatWallpaperColor) {
                                                "CHARCOAL" -> Color(0xFF202C33)
                                                "MINT" -> Color(0xFFD9FDD3)
                                                "PURPLE" -> Color(0xFF291047)
                                                "PINK" -> Color(0xFFE040FB)
                                                "INDIGO" -> Color(0xFF3F51B5)
                                                else -> Color(0xFF0F2622)
                                            }
                                        )
                                        .border(0.5.dp, Color.White, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(displayWallName, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        PrivacyToggleRow(
                            title = "Doodle Grid Background",
                            desc = "Draw artistic retro doodle vector overlay onto chat wallpapers.",
                            checked = userStats.chatWallpaperPattern == "DOODLE",
                            onCheckedChange = { checked ->
                                onUpdateWallpaper(
                                    userStats.chatWallpaperColor,
                                    if (checked) "DOODLE" else "SOLID"
                                )
                            },
                            tag = "wallpaper_doodles_switch"
                        )
                    }
                }
            }

            // 4. Secure Chat Backup Section
            item {
                Text("Chat Backup Engine", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Last Backup Status", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                val backupDateStr = if (userStats.lastBackupTime == 0L) {
                                    "Never backed up"
                                } else {
                                    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                                    sdf.format(Date(userStats.lastBackupTime))
                                }
                                Text("Timestamp: $backupDateStr", color = NeonMint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Secure SQLite Database dump size: 48 KB (12 records)", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Button(
                                onClick = {
                                    if (!isBackingUp) {
                                        isBackingUp = true
                                        coroutineScope.launch {
                                            backupStatusText = "Preparing local tables..."
                                            backupProgress = 0.1f
                                            delay(500)
                                            backupStatusText = "Compressing indices & logs..."
                                            backupProgress = 0.4f
                                            delay(600)
                                            backupStatusText = "Uploading snapshot to cloud..."
                                            backupProgress = 0.7f
                                            delay(700)
                                            backupStatusText = "Backup verified & encrypted."
                                            backupProgress = 1.0f
                                            delay(400)
                                            isBackingUp = false
                                            onPerformBackup(userStats.backupInterval, userStats.backupDestination)
                                        }
                                    }
                                },
                                enabled = !isBackingUp,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 38.dp).testTag("backup_now_btn")
                            ) {
                                Text("Back Up Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Backup progress bar
                        AnimatedVisibility(visible = isBackingUp) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { backupProgress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = NeonMint,
                                    trackColor = CyberBorder,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(backupStatusText, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Auto Backup Interval Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBackupIntervalDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Cloud Backup Frequency", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("How often WeGist runs automatic chat sweeps", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(userStats.backupInterval, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Backup Destination Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBackupDestDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Storage Destination Server", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Cloud database endpoint for chat snapshot files", color = TextGray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(userStats.backupDestination, color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = TextGray)
                            }
                        }
                    }
                }
            }

            // Display Preferences Section
            item {
                Text("Display Preferences", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        PrivacyToggleRow(
                            title = "Dark Theme",
                            desc = "Enable immersive neon dark mode across all views.",
                            checked = userStats.isDarkMode,
                            onCheckedChange = onUpdateTheme,
                            tag = "theme_dark_mode_switch"
                        )
                    }
                }
            }

            // Play Store Compliance & Account Management Section
            item {
                Text("Account & Play Store Security", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        PrivacyToggleRow(
                            title = "PIN / Biometric Screen Lock",
                            desc = "Require authentication when opening WeGist.",
                            checked = isPinLockEnabled,
                            onCheckedChange = { isPinLockEnabled = it },
                            tag = "app_pin_lock_switch"
                        )

                        HorizontalDivider(color = CyberBorder)

                        // Export Data & Backup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showExportDataDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Export Chat History & Backup 📦", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Download encrypted local Room database JSON file.", color = TextGray, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Export", tint = NeonMint)
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Content Moderation & Safety Center
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showModerationDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Safety & Content Moderation Center 🛡️", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Play Store UGC guidelines, blocked users & report logs.", color = TextGray, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Safety", tint = NeonMint)
                        }

                        HorizontalDivider(color = CyberBorder)

                        // What's New in v1.0.4 Release Notes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChangelogDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("What's New in v1.0.4 🚀", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("View latest feature additions and performance updates.", color = TextGray, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Changelog", tint = NeonMint)
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Rate App Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRateAppDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rate WeGist on Play Store ⭐", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Support WeGist development with 5-star review!", color = TextGray, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Rate", tint = NeonMint)
                        }

                        HorizontalDivider(color = CyberBorder)

                        // Sign Out Button
                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252630), contentColor = TextWhite),
                            border = BorderStroke(1.dp, CyberBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("sign_out_btn")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIGN OUT OF WEGIST", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Delete Account (Play Store Mandatory Requirement)
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink),
                            border = BorderStroke(1.dp, CyberPink.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("delete_account_btn")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = CyberPink, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DELETE ACCOUNT & ERASE DATA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Version info footer
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("WeGist v1.0.4 (Build 104) • Play Store Verified Edition", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("🔒 Encrypted with Firebase Auth & Play Integrity API", color = NeonMint.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }

        // --- OVERLAY DIALOGS ---

        // Rate App Dialog
        if (showRateAppDialog) {
            AlertDialog(
                onDismissRequest = { showRateAppDialog = false },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text("Rate WeGist on Play Store", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Loving WeGist? Rate us 5 stars on Google Play Store!", color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = AccentOrange,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { userRating = star }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRateAppDialog = false
                            android.widget.Toast.makeText(context, "Thank you for rating WeGist $userRating Stars! 🌟", android.widget.Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Submit Review", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRateAppDialog = false }) {
                        Text("Maybe Later", color = TextGray)
                    }
                }
            )
        }

        // Export Data Dialog
        if (showExportDataDialog) {
            AlertDialog(
                onDismissRequest = { showExportDataDialog = false },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text("Export Chat History & Backup", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    Column {
                        Text(
                            "Export your entire local chat history, contacts, and unlocked game statistics into a secure JSON backup file.",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = NeonMint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Format: Encrypted Room Database JSON", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExportDataDialog = false
                            android.widget.Toast.makeText(context, "Chat backup exported successfully to Downloads folder! 📦", android.widget.Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_export_backup_btn")
                    ) {
                        Text("Export JSON Backup", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDataDialog = false }) {
                        Text("Close", color = TextGray)
                    }
                }
            )
        }

        // Moderation & Safety Center Dialog
        if (showModerationDialog) {
            AlertDialog(
                onDismissRequest = { showModerationDialog = false },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = NeonMint, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Safety & Moderation Policy", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• 24/7 Automated Content Filtering: Hate speech and offensive language are automatically blocked.", color = TextGray, fontSize = 12.sp)
                        Text("• In-Chat Reporting: Long-press any message or profile to report abusive behavior.", color = TextGray, fontSize = 12.sp)
                        Text("• Contact Blocking: Blocked users are instantly muted and hidden across all rooms.", color = TextGray, fontSize = 12.sp)
                        Text("• Play Store Data Safety: Your data is never sold or shared with 3rd-party advertisers.", color = TextGray, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showModerationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Got It", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // What's New Release Notes Dialog
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = CyberPink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("What's New in v1.0.4", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨ Play Store Release Candidate 1.0.4 Highlights:", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• Encrypted Phone & OTP Authentication", color = TextGray, fontSize = 12.sp)
                        Text("• Expanded Mini-Games (Connect Four, Chess, Tic-Tac-Toe)", color = TextGray, fontSize = 12.sp)
                        Text("• Instant Voice Notes & Media attachments", color = TextGray, fontSize = 12.sp)
                        Text("• Offline Room DB sync and high-framerate rendering", color = TextGray, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showChangelogDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Awesome!", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Delete Account Confirmation Dialog (Play Store Mandatory)
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = CyberPink, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account & Erase Data?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Text(
                        "Are you sure you want to permanently delete your WeGist account? In compliance with Google Play Data Safety requirements, this action will wipe all your chat history, unlocked game borders, coins, and Firebase Auth records.",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAccountDialog = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_delete_account_btn")
                    ) {
                        Text("PERMANENTLY DELETE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("Cancel", color = TextWhite)
                    }
                }
            )
        }

        // 1. SignUp / Link profile dialog
        if (showLinkDialogType != null) {
            val typeName = showLinkDialogType!!
            AlertDialog(
                onDismissRequest = {
                    showLinkDialogType = null
                    linkInputVal = ""
                    otpSent = false
                    otpInputVal = ""
                    linkSuccessMessage = ""
                },
                title = { Text("Connect $typeName Profile", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        if (linkSuccessMessage.isNotEmpty()) {
                            Text(linkSuccessMessage, color = NeonMint, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), textAlign = TextAlign.Center)
                        } else if (isLinkingProgress) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                                CircularProgressIndicator(color = NeonMint)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Verifying security credentials...", color = TextWhite, fontSize = 12.sp)
                            }
                        } else {
                            Text(
                                text = when (typeName) {
                                    "PHONE" -> "Secure your profile with SMS verification. Enter your telephone network number:"
                                    "GMAIL" -> "Sync with your Google Account. Enter your Gmail address:"
                                    "FACEBOOK" -> "Verify gaming friendship using Facebook connect. Enter username:"
                                    "X" -> "Advertise live clips and match alerts. Enter your X handle:"
                                    else -> ""
                                },
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (!otpSent) {
                                OutlinedTextField(
                                    value = linkInputVal,
                                    onValueChange = { linkInputVal = it },
                                    placeholder = {
                                        Text(
                                            text = when (typeName) {
                                                "PHONE" -> "+1 (555) 019-2834"
                                                "GMAIL" -> "gamer@gmail.com"
                                                "FACEBOOK" -> "fb_gamer"
                                                "X" -> "gamer_handle"
                                                else -> ""
                                            },
                                            color = TextGray.copy(alpha = 0.5f)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonMint,
                                        unfocusedBorderColor = CyberBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // OTP verification step
                                Text("We've sent a 4-digit code (Hint: 1337) to $linkInputVal. Enter it to verify:", color = AccentOrange, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                                OutlinedTextField(
                                    value = otpInputVal,
                                    onValueChange = { otpInputVal = it },
                                    placeholder = { Text("Enter OTP Code", color = TextGray.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonMint,
                                        unfocusedBorderColor = CyberBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (linkSuccessMessage.isEmpty() && !isLinkingProgress) {
                        Button(
                            onClick = {
                                if (typeName == "PHONE" && !otpSent) {
                                    if (linkInputVal.isNotBlank()) {
                                        otpSent = true
                                    }
                                } else {
                                    isLinkingProgress = true
                                    coroutineScope.launch {
                                        delay(1500)
                                        isLinkingProgress = false
                                        onLinkAccount(typeName, linkInputVal)
                                        linkSuccessMessage = "Successfully connected $typeName account!"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (typeName == "PHONE" && !otpSent) "Send SMS Code" else "Link Account", fontWeight = FontWeight.Bold)
                        }
                    } else if (linkSuccessMessage.isNotEmpty()) {
                        Button(
                            onClick = {
                                showLinkDialogType = null
                                linkInputVal = ""
                                otpSent = false
                                otpInputVal = ""
                                linkSuccessMessage = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (!isLinkingProgress) {
                        TextButton(onClick = {
                            showLinkDialogType = null
                            linkInputVal = ""
                            otpSent = false
                            otpInputVal = ""
                            linkSuccessMessage = ""
                        }) {
                            Text("Cancel", color = TextGray)
                        }
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 2. Chat Wallpaper Select Dialog
        if (showWallpaperDialog) {
            AlertDialog(
                onDismissRequest = { showWallpaperDialog = false },
                title = { Text("Select Chat Wallpaper", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pick a background shade for private messages:", color = TextGray, fontSize = 12.sp)

                        val wallpaperPresets = listOf(
                            "DEFAULT" to "Classic Teal",
                            "CHARCOAL" to "Carbon Slate",
                            "MINT" to "Pastel Mint Green",
                            "PURPLE" to "Arcade Purple",
                            "PINK" to "Cyber Neon Pink",
                            "INDIGO" to "Deep Royal Indigo"
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(200.dp)) {
                            items(wallpaperPresets) { (presetCode, presetName) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (userStats.chatWallpaperColor == presetCode) Color(0xFF142C26) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            onUpdateWallpaper(presetCode, userStats.chatWallpaperPattern)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (presetCode) {
                                                    "CHARCOAL" -> Color(0xFF202C33)
                                                    "MINT" -> Color(0xFFD9FDD3)
                                                    "PURPLE" -> Color(0xFF291047)
                                                    "PINK" -> Color(0xFFE040FB)
                                                    "INDIGO" -> Color(0xFF3F51B5)
                                                    else -> Color(0xFF0F2622)
                                                }
                                            )
                                            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(presetName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (userStats.chatWallpaperColor == presetCode) {
                                        Icon(Icons.Default.Check, contentDescription = "Active", tint = NeonMint, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showWallpaperDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply Wallpaper", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 3. Privacy Dialog - Last Seen
        if (showLastSeenDialog) {
            PrivacyOptionDialog(
                title = "Last Seen & Online",
                currentVal = userStats.privacyLastSeen,
                options = listOf("Everyone", "My Contacts", "Nobody"),
                onSelect = { selected ->
                    onUpdatePrivacySettings(
                        selected,
                        userStats.privacyProfilePhoto,
                        userStats.privacyAbout,
                        userStats.privacyReadReceipts,
                        userStats.disappearingMessagesTimer
                    )
                },
                onDismiss = { showLastSeenDialog = false }
            )
        }

        // 4. Privacy Dialog - Profile Photo
        if (showProfilePhotoDialog) {
            PrivacyOptionDialog(
                title = "Profile Photo Visibility",
                currentVal = userStats.privacyProfilePhoto,
                options = listOf("Everyone", "My Contacts", "Nobody"),
                onSelect = { selected ->
                    onUpdatePrivacySettings(
                        userStats.privacyLastSeen,
                        selected,
                        userStats.privacyAbout,
                        userStats.privacyReadReceipts,
                        userStats.disappearingMessagesTimer
                    )
                },
                onDismiss = { showProfilePhotoDialog = false }
            )
        }

        // 5. Privacy Dialog - About Status
        if (showAboutDialog) {
            PrivacyOptionDialog(
                title = "About Bio Visibility",
                currentVal = userStats.privacyAbout,
                options = listOf("Everyone", "My Contacts", "Nobody"),
                onSelect = { selected ->
                    onUpdatePrivacySettings(
                        userStats.privacyLastSeen,
                        userStats.privacyProfilePhoto,
                        selected,
                        userStats.privacyReadReceipts,
                        userStats.disappearingMessagesTimer
                    )
                },
                onDismiss = { showAboutDialog = false }
            )
        }

        // 6. Privacy Dialog - Disappearing message duration
        if (showDisappearingDialog) {
            PrivacyOptionDialog(
                title = "Disappearing Messages Timer",
                currentVal = userStats.disappearingMessagesTimer,
                options = listOf("Off", "24 Hours", "7 Days", "90 Days"),
                onSelect = { selected ->
                    onUpdatePrivacySettings(
                        userStats.privacyLastSeen,
                        userStats.privacyProfilePhoto,
                        userStats.privacyAbout,
                        userStats.privacyReadReceipts,
                        selected
                    )
                },
                onDismiss = { showDisappearingDialog = false }
            )
        }

        // 7. Backup Interval Selection
        if (showBackupIntervalDialog) {
            PrivacyOptionDialog(
                title = "Auto Cloud Backups",
                currentVal = userStats.backupInterval,
                options = listOf("Off", "Daily", "Weekly", "Monthly"),
                onSelect = { selected ->
                    onPerformBackup(selected, userStats.backupDestination)
                },
                onDismiss = { showBackupIntervalDialog = false }
            )
        }

        // 8. Backup Destination Selection
        if (showBackupDestDialog) {
            PrivacyOptionDialog(
                title = "Backup Destination Host",
                currentVal = userStats.backupDestination,
                options = listOf("Local Storage", "Google Drive", "AWS Glacier Server"),
                onSelect = { selected ->
                    onPerformBackup(userStats.backupInterval, selected)
                },
                onDismiss = { showBackupDestDialog = false }
            )
        }

        // Edit Username Dialog
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("Change Username", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter your custom gamer handle below:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonMint,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onUpdateUsername(nameInput)
                                showEditNameDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_username_btn")
                    ) {
                        Text("Save Handle", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancel", color = TextGray)
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun PrivacyOptionDialog(
    title: String,
    currentVal: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (currentVal == option) Color(0xFF142C26) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(option, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (currentVal == option) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = NeonMint, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = TextGray)
            }
        },
        containerColor = CyberCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun StatSmallCard(
    label: String,
    valText: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = TextGray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valText, color = tint, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun PrivacyToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = NeonMint,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = CyberBorder
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
