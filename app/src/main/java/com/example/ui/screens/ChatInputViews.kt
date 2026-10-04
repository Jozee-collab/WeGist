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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.ChatMessage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VoiceWaveformPlayer() {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    if (isPlaying) {
        LaunchedEffect(Unit) {
            while (progress < 1.0f) {
                delay(100)
                progress += 0.05f
            }
            isPlaying = false
            progress = 0f
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(200.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .background(NeonMint, CircleShape)
                .size(30.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        // Wave representation lines
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heights = listOf(14, 25, 10, 32, 18, 28, 12, 22, 15, 30, 8, 20)
            heights.forEachIndexed { idx, h ->
                val fraction = idx.toFloat() / heights.size
                val active = fraction < progress
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(h.dp)
                        .background(if (active) NeonMint else TextGray, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

// Custom UI rendering in-chat casual game board states

@Composable
fun StickersSelectionView(
    onSelectSticker: (String) -> Unit
) {
    Surface(
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gaming Stickers", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val stickers = listOf("👾", "💣", "🏆", "🎮", "🕹️", "💥", "🔥", "👑")
                stickers.forEach { sticker ->
                    Text(
                        text = sticker,
                        fontSize = 36.sp,
                        modifier = Modifier
                            .clickable { onSelectSticker(sticker) }
                            .padding(4.dp)
                            .testTag("sticker_$sticker")
                    )
                }
            }
        }
    }
}

// Attachment Selection view (WhatsApp Style)

@Composable
fun AttachmentSelectionView(
    onSelectFile: (name: String, size: String, type: String) -> Unit,
    onPayCoinsClicked: () -> Unit
) {
    Surface(
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Share Files (WhatsApp Style)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val whatsappFiles = listOf(
                    Triple("Document", Icons.Default.Description, Color(0xFF2196F3)),
                    Triple("Gallery", Icons.Default.Image, Color(0xFFE040FB)),
                    Triple("Audio", Icons.Default.Audiotrack, Color(0xFFFF9800)),
                    Triple("Location", Icons.Default.Place, Color(0xFF4CAF50)),
                    Triple("Apk / Mod", Icons.Default.Android, Color(0xFF8BC34A)),
                    Triple("Pay Coins", Icons.Default.MonetizationOn, AccentOrange)
                )
                
                whatsappFiles.forEach { fileAction ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                if (fileAction.first == "Pay Coins") {
                                    onPayCoinsClicked()
                                } else {
                                    val picked = when (fileAction.first) {
                                        "Document" -> Triple("Strategic_Guild_Rules.pdf", "1.8 MB", "PDF")
                                        "Gallery" -> Triple("fortnite_victory_royale.jpg", "640 KB", "IMAGE")
                                        "Audio" -> Triple("voice_chat_discord.mp3", "3.4 MB", "AUDIO")
                                        "Location" -> Triple("Gamer_Guild_HQ.loc", "45 bytes", "LOCATION")
                                        "Apk / Mod" -> Triple("wegist_game_mods.zip", "15.2 MB", "ZIP")
                                        else -> Triple("speedrun_proof.mp4", "18.1 MB", "VIDEO")
                                    }
                                    onSelectFile(picked.first, picked.second, picked.third)
                                }
                            }
                            .padding(4.dp)
                            .testTag("attach_${fileAction.first.replace(" ", "_").lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(fileAction.third, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = fileAction.second,
                                contentDescription = fileAction.first,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(fileAction.first, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
