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
    modifier: Modifier = Modifier
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(userStats.username) }
    var dailyClaimed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                            Image(
                                painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                contentDescription = "My Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, NeonMint, CircleShape)
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

                        Text(
                            text = "@${userStats.username}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextWhite
                        )
                        Text(
                            text = "Level 8 Gamer Guild Leader",
                            fontSize = 12.sp,
                            color = NeonMint,
                            fontWeight = FontWeight.SemiBold
                        )
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

                        // Gmail Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Mail, contentDescription = "Gmail", tint = CyberPink, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Google Account", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (userStats.authGmail.isEmpty()) "Not connected" else "Linked: ${userStats.authGmail}",
                                        color = if (userStats.authGmail.isEmpty()) TextGray else NeonMint,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showLinkDialogType = "GMAIL" },
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

                        HorizontalDivider(color = CyberBorder)

                        // X (Twitter) Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Language, contentDescription = "X Twitter", tint = AccentOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("X Profile (Twitter)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (userStats.authX.isEmpty()) "Not connected" else "Linked: @${userStats.authX}",
                                        color = if (userStats.authX.isEmpty()) TextGray else NeonMint,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showLinkDialogType = "X" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userStats.authX.isEmpty()) CyberBorder else Color(0xFF0F2D1E),
                                    contentColor = if (userStats.authX.isEmpty()) TextWhite else NeonMint
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp).testTag("link_x_btn")
                            ) {
                                Text(if (userStats.authX.isEmpty()) "Connect" else "Modify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        }

        // --- OVERLAY DIALOGS ---

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
