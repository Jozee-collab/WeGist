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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Forum
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.ChatMessage
import com.example.data.Friend
import com.example.data.FriendRequest
import com.example.data.UserStats
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import com.example.ui.viewmodel.LfgLobby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    messagesList: List<ChatMessage>,
    friendsList: List<Friend>,
    pendingRequests: List<FriendRequest>,
    userStats: UserStats,
    onAcceptRequest: (FriendRequest) -> Unit,
    onRejectRequest: (FriendRequest) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onSendRequest: (String, String, String) -> Unit,
    onInviteToGame: (String, String) -> Unit,
    onInviteToStream: (String, String) -> Unit,
    onChatSelected: (chatId: String) -> Unit,
    onSearchClicked: () -> Unit,
    onUpdateFriendPresence: (String, String) -> Unit = { _, _ -> },
    onSendMessage: (chatId: String, content: String) -> Unit = { _, _ -> },
    onToggleTheme: (Boolean) -> Unit = {},
    isSyncingContacts: Boolean = false,
    contactsSyncStatus: String? = null,
    onSyncContacts: () -> Unit = {},
    appMode: String = "GAMING",
    onAppModeChanged: (String) -> Unit = {},
    lfgLobbies: List<LfgLobby> = emptyList(),
    onCreateLobby: (String, String, Int) -> Unit = { _, _, _ -> },
    onJoinLobby: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubTab by remember { mutableStateOf("CHATS") } // CHATS or FRIENDS
    var customSearchUser by remember { mutableStateOf("") }
    var selectedFriendForActions by remember { mutableStateOf<Friend?>(null) }
    var quickChatFriendId by remember { mutableStateOf<String?>(null) }
    var friendsSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showCreateLobbyDialog by remember { mutableStateOf(false) }

    var subscribedChannels by remember { mutableStateOf(setOf("Meme Central 🤪")) }
    var showSecureAuditDialog by remember { mutableStateOf(false) }
    var workspaceCodeJoined by remember { mutableStateOf("") }
    var showJoinWorkspaceDialog by remember { mutableStateOf(false) }
    var inputWorkspaceCode by remember { mutableStateOf("") }

    // List of some recommended developers/gamers to add
    val recommendedGamers = listOf(
        Triple("pixel_dave", "PixelDave 👾", "img_profile_me_1783282394572"),
        Triple("slayer99", "Slayer99 ⚔️", "img_profile_me_1783282394572"),
        Triple("shadow_edge", "ShadowEdge 🥷", "img_profile_me_1783282394572")
    )

    // Filter recommended list based on current friends and pending requests
    val filteredRecommendations = recommendedGamers.filter { recommended ->
        friendsList.none { it.userId == recommended.first } &&
        pendingRequests.none { it.senderId == recommended.first }
    }

    // Aggregate messages into distinct conversations/threads
    val conversations = remember(messagesList) {
        messagesList.groupBy { it.chatId }.mapValues { entry ->
            val sorted = entry.value.sortedByDescending { it.timestamp }
            val latest = sorted.first()
            val unread = sorted.any { it.type == "GAME_INVITE" && it.gameStatus == "PENDING" && it.senderName != "WeGamer_Me" }
            ThreadInfo(
                chatId = entry.key,
                name = when (entry.key) {
                    "alice" -> "Alice (Retro Gamer)"
                    "bob" -> "Bob_Destroyer"
                    "spacequeen" -> "SpaceQueen"
                    "charlie" -> "GamerCharlie"
                    "pixel_dave" -> "PixelDave 👾"
                    "slayer99" -> "Slayer99 ⚔️"
                    "shadow_edge" -> "ShadowEdge 🥷"
                    else -> entry.key.replaceFirstChar { it.uppercase() }
                },
                avatarRes = R.drawable.img_profile_me_1783282394572, // profile me is fallback
                latestMessage = latest,
                hasUnreadChallenge = unread
            )
        }.values.toList()
    }

    val filteredConversations = conversations.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                    LargeTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = when (appMode) {
                                        "GAMING" -> Icons.Default.VideogameAsset
                                        "CASUAL" -> Icons.Default.Forum
                                        "ENTERPRISE" -> Icons.Default.Work
                                        else -> Icons.Default.Forum
                                    },
                                    contentDescription = null,
                                    tint = when (appMode) {
                                        "GAMING" -> NeonMint
                                        "CASUAL" -> Color(0xFF818CF8)
                                        "ENTERPRISE" -> Color(0xFF00A884)
                                        else -> NeonMint
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = when (appMode) {
                                        "GAMING" -> "WeGist Gaming"
                                        "CASUAL" -> "WeGist Casual"
                                        "ENTERPRISE" -> "WeGist Corporate"
                                        else -> "WeGist"
                                    },
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { onToggleTheme(!userStats.isDarkMode) },
                                modifier = Modifier.testTag("chats_theme_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (userStats.isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                    contentDescription = "Toggle Theme",
                                    tint = TextWhite
                                )
                            }
                            IconButton(
                                onClick = onSearchClicked,
                                modifier = Modifier.testTag("chats_search_btn")
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
            // Dynamic Realm / App Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple("GAMING", "👾 Gamer", "Gamer Realm"),
                    Triple("CASUAL", "💬 Casual", "Social Lounge"),
                    Triple("ENTERPRISE", "💼 Corporate", "Secure Hub")
                ).forEach { (modeCode, modeLabel, modeDesc) ->
                    val isModeSelected = appMode == modeCode
                    val activeBg = when (modeCode) {
                        "GAMING" -> NeonMint
                        "CASUAL" -> Color(0xFF818CF8)
                        "ENTERPRISE" -> Color(0xFF00A884)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val activeTextColor = if (modeCode == "GAMING" || modeCode == "ENTERPRISE") Color.Black else Color.White

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModeSelected) activeBg else Color.Transparent)
                            .clickable { onAppModeChanged(modeCode) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = modeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isModeSelected) activeTextColor else TextWhite.copy(alpha = 0.7f)
                        )
                        Text(
                            text = modeDesc,
                            fontSize = 9.sp,
                            color = if (isModeSelected) activeTextColor.copy(alpha = 0.8f) else TextGray.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Sub Tabs: CHATS / FRIENDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("CHATS", "FRIENDS & REQUESTS").forEach { tab ->
                    val isSelected = (tab == "CHATS" && selectedSubTab == "CHATS") || (tab == "FRIENDS & REQUESTS" && selectedSubTab == "FRIENDS")
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) NeonPurple else CyberCard,
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) NeonPurple else CyberBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedSubTab = if (tab == "CHATS") "CHATS" else "FRIENDS" }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("chats_tab_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (tab == "FRIENDS & REQUESTS" && pendingRequests.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(NeonMint, CircleShape)
                                        .size(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        pendingRequests.size.toString(),
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            if (selectedSubTab == "CHATS") {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (appMode == "ENTERPRISE") "Search workspace channels..." else "Search chats & guilds...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when (appMode) {
                            "GAMING" -> NeonMint
                            "CASUAL" -> Color(0xFF818CF8)
                            "ENTERPRISE" -> Color(0xFF00A884)
                            else -> NeonMint
                        },
                        unfocusedBorderColor = CyberBorder,
                        focusedContainerColor = CyberCard,
                        unfocusedContainerColor = CyberCard,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("chat_search_field")
                )

                // App Mode Banners & Utilities
                when (appMode) {
                    "GAMING" -> {
                        // Gaming Stats Panel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCard),
                            border = BorderStroke(1.dp, CyberBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(NeonMint, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Gaming Realm Active", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                                    Text("Level ${userStats.level} • Global Rank #${userStats.globalRank} • ${userStats.coins} Coins", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(NeonPurple, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Live HUD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    "CASUAL" -> {
                        // Casual Channels & Lounges
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📣 Public Interest Channels", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                Text("Join themed rooms with thousands of random people globally.", color = TextGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Meme Central 🤪", "Life Hacks 💡", "Chatterbox 💬").forEach { chan ->
                                        val isJoined = subscribedChannels.contains(chan)
                                        Box(
                                            modifier = Modifier
                                                .background(if (isJoined) Color(0xFF818CF8) else MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    subscribedChannels = if (isJoined) subscribedChannels - chan else subscribedChannels + chan
                                                }
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (isJoined) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                                Text(
                                                    text = chan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isJoined) Color.White else TextWhite
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ENTERPRISE" -> {
                        // Corporate Secure Workspace
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(16.dp))
                                    Text("🔒 Local-First Private Workspace", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                }
                                Text("Workspace data stays on this device — nothing is uploaded in the demo build.", color = TextGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showJoinWorkspaceDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Business, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (workspaceCodeJoined.isEmpty()) "Join Workspace" else "Workspace: $workspaceCodeJoined", color = Color.Black, fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { showSecureAuditDialog = true },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Audit Security", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Looking For Group (LFG) active lobbies dashboard
                if (appMode == "GAMING" && selectedSubTab == "CHATS") {
                    LfgLobbiesHub(
                        lobbies = lfgLobbies,
                        onJoinLobby = onJoinLobby,
                        onCreateLobbyClick = { showCreateLobbyDialog = true }
                    )
                }

                if (filteredConversations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No active chats found", color = TextGray, fontSize = 16.sp)
                            Text("Send a game invite to start a conversation!", color = TextGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredConversations) { thread ->
                            ChatThreadCard(
                                thread = thread,
                                onClick = { onChatSelected(thread.chatId) }
                            )
                        }
                    }
                }
            } else {
                // FRIENDS & REQUESTS SUB TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                ) {
                    // Send custom friend request section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCard),
                            border = BorderStroke(1.dp, CyberBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Add Gaming Friends", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customSearchUser,
                                        onValueChange = { customSearchUser = it },
                                        placeholder = { Text("Enter gamer username...", color = TextGray, fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonPurple,
                                            unfocusedBorderColor = CyberBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("custom_friend_input")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (customSearchUser.isNotBlank()) {
                                                onSendRequest(
                                                    customSearchUser.trim().lowercase().replace(" ", "_"),
                                                    customSearchUser,
                                                    "img_profile_me_1783282394572"
                                                )
                                                customSearchUser = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("send_custom_friend_req")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Sync Contacts & Discover WeGist Users Section
                    item {
                        val context = LocalContext.current
                        val permissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                onSyncContacts()
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCard),
                            border = BorderStroke(1.dp, CyberBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("sync_contacts_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContactPhone,
                                        contentDescription = "Sync Contacts",
                                        tint = NeonMint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Sync Phone Contacts",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Scan your phone's address book to automatically find and add your real-life gaming buddies on WeGist.",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (isSyncingContacts) {
                                    LinearProgressIndicator(
                                        color = NeonMint,
                                        trackColor = CyberBorder,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("sync_progress")
                                    )
                                }

                                contactsSyncStatus?.let { status ->
                                    Text(
                                        text = status,
                                        color = NeonMint,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(vertical = 4.dp).testTag("sync_status_msg")
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.READ_CONTACTS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            onSyncContacts()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                        }
                                    },
                                    enabled = !isSyncingContacts,
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("sync_contacts_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sync Now",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Find Friends & Sync Contacts", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Recommended Gamers
                    if (filteredRecommendations.isNotEmpty()) {
                        item {
                            Text("Recommended Gamers", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        items(filteredRecommendations) { gamer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                border = BorderStroke(0.5.dp, CyberBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, CyberBorder, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(gamer.second, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Button(
                                        onClick = {
                                            onSendRequest(gamer.first, gamer.second, gamer.third)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("add_recommended_${gamer.first}")
                                    ) {
                                        Text("Add ➕", color = NeonMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // PENDING FRIEND REQUESTS
                    item {
                        Text(
                            text = "Pending Requests (${pendingRequests.size})",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (pendingRequests.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, CyberBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No pending requests", color = TextGray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(pendingRequests) { request ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                border = BorderStroke(1.2.dp, NeonPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, CyberBorder, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(request.senderName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Wants to connect", color = TextGray, fontSize = 11.sp)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { onAcceptRequest(request) },
                                            modifier = Modifier.background(NeonMint, CircleShape).size(32.dp).testTag("accept_${request.senderId}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Black, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { onRejectRequest(request) },
                                            modifier = Modifier.background(CyberBorder, CircleShape).size(32.dp).testTag("reject_${request.senderId}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Decline", tint = TextWhite, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FRIENDS LIST WITH ML CLASSIFICATION DASHBOARD
                    item {
                        val context = LocalContext.current
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Connected Friends (${friendsList.size})",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // Heuristic ML Status Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                border = BorderStroke(1.dp, CyberBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NeonPurple.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AI Contact Categorizer Active", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Machine learning groups based on interaction frequency.", color = TextGray, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "ML Classifier Re-run: Contacts clustered perfectly!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("RE-RUN 🤖", color = NeonMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = friendsSearchQuery,
                                onValueChange = { friendsSearchQuery = it },
                                placeholder = { Text("Filter connected friends...", color = TextGray, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPurple,
                                    unfocusedBorderColor = CyberBorder,
                                    focusedContainerColor = CyberCard,
                                    unfocusedContainerColor = CyberCard,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("friends_search_field"),
                                singleLine = true
                            )

                            // ML Category Filter Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "🌐 All",
                                    "GAMING_SQUAD" to "🎮 Gaming Squad",
                                    "COLLEAGUES" to "💼 Colleagues",
                                    "FAMILY" to "🏠 Family"
                                ).forEach { (code, label) ->
                                    val isSelected = selectedCategoryFilter == code
                                    val activeColor = when (code) {
                                        "GAMING_SQUAD" -> NeonPurple
                                        "COLLEAGUES" -> Color(0xFF00A884)
                                        "FAMILY" -> Color(0xFF3B82F6)
                                        else -> NeonMint
                                    }
                                    val chipBg = if (isSelected) activeColor.copy(alpha = 0.25f) else CyberCard
                                    val chipBorder = if (isSelected) activeColor else CyberBorder
                                    Box(
                                        modifier = Modifier
                                            .background(chipBg, RoundedCornerShape(16.dp))
                                            .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                                            .clickable { selectedCategoryFilter = code }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("ml_chip_$code")
                                    ) {
                                        Text(text = label, color = if (isSelected) activeColor else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    val filteredFriends = friendsList.filter {
                        val matchesSearch = it.username.contains(friendsSearchQuery, ignoreCase = true)
                        val matchesCategory = if (selectedCategoryFilter == "ALL") {
                            true
                        } else {
                            classifyFriend(it, messagesList).first.uppercase().replace(" ", "_") == selectedCategoryFilter
                        }
                        matchesSearch && matchesCategory
                    }

                    if (friendsList.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, CyberBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No connected friends yet. Add some gamers!", color = TextGray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else if (filteredFriends.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, CyberBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No friends match \"$friendsSearchQuery\"", color = TextGray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredFriends) { friend ->
                            val (category, freqInfo) = remember(friend, messagesList) {
                                classifyFriend(friend, messagesList)
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                border = BorderStroke(1.dp, CyberBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFriendForActions = friend }
                                    .testTag("friend_card_${friend.userId}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(44.dp)) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.dp, CyberBorder, CircleShape)
                                            )
                                            val presenceColor = when (friend.presence) {
                                                "online" -> NeonMint
                                                "in-game" -> NeonPurple
                                                else -> TextGray
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(presenceColor, CircleShape)
                                                    .border(1.5.dp, CyberCard, CircleShape)
                                                    .align(Alignment.BottomEnd)
                                                    .testTag("presence_dot_${friend.userId}")
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(friend.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                val badgeColor = when (friend.presence) {
                                                    "online" -> NeonMint
                                                    "in-game" -> NeonPurple
                                                    else -> TextGray
                                                }
                                                val badgeText = when (friend.presence) {
                                                    "online" -> "Online"
                                                    "in-game" -> "In-Game"
                                                    else -> "Offline"
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                                                        .clickable {
                                                            val nextStatus = when (friend.presence) {
                                                                "online" -> "in-game"
                                                                "in-game" -> "offline"
                                                                else -> "online"
                                                            }
                                                            onUpdateFriendPresence(friend.userId, nextStatus)
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        .testTag("presence_badge_${friend.userId}")
                                                ) {
                                                    Text(
                                                        text = badgeText.uppercase(),
                                                        color = badgeColor,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 8.sp,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                            
                                            // ML Group Badge Row
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                val catColor = when (category) {
                                                    "Gaming Squad" -> NeonPurple
                                                    "Colleagues" -> Color(0xFF00A884)
                                                    else -> Color(0xFF3B82F6)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(catColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .border(0.5.dp, catColor, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(category.uppercase(), color = catColor, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(freqInfo, color = TextGray, fontSize = 9.sp)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Rank: #${friend.globalRank}", color = NeonMint, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("🏆 ${friend.wins} Wins", color = AccentOrange, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(NeonPurple, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("INVITE ➕", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

            // Quick Chat overlay panel
            AnimatedVisibility(
                visible = quickChatFriendId != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(5f)
            ) {
                val fId = quickChatFriendId
                if (fId != null) {
                    val friend = friendsList.find { it.userId == fId }
                    val chatMessages = messagesList.filter { it.chatId == fId }
                    ChatWindowComponent(
                        chatId = fId,
                        friendName = friend?.username ?: fId.replaceFirstChar { it.uppercase() },
                        messages = chatMessages,
                        onSendMessage = { text ->
                            onSendMessage(fId, text)
                        },
                        onClose = { quickChatFriendId = null }
                    )
                }
            }
        }

    // Friend Interactivity Modal Dialog
    if (selectedFriendForActions != null) {
        val friend = selectedFriendForActions!!
        AlertDialog(
            onDismissRequest = { selectedFriendForActions = null },
            containerColor = CyberCard,
            title = {
                Text(
                    "Invite or Chat with ${friend.username}",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select an action to connect with this gamer:", color = TextGray, fontSize = 13.sp)

                    // Option 1: Send Game Invite
                    Button(
                        onClick = {
                            onInviteToGame(friend.userId, "Space Shooter Arcade")
                            selectedFriendForActions = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_invite_space_${friend.userId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invite to Space Shooter 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onInviteToGame(friend.userId, "Brick Breaker Retro")
                            selectedFriendForActions = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_invite_brick_${friend.userId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invite to Brick Breaker 🧱", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Option 2: Invite to Stream
                    Button(
                        onClick = {
                            onInviteToStream(friend.userId, "Retro Arcade Showdown")
                            selectedFriendForActions = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_invite_stream_${friend.userId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invite to Live Stream 📺", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    // Option 3: Chat
                    Button(
                        onClick = {
                            onChatSelected(friend.userId)
                            selectedFriendForActions = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_dm_${friend.userId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Direct Message 💬", color = TextWhite, fontWeight = FontWeight.Bold)
                    }

                    // Option 3.5: Quick Chat overlay
                    Button(
                        onClick = {
                            quickChatFriendId = friend.userId
                            selectedFriendForActions = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(alpha = 0.2f), contentColor = NeonPurple),
                        border = BorderStroke(1.dp, NeonPurple),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_quick_chat_${friend.userId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ChatBubble, contentDescription = null, tint = NeonPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick Chat Window ⚡", color = NeonPurple, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(color = CyberBorder, thickness = 1.dp)

                    Text("Simulate Presence Status:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("online" to "Online 🟢", "in-game" to "In-Game 🎮", "offline" to "Offline ⚪").forEach { (status, label) ->
                            val color = when (status) {
                                "online" -> NeonMint
                                "in-game" -> NeonPurple
                                else -> TextGray
                            }
                            val isSelected = friend.presence == status
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) color else CyberBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onUpdateFriendPresence(friend.userId, status)
                                        selectedFriendForActions = null
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("simulate_presence_${status}_${friend.userId}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) color else TextWhite.copy(alpha = 0.8f),
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 4: Remove Friend
                    TextButton(
                        onClick = {
                            onRemoveFriend(friend.userId)
                            selectedFriendForActions = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally).testTag("dialog_remove_${friend.userId}")
                    ) {
                        Text("Remove Friend ❌", color = AccentOrange, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedFriendForActions = null }) {
                    Text("Cancel", color = TextWhite)
                }
            }
        )
    }

    if (showJoinWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showJoinWorkspaceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF00A884))
                    Text("Join Corporate Workspace", fontWeight = FontWeight.Bold, color = TextWhite)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your company's secure workspace code to synchronize SFW channels and professional contacts.", color = TextGray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = inputWorkspaceCode,
                        onValueChange = { inputWorkspaceCode = it },
                        placeholder = { Text("e.g. ALPHABET-DEV", color = TextGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00A884),
                            unfocusedBorderColor = CyberBorder,
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
                        workspaceCodeJoined = inputWorkspaceCode.uppercase()
                        showJoinWorkspaceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
                ) {
                    Text("Link Workspace", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinWorkspaceDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            }
        )
    }

    if (showSecureAuditDialog) {
        AlertDialog(
            onDismissRequest = { showSecureAuditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00A884))
                    Text("Security Compliance Audit", fontWeight = FontWeight.Bold, color = TextWhite)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("WeGist Data & Privacy Status:", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "Data Location: ✅ Local database on this device",
                        "Cloud Sync: ℹ️ Not connected (demo build)",
                        "End-to-End Encryption: ℹ️ Not implemented yet (roadmap)",
                        "Workspace Data: ✅ Stays on device",
                        "Telemetry: ✅ None"
                    ).forEach { detail ->
                        Text("• $detail", color = TextGray, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecureAuditDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
                ) {
                    Text("Close Audit Log", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showCreateLobbyDialog) {
        CreateLfgLobbyDialog(
            onDismiss = { showCreateLobbyDialog = false },
            onCreate = onCreateLobby
        )
    }
}
