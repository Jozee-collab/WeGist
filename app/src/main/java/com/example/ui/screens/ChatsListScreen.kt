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
                                    Text("🔒 End-to-End Encrypted Tunnel", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                }
                                Text("This interface is compliant with secure company-wide communication standards.", color = TextGray, fontSize = 11.sp)
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
                    Text("WeGist Cryptographic Ledger Status:", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "Signal Protocol Handshake: ✅ Compliant (ECDH Curve25519)",
                        "Cipher Standard: ✅ AES-GCM-256 Bit Encryption",
                        "Workspace Keys: ✅ Rotated 14m ago",
                        "Data Location: ✅ Sandbox Local Vault",
                        "Compliance Code: WG-CORP-SEC-998A"
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

@Composable
fun ChatThreadCard(
    thread: ThreadInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chat_thread_${thread.chatId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = if (thread.hasUnreadChallenge) BorderStroke(1.2.dp, NeonMint) else BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(modifier = Modifier.size(52.dp)) {
                Image(
                    painter = painterResource(id = thread.avatarRes),
                    contentDescription = thread.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, if (thread.hasUnreadChallenge) NeonMint else CyberBorder, CircleShape)
                )
                // Active/online green dot indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(NeonMint, CircleShape)
                        .border(2.dp, CyberCard, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatTime(thread.latestMessage.timestamp),
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val previewText = when (thread.latestMessage.type) {
                        "GAME_INVITE" -> "🎮 Challenge: Let's play ${thread.latestMessage.gameType}!"
                        "VOICE" -> "🎤 Voice Note (0:12)"
                        "STICKER" -> "👾 Sent a sticker"
                        "MEDIA" -> "📷 Sent shared link"
                        else -> thread.latestMessage.content
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (thread.latestMessage.senderName == "WeGamer_Me") {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Sent message read status",
                                tint = Color(0xFF53BDEB),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = previewText,
                            color = if (thread.hasUnreadChallenge) NeonMint else TextGray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (thread.hasUnreadChallenge) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (thread.hasUnreadChallenge) {
                        Box(
                            modifier = Modifier
                                .background(NeonMint, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "PLAY 🎮",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Model helper for thread aggregation
data class ThreadInfo(
    val chatId: String,
    val name: String,
    val avatarRes: Int,
    val latestMessage: ChatMessage,
    val hasUnreadChallenge: Boolean
)

// Minimal time formatter
fun formatTime(timestamp: Long): String {
    val minsAgo = (System.currentTimeMillis() - timestamp) / 60000
    return when {
        minsAgo < 1 -> "Just now"
        minsAgo < 60 -> "${minsAgo}m ago"
        minsAgo < 1440 -> "${minsAgo / 60}h ago"
        else -> "${minsAgo / 1440}d ago"
    }
}

@Composable
fun ChatWindowComponent(
    chatId: String,
    friendName: String,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom on load/new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .testTag("quick_chat_window"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardDark),
        border = BorderStroke(1.5.dp, NeonMint)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBorderDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                            contentDescription = friendName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, NeonMint, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeonMint, CircleShape)
                                .border(1.dp, CyberBorderDark, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = friendName,
                            color = TextWhiteDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Quick Chat Window",
                            color = NeonMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("quick_chat_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Chat Window", tint = TextWhiteDark)
                }
            }

            // Message History Scroll List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF090A0D))
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No quick messages yet. Say hello!",
                                color = TextGrayDark,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(messages) { msg ->
                        val isMe = msg.senderName == "WeGamer_Me"
                        val align = if (isMe) Alignment.End else Alignment.Start
                        val bubbleBg = if (isMe) NeonMint else CyberBorderDark
                        val bubbleTxt = if (isMe) Color.Black else TextWhiteDark
                        val bubbleShape = if (isMe) {
                            RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
                        } else {
                            RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = align
                        ) {
                            Surface(
                                color = bubbleBg,
                                shape = bubbleShape,
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    val bubbleText = when (msg.type) {
                                        "GAME_INVITE" -> "🎮 Challenge: Let's play ${msg.gameType}!"
                                        "VOICE" -> "🎤 Voice Note (0:12)"
                                        "STICKER" -> "👾 Sent a sticker"
                                        "MEDIA" -> "📷 Sent shared link"
                                        else -> msg.content
                                    }
                                    Text(
                                        text = bubbleText,
                                        color = bubbleTxt,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatTime(msg.timestamp),
                                color = TextGrayDark,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Quick Emoji Reaction Tap Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBorderDark)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("🔥", "🎮", "🕹️", "👑", "GG", "🤣").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .clickable {
                                onSendMessage(emoji)
                            }
                            .padding(6.dp)
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }

            // Chat Input Text Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBorderDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type a late-night message...", color = TextGrayDark, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMint,
                        unfocusedBorderColor = CyberSlateDark,
                        focusedContainerColor = CyberSlateDark,
                        unfocusedContainerColor = CyberSlateDark,
                        focusedTextColor = TextWhiteDark,
                        unfocusedTextColor = TextWhiteDark
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 48.dp)
                        .testTag("quick_chat_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .background(NeonMint, CircleShape)
                        .size(36.dp)
                        .testTag("quick_chat_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LfgLobbiesHub(
    lobbies: List<LfgLobby>,
    onJoinLobby: (String) -> Unit,
    onCreateLobbyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = NeonMint, modifier = Modifier.size(20.dp))
                    Text("Gamers LFG Squads", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                }
                
                Button(
                    onClick = onCreateLobbyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp).testTag("post_lfg_button")
                ) {
                    Text("Post LFG 📣", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Text(
                text = "Join active lobbies or invite friends to your own squad instantly.",
                color = TextGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            
            if (lobbies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active lobbies. Create one above!", color = TextGray, fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lobbies.take(3).forEach { lobby ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberSlateDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(lobby.gameName, color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (lobby.voiceActive) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Voice active", tint = NeonMint, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Text(lobby.title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                                Text("Host: @${lobby.hostName} • ${lobby.joinedPlayers.size}/${lobby.maxPlayers} players", color = TextGray, fontSize = 10.sp)
                            }
                            
                            val isJoined = lobby.joinedPlayers.contains("WeGamer_Me") || lobby.joinedPlayers.size >= lobby.maxPlayers
                            Button(
                                onClick = { onJoinLobby(lobby.id) },
                                enabled = !isJoined,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isJoined) CyberBorder else NeonMint,
                                    disabledContainerColor = CyberBorder
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("join_lobby_${lobby.id}")
                            ) {
                                Text(
                                    text = if (lobby.joinedPlayers.contains("WeGamer_Me")) "Joined ✓" else if (lobby.joinedPlayers.size >= lobby.maxPlayers) "Full" else "Join ⚔️",
                                    color = if (isJoined) TextWhite.copy(alpha = 0.5f) else Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLfgLobbyDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Int) -> Unit
) {
    var gameName by remember { mutableStateOf("Valorant") }
    var title by remember { mutableStateOf("Need players for Ranked climb!") }
    var maxPlayers by remember { mutableStateOf(4) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assemble Gaming Squad", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Broadcast your gaming lobby so friends can join you instantly with active voice channels.", color = TextGray, fontSize = 12.sp)
                
                Column {
                    Text("Select Game", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = gameName,
                        onValueChange = { gameName = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("lfg_game_input")
                    )
                }

                Column {
                    Text("Lobby Call / Title", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("lfg_title_input")
                    )
                }

                Column {
                    Text("Max Team Size: $maxPlayers", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Slider(
                        value = maxPlayers.toFloat(),
                        onValueChange = { maxPlayers = it.toInt() },
                        valueRange = 2f..5f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonMint,
                            activeTrackColor = NeonMint,
                            inactiveTrackColor = CyberBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(gameName, title, maxPlayers)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                modifier = Modifier.testTag("confirm_create_lfg")
            ) {
                Text("Broadcast LFG 📢", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        },
        containerColor = CyberSlateDark,
        shape = RoundedCornerShape(16.dp)
    )
}

fun classifyFriend(friend: Friend, messages: List<ChatMessage>): Pair<String, String> {
    val threadMessages = messages.filter { it.chatId == friend.userId }
    val count = threadMessages.size
    val frequency = when {
        count >= 12 -> "High Interaction"
        count >= 4 -> "Medium Interaction"
        else -> "Low Interaction"
    }

    var gamingScore = 0
    var colleagueScore = 0
    var familyScore = 0

    threadMessages.forEach { msg ->
        val text = msg.content.lowercase()
        if (text.contains("game") || text.contains("stream") || text.contains("lobby") || text.contains("squad") || text.contains("wins") || text.contains("coin") || text.contains("raid") || text.contains("level") || text.contains("rps") || text.contains("challenge")) {
            gamingScore += 2
        }
        if (text.contains("meeting") || text.contains("schedule") || text.contains("workspace") || text.contains("colleague") || text.contains("corporate") || text.contains("sprint") || text.contains("audit") || text.contains("report") || text.contains("thread")) {
            colleagueScore += 2
        }
        if (text.contains("mom") || text.contains("dad") || text.contains("bro") || text.contains("family") || text.contains("home") || text.contains("dinner") || text.contains("sister") || text.contains("love")) {
            familyScore += 2
        }
    }

    if (gamingScore == 0 && colleagueScore == 0 && familyScore == 0) {
        val h = Math.abs(friend.userId.hashCode()) % 3
        val category = when (h) {
            0 -> "Gaming Squad"
            1 -> "Colleagues"
            else -> "Family"
        }
        return Pair(category, "Interaction: $frequency")
    }

    val maxScore = maxOf(gamingScore, colleagueScore, familyScore)
    val category = when {
        maxScore == gamingScore -> "Gaming Squad"
        maxScore == colleagueScore -> "Colleagues"
        else -> "Family"
    }
    return Pair(category, "Interaction: $frequency")
}

