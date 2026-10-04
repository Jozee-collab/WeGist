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
