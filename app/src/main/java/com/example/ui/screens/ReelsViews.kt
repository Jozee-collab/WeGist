package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Post
import com.example.data.Reel
import com.example.data.Story
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReelsPlayerView(
    reels: List<Reel>,
    onLikeReel: (Reel) -> Unit,
    onAddReelComment: (Reel) -> Unit,
    onShareReel: (Reel) -> Unit,
    onViewReel: (Reel) -> Unit,
    onPostReelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (reels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No gaming clips available yet.", color = TextGray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPostReelClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.Black)
                ) {
                    Text("Post a Retro Clip", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var activeIndex by remember { mutableStateOf(0) }
    val currentReel = reels.getOrNull(activeIndex)

    if (currentReel != null) {
        // Increment view count when loaded
        LaunchedEffect(currentReel.id) {
            onViewReel(currentReel)
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -50) {
                            // Swipe up -> Next Reel
                            if (activeIndex < reels.size - 1) {
                                activeIndex++
                            }
                        } else if (dragAmount > 50) {
                            // Swipe down -> Prev Reel
                            if (activeIndex > 0) {
                                activeIndex--
                            }
                        }
                    }
                }
        ) {
            // Immersive Game Clip Simulation rendered on a Canvas
            LiveGameClipSimulator(
                gameTag = currentReel.gameTag,
                modifier = Modifier.fillMaxSize()
            )

            // Side HUD Controller bar (Like, Comment, Info, Upload)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .padding(bottom = 120.dp, end = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Profile Image of Creator
                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                            contentDescription = currentReel.creatorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, NeonPurple, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .offset(y = 6.dp)
                                .background(NeonPurple, CircleShape)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Follow",
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    // Like Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clip(CircleShape)
                                .clickable { onLikeReel(currentReel) }
                                .testTag("like_reel_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentReel.hasLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Reel",
                                tint = if (currentReel.hasLiked) CyberPink else TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentReel.likesCount.toString(),
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Comment Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clip(CircleShape)
                                .clickable { onAddReelComment(currentReel) }
                                .testTag("comment_reel_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Comment Reel",
                                tint = TextWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentReel.commentsCount.toString(),
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share Reel Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clip(CircleShape)
                                .clickable { onShareReel(currentReel) }
                                .testTag("share_reel_btn_${currentReel.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Share Reel",
                                tint = TextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Upload / Publish Reel icon
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(NeonPurple, CircleShape)
                            .clip(CircleShape)
                            .clickable { onPostReelClick() }
                            .testTag("publish_clip_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = "Post Clip",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Info Panel (Caption, user name, and current gameplay tag)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp, start = 16.dp, end = 80.dp, top = 40.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "@${currentReel.creatorName}",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, NeonPurple, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentReel.gameTag,
                                color = NeonPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = currentReel.caption,
                        color = TextWhite.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Views",
                            tint = TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${currentReel.viewsCount} views",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Swipe instruction indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (activeIndex > 0) activeIndex-- },
                    enabled = activeIndex > 0,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Previous Clip",
                        tint = if (activeIndex > 0) TextWhite else Color.Transparent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Clip ${activeIndex + 1} of ${reels.size} (Swipe Up/Down)",
                    color = TextWhite.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { if (activeIndex < reels.size - 1) activeIndex++ },
                    enabled = activeIndex < reels.size - 1,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next Clip",
                        tint = if (activeIndex < reels.size - 1) TextWhite else Color.Transparent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual Gameplay Simulation drawn dynamically on a Canvas to give the Reel / Clip an
 * extremely rich and premium active feel.
 */

@Composable
fun LiveGameClipSimulator(
    gameTag: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GameSimulation")

    // Dynamic state modifiers driven by infinite loop transitions
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709))
    ) {
        val width = size.width
        val height = size.height

        // Draw an outer futuristic grid lines representation
        val gridStep = 60f
        val linePaintColor = Color(0xFF14141A)
        for (x in 0..width.toInt() step gridStep.toInt()) {
            drawLine(
                color = linePaintColor,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), height),
                strokeWidth = 1f
            )
        }
        for (y in 0..height.toInt() step gridStep.toInt()) {
            drawLine(
                color = linePaintColor,
                start = Offset(0f, y.toFloat()),
                end = Offset(width, y.toFloat()),
                strokeWidth = 1f
            )
        }

        if (gameTag == "Space Shooter") {
            // SPACE SHOOTER VIEW SIMULATION:
            // Drawing stars, spaceships, laser beams, and invaders flying!
            // 1. Stars background
            for (i in 0..25) {
                val seedX = (width * ((i * 17) % 100) / 100f)
                val seedY = ((height * ((i * 31) % 100) / 100f) + (time * 5)) % height
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 3f,
                    center = Offset(seedX, seedY)
                )
            }

            // 2. Spaceship player at bottom
            val playerX = width / 2f + (kotlin.math.sin(time / 10f) * 120f)
            val playerY = height - 320f
            
            // Draw space shooter fighter
            drawRect(
                color = NeonMint,
                topLeft = Offset(playerX - 25f, playerY),
                size = androidx.compose.ui.geometry.Size(50f, 30f)
            )
            drawRect(
                color = NeonPurple,
                topLeft = Offset(playerX - 5f, playerY - 15f),
                size = androidx.compose.ui.geometry.Size(10f, 15f)
            )

            // Laser beams
            val laserY = playerY - 30f - ((time * 15f) % (playerY - 100f))
            drawRect(
                color = CyberPink,
                topLeft = Offset(playerX - 3f, laserY),
                size = androidx.compose.ui.geometry.Size(6f, 25f)
            )

            // Invaders bouncing at top
            val rowXOffset = (kotlin.math.cos(time / 20f) * 80f)
            for (col in 0..4) {
                val invaderX = (width / 2f) - 150f + (col * 75f) + rowXOffset
                val invaderY = 250f + (kotlin.math.sin(time / 10f + col) * 15f)

                // Draw alien invader boxes
                drawCircle(
                    color = NeonPurple,
                    radius = 16f,
                    center = Offset(invaderX, invaderY)
                )
                drawRect(
                    color = CyberPink,
                    topLeft = Offset(invaderX - 8f, invaderY + 6f),
                    size = androidx.compose.ui.geometry.Size(16f, 6f)
                )
            }
        } else {
            // BRICK BREAKER CLASSIC VIEW SIMULATION:
            // Drawing bricks array at top, bouncing ball, and paddle moving at the bottom!
            // 1. Draw Brick Grid rows
            val brickWidth = (width - 120f) / 6f
            val brickHeight = 40f
            val brickGap = 10f

            for (row in 0..3) {
                val rowColor = when (row) {
                    0 -> CyberPink
                    1 -> NeonPurple
                    2 -> AccentOrange
                    else -> NeonMint
                }
                for (col in 0..5) {
                    // Seed some broken bricks based on time calculation to simulate active game progression
                    val isBroken = (col + row * 2) % 4 == (time.toInt() / 25) % 4
                    if (!isBroken) {
                        drawRect(
                            color = rowColor,
                            topLeft = Offset(60f + (col * (brickWidth + brickGap)), 200f + (row * (brickHeight + brickGap))),
                            size = androidx.compose.ui.geometry.Size(brickWidth, brickHeight)
                        )
                    }
                }
            }

            // 2. Bottom Paddle tracking the ball
            val ballPeriodX = kotlin.math.sin(time / 8f) * (width / 2f - 100f)
            val paddleX = (width / 2f) + ballPeriodX
            val paddleY = height - 280f

            drawRect(
                color = NeonMint,
                topLeft = Offset(paddleX - 60f, paddleY),
                size = androidx.compose.ui.geometry.Size(120f, 15f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            // 3. Bouncing Ball with active path simulation
            val ballPeriodY = kotlin.math.abs(kotlin.math.sin(time / 4f)) * (paddleY - 450f)
            val ballX = (width / 2f) + ballPeriodX
            val ballY = paddleY - ballPeriodY - 15f

            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(ballX, ballY)
            )
        }
    }
}
