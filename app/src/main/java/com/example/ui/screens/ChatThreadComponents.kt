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
