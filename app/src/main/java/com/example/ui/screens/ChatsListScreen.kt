package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    messagesList: List<ChatMessage>,
    onChatSelected: (chatId: String) -> Unit,
    onSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

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
                    else -> "Gaming Guild"
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

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "WeGist Chats",
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
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
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search friends & guilds...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonMint,
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
        }
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

                    Text(
                        text = previewText,
                        color = if (thread.hasUnreadChallenge) NeonMint else TextGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (thread.hasUnreadChallenge) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

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
