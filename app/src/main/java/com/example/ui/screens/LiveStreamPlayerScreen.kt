package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiveStream
import com.example.ui.theme.*
import com.example.ui.viewmodel.FloatingEmoji
import com.example.ui.viewmodel.StreamComment
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamPlayerScreen(
    stream: LiveStream,
    comments: List<StreamComment>,
    floatingEmojis: List<FloatingEmoji>,
    userCoins: Int,
    onSendComment: (content: String) -> Unit,
    onTriggerReaction: (emoji: String) -> Unit,
    onSendGift: (cost: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chatInput by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()
    var showGiftSheet by remember { mutableStateOf(false) }

    // Scroll chat to bottom
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            chatListState.animateScrollToItem(comments.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stream.streamTitle, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("Streaming ${stream.gameName} with ${stream.viewerCount} viewers", color = TextGray, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("stream_player_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    // Floating red indicators
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(CyberPink, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = "Live", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberCard)
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
            // Simulated video player canvas (16:9 aspect ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                // Autonomous animated pixel game drawing on a canvas
                SimulatedGameplayCanvas(gameName = stream.gameName)

                // Overlay for floating reaction emojis
                FloatingEmojisOverlay(floatingEmojis = floatingEmojis)

                // Bottom gradient label overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                // Small overlay displaying streamer handle
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(NeonMint, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("@${stream.streamerName}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Stream controls & Quick reactions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard)
                    .border(0.5.dp, CyberBorder)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Heart, thumbs, fire quick reactions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("❤️", "🔥", "🎮", "👏", "🤣").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .background(CyberBorder, CircleShape)
                                .clickable { onTriggerReaction(emoji) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("stream_quick_reaction_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 15.sp)
                        }
                    }
                }

                // Virtual gift launcher button
                Button(
                    onClick = { showGiftSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("gift_shop_btn")
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Gift", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Support", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Chat Title
            Text(
                "Spectator Live Chat",
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )

            // Chat Message List
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(comments) { comment ->
                    SpectatorCommentRow(comment = comment)
                }
            }

            // Chat Input Panel
            Surface(
                color = CyberCard,
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Say something in chat...", color = TextGray, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = Color(0xFF0F0F13),
                            unfocusedContainerColor = Color(0xFF0F0F13)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stream_chat_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                onSendComment(chatInput)
                                chatInput = ""
                            }
                        },
                        enabled = chatInput.isNotBlank(),
                        modifier = Modifier
                            .background(if (chatInput.isNotBlank()) NeonMint else CyberBorder, CircleShape)
                            .size(36.dp)
                            .testTag("stream_chat_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Virtual Gift selection bottom sheet panel
        if (showGiftSheet) {
            AlertDialog(
                onDismissRequest = { showGiftSheet = false },
                title = { Text("Send Streaming Support Gift", color = TextWhite, fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Your Balance:", color = TextGray)
                            Text("🪙 $userCoins coins", color = AccentOrange, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                Triple("Star ⭐", 50, Color.Yellow),
                                Triple("Shield 🛡️", 100, NeonMint),
                                Triple("Crown 👑", 200, NeonPurple)
                            ).forEach { (gift, cost, tint) ->
                                val affordable = userCoins >= cost
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = affordable) {
                                            onSendGift(cost)
                                            showGiftSheet = false
                                        }
                                        .testTag("gift_$cost"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (affordable) CyberBorder else Color.Black.copy(alpha = 0.2f)
                                    ),
                                    border = BorderStroke(1.dp, if (affordable) tint.copy(alpha = 0.5f) else Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(gift, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("${cost} 🪙", color = if (affordable) AccentOrange else TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGiftSheet = false }) {
                        Text("Close Shop", color = TextWhite)
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// Automated game simulator
@Composable
fun SimulatedGameplayCanvas(gameName: String) {
    var tick by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30)
            tick += 2f
            if (tick > 1000f) tick = 0f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background scrolling stars
        val starY1 = (tick * 0.5f) % height
        val starY2 = ((tick * 0.5f) + height/2) % height
        drawCircle(Color.White.copy(alpha = 0.4f), 3f, Offset(width * 0.15f, starY1))
        drawCircle(Color.White.copy(alpha = 0.6f), 4f, Offset(width * 0.75f, starY2))
        drawCircle(Color.White.copy(alpha = 0.3f), 3f, Offset(width * 0.45f, (starY1 + 100f) % height))
        drawCircle(Color.White.copy(alpha = 0.5f), 4f, Offset(width * 0.90f, (starY2 + 200f) % height))

        if (gameName == "Space Shooter") {
            // Space Shooter Automated Drawing
            // Player Spaceship at bottom middle, sliding side to side gently
            val shipX = width / 2f + Math.sin(tick * 0.02).toFloat() * (width / 3f)
            val shipY = height - 40f

            // Spaceship triangle
            drawCircle(NeonMint, 12f, Offset(shipX, shipY))
            // Lasers rising up
            val laserY = (height - 40f - (tick * 3f) % height)
            drawCircle(CyberPink, 6f, Offset(shipX, laserY))
            drawCircle(CyberPink, 6f, Offset(shipX, (laserY + height/2f) % height))

            // Alien squares falling down
            for (i in 0..2) {
                val alienX = width * (0.25f + i * 0.25f)
                val alienY = ((tick * 1.5f) + i * 150f) % (height - 80f)
                // Draw enemy square
                if (Math.abs(alienY - laserY) > 30f) { // mock hit flash deletion
                    drawRect(
                        color = NeonPurple,
                        topLeft = Offset(alienX - 15f, alienY - 15f),
                        size = androidx.compose.ui.geometry.Size(30f, 30f)
                    )
                } else {
                    // Exploding spark circle
                    drawCircle(Color.Yellow, 24f, Offset(alienX, alienY), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
                }
            }

        } else {
            // Brick Breaker Automated Drawing
            // Paddle sliding
            val padX = width / 2f + Math.cos(tick * 0.01).toFloat() * (width / 4f)
            val padY = height - 25f
            drawRoundRect(
                color = NeonMint,
                topLeft = Offset(padX - 40f, padY),
                size = androidx.compose.ui.geometry.Size(80f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Ball bouncing around in a preset loop
            val ballX = width/2f + Math.sin(tick * 0.015).toFloat() * (width/3f)
            val ballY = height/2f + Math.cos(tick * 0.03).toFloat() * (height/3f)
            drawCircle(Color.White, 8f, Offset(ballX, ballY))

            // Draw some colored bricks at top
            for (row in 0..1) {
                for (col in 0..7) {
                    val bx = col * (width / 8f) + 4f
                    val by = row * 20f + 20f
                    // only draw bricks ball hasn't hit
                    drawRect(
                        color = if (row == 0) CyberPink else NeonPurple,
                        topLeft = Offset(bx, by),
                        size = androidx.compose.ui.geometry.Size((width/8f) - 8f, 14f)
                    )
                }
            }
        }
    }
}

// Spectator comment rows
@Composable
fun SpectatorCommentRow(comment: StreamComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (comment.badge.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(CyberBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(comment.badge, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${comment.senderName}",
                    fontWeight = FontWeight.Bold,
                    color = if (comment.senderName == "WeGamer_Me") NeonMint else TextGray,
                    fontSize = 13.sp
                )
            }
            Text(text = comment.content, color = TextWhite, fontSize = 13.sp)
        }
    }
}

// Floating reaction emojis overlays
@Composable
fun FloatingEmojisOverlay(floatingEmojis: List<FloatingEmoji>) {
    Box(modifier = Modifier.fillMaxSize()) {
        floatingEmojis.forEach { fe ->
            key(fe.id) {
                AnimatedFloatingEmoji(fe = fe)
            }
        }
    }
}

@Composable
fun AnimatedFloatingEmoji(fe: FloatingEmoji) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Simple animation loop pushing offset up
        while (progress < 1.0f) {
            delay(20)
            progress += 0.015f
        }
    }

    val yOffset = 300.dp * (1.0f - progress)
    val alpha = 1.0f - progress
    val scale = 0.8f + (progress * 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = fe.emoji,
            fontSize = 24.sp,
            modifier = Modifier
                .offset(x = fe.xOffset.dp, y = -yOffset)
                .alpha(alpha)
                .animateContentSize()
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.2f * alpha))
                .padding(4.dp)
        )
    }
}
