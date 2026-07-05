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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LiveStream
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamsScreen(
    streamsList: List<LiveStream>,
    onSelectStream: (streamId: String) -> Unit,
    onStartBroadcast: (title: String, game: String) -> Unit,
    onSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastTitleInput by remember { mutableStateOf("Arcade Highscore Run! 👾") }
    var selectedGame by remember { mutableStateOf("Space Shooter") }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "WeGist Live Streams",
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSearchClicked,
                        modifier = Modifier.testTag("streams_search_btn")
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBroadcastDialog = true },
                containerColor = CyberPink,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Sensors, contentDescription = "Go Live") },
                text = { Text("Go Live", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .padding(bottom = 70.dp) // Offset bottom nav bar overlap
                    .testTag("go_live_fab")
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
            // Featured Hero banner
            FeaturedStreamBanner(
                onClick = {
                    val first = streamsList.firstOrNull()
                    if (first != null) onSelectStream(first.id)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Ongoing Games",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = TextWhite,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            if (streamsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active streams right now.", color = TextGray)
                        Text("Be the first to start a stream!", color = TextGray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(streamsList) { stream ->
                        LiveStreamCard(
                            stream = stream,
                            onClick = { onSelectStream(stream.id) }
                        )
                    }
                }
            }
        }

        // Broadcaster setup popup dialog
        if (showBroadcastDialog) {
            AlertDialog(
                onDismissRequest = { showBroadcastDialog = false },
                title = { Text("Go Live Broadcasting", color = TextWhite, fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Share your gameplay screen with your chats and global watchers!", color = TextGray, fontSize = 12.sp)

                        OutlinedTextField(
                            value = broadcastTitleInput,
                            onValueChange = { broadcastTitleInput = it },
                            label = { Text("Stream Title", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonMint,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("broadcast_title_input")
                        )

                        Text("Select Game to Stream:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Space Shooter", "Brick Breaker").forEach { game ->
                                val active = selectedGame == game
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedGame = game }
                                        .testTag("broadcast_game_$game"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) Color(0xFF1B3D35) else CyberBorder
                                    ),
                                    border = BorderStroke(1.2.dp, if (active) NeonMint else Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VideogameAsset,
                                            contentDescription = game,
                                            tint = if (active) NeonMint else TextGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(game, color = if (active) TextWhite else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onStartBroadcast(broadcastTitleInput, selectedGame)
                            showBroadcastDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("broadcast_confirm_btn")
                    ) {
                        Text("GO LIVE 📡", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBroadcastDialog = false }) {
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
fun FeaturedStreamBanner(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(NeonPurple, CyberPink)
                )
            )
            .clickable(onClick = onClick)
    ) {
        // Decorative space game elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("🔴 FEATURED ARENA", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 9.sp)
            }

            Column {
                Text(
                    "Join Live Watchparties",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    "Interact with real-time streamers and send rewards!",
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LiveStreamCard(
    stream: LiveStream,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stream_card_${stream.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column {
            // Stream Thumbnail (16:9 Aspect ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val thumbRes = when (stream.thumbnailResName) {
                    "img_retro_stream_thumbnail_1783282368689" -> R.drawable.img_retro_stream_thumbnail_1783282368689
                    else -> R.drawable.img_puzzle_stream_thumbnail_1783282381092
                }
                Image(
                    painter = painterResource(id = thumbRes),
                    contentDescription = stream.streamTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay gradient at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // LIVE red badge (glowing)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(CyberPink, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }

                // Viewer count badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("👁️ ${stream.viewerCount}", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Game name tag overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .background(NeonPurple, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(stream.gameName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            // Streamer details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                    contentDescription = stream.streamerName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, NeonMint, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stream.streamTitle,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "Broadcaster: @${stream.streamerName}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
