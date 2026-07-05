package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrickBreakerGame
import com.example.ui.components.SpaceShooterGame
import com.example.ui.theme.*
import com.example.ui.viewmodel.StreamComment
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBroadcasterScreen(
    streamTitle: String,
    gameName: String,
    viewerCount: Int,
    comments: List<StreamComment>,
    onStopBroadcast: () -> Unit,
    onAwardCoins: (coins: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatListState = rememberLazyListState()
    var streamScore by remember { mutableStateOf(0) }
    var broadcastTimeSecs by remember { mutableStateOf(0) }

    // Scroll chat to bottom
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            chatListState.animateScrollToItem(comments.size - 1)
        }
    }

    // Elapsed timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            broadcastTimeSecs++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BROADCAST STUDIO", color = CyberPink, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(streamTitle, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(CyberPink, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${broadcastTimeSecs / 60}:${String.format("%02d", broadcastTimeSecs % 60)}",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
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
            // Live Status Header Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E0E18))
                    .border(0.5.dp, CyberPink.copy(alpha = 0.3f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📡 You are LIVE", color = CyberPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("👁️ $viewerCount spectators watching", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("🎮 Score: $streamScore", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            // Interactive Gaming Area where player actually plays the retro game!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (gameName == "Space Shooter") {
                    SpaceShooterGame(
                        onGameComplete = { finalScore ->
                            streamScore = finalScore
                            onAwardCoins(finalScore / 10) // 10% of game score awarded as real coins!
                        }
                    )
                } else {
                    BrickBreakerGame(
                        onGameComplete = { finalScore ->
                            streamScore = finalScore
                            onAwardCoins(finalScore / 10)
                        }
                    )
                }
            }

            // Audience Reaction feed and comment box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Audience Activity",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Comments feed
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(comments) { comment ->
                        SpectatorCommentRow(comment = comment)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stop Broadcast Action Button
                Button(
                    onClick = onStopBroadcast,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("stop_broadcast_btn")
                ) {
                    Icon(Icons.Default.SensorsOff, contentDescription = "Stop Live")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("END STREAM BROADCAST", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
