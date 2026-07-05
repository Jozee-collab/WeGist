package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// Space Shooter game structures
data class Laser(var x: Float, var y: Float)
data class Enemy(var x: Float, var y: Float, val speed: Float, val scoreVal: Int, var size: Float = 60f)

@Composable
fun SpaceShooterGame(
    onGameComplete: (score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }

    var playerX by remember { mutableStateOf(300f) }
    val lasers = remember { mutableStateListOf<Laser>() }
    val enemies = remember { mutableStateListOf<Enemy>() }

    // Game loop
    if (isPlaying && !gameOver) {
        LaunchedEffect(Unit) {
            var counter = 0
            while (isPlaying && !gameOver) {
                delay(20) // ~50fps

                // Move lasers upward
                val laserIterator = lasers.iterator()
                while (laserIterator.hasNext()) {
                    val laser = laserIterator.next()
                    laser.y -= 15f
                    if (laser.y < 0) {
                        laserIterator.remove()
                    }
                }

                // Move enemies downward
                val enemyIterator = enemies.iterator()
                while (enemyIterator.hasNext()) {
                    val enemy = enemyIterator.next()
                    enemy.y += enemy.speed
                    
                    // Collision with player (simplistic bounds check at y=800f)
                    if (enemy.y > 800f && enemy.x > playerX - 60f && enemy.x < playerX + 60f && enemy.y < 850f) {
                        lives--
                        enemyIterator.remove()
                        if (lives <= 0) {
                            gameOver = true
                            isPlaying = false
                            onGameComplete(score)
                        }
                    } else if (enemy.y > 900f) { // crossed screen
                        lives--
                        enemyIterator.remove()
                        if (lives <= 0) {
                            gameOver = true
                            isPlaying = false
                            onGameComplete(score)
                        }
                    }
                }

                // Spawn enemies
                counter++
                if (counter % 40 == 0) { // spawn every 40 frames
                    val rx = (50..650).random().toFloat()
                    val speed = (3..7).random().toFloat()
                    enemies.add(Enemy(rx, 0f, speed, 10))
                }

                // Check laser-enemy collision
                val hitLasers = mutableListOf<Laser>()
                val hitEnemies = mutableListOf<Enemy>()

                for (laser in lasers) {
                    for (enemy in enemies) {
                        // Collision check
                        val distSq = (laser.x - enemy.x) * (laser.x - enemy.x) + (laser.y - enemy.y) * (laser.y - enemy.y)
                        if (distSq < 1600) { // 40px radius
                            hitLasers.add(laser)
                            hitEnemies.add(enemy)
                            score += enemy.scoreVal
                        }
                    }
                }

                lasers.removeAll(hitLasers)
                enemies.removeAll(hitEnemies)
            }
        }

        // Fire auto-lasers periodically
        LaunchedEffect(Unit) {
            while (isPlaying && !gameOver) {
                delay(350)
                lasers.add(Laser(playerX, 800f))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(Color(0xFF070709), shape = RoundedCornerShape(12.dp))
            .border(1.dp, CyberBorder, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!isPlaying) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (gameOver) "GAME OVER" else "SPACE SHOOTER",
                    color = if (gameOver) CyberPink else NeonMint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (gameOver) {
                    Text("Final Score: $score pts", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text("Drag spaceship to shoot aliens and protect orbit!", color = TextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(
                    onClick = {
                        score = 0
                        lives = 3
                        gameOver = false
                        playerX = 300f
                        lasers.clear()
                        enemies.clear()
                        isPlaying = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_space_shooter_btn")
                ) {
                    Icon(imageVector = if (gameOver) Icons.Filled.Refresh else Icons.Filled.PlayArrow, contentDescription = "Play")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (gameOver) "Try Again" else "Play Now", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Interactive gameplay Canvas
            Column(modifier = Modifier.fillMaxSize()) {
                // Game HUD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Score: $score", color = NeonMint, fontWeight = FontWeight.Bold)
                    Text("❤️ Lives: $lives", color = CyberPink, fontWeight = FontWeight.Bold)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                playerX = (playerX + dragAmount.x).coerceIn(40f, size.width.toFloat() - 40f)
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw stars
                    drawCircle(Color.White.copy(alpha = 0.3f), 4f, Offset(canvasWidth * 0.2f, canvasHeight * 0.3f))
                    drawCircle(Color.White.copy(alpha = 0.5f), 5f, Offset(canvasWidth * 0.8f, canvasHeight * 0.1f))
                    drawCircle(Color.White.copy(alpha = 0.4f), 3f, Offset(canvasWidth * 0.6f, canvasHeight * 0.7f))
                    drawCircle(Color.White.copy(alpha = 0.2f), 4f, Offset(canvasWidth * 0.4f, canvasHeight * 0.8f))

                    // Draw player spaceship (triangle at playerX, y = canvasHeight - 60)
                    val pY = canvasHeight - 50f
                    val path = Path().apply {
                        moveTo(playerX, pY - 30f)
                        lineTo(playerX - 25f, pY + 15f)
                        lineTo(playerX + 25f, pY + 15f)
                        close()
                    }
                    drawPath(path, color = NeonMint)
                    // Core engine glowing dot
                    drawCircle(Color.White, 8f, Offset(playerX, pY + 5f))

                    // Draw lasers
                    for (laser in lasers) {
                        drawCircle(CyberPink, 8f, Offset(laser.x, laser.y))
                        drawCircle(Color.White, 4f, Offset(laser.x, laser.y))
                    }

                    // Draw enemies (pulsing aliens)
                    for (enemy in enemies) {
                        drawRect(
                            color = NeonPurple,
                            topLeft = Offset(enemy.x - 20f, enemy.y - 20f),
                            size = Size(40f, 40f)
                        )
                        // Alien glowing eye
                        drawCircle(Color.White, 5f, Offset(enemy.x, enemy.y - 5f))
                        drawRect(
                            color = NeonPurple.copy(alpha = 0.3f),
                            topLeft = Offset(enemy.x - 25f, enemy.y - 25f),
                            size = Size(50f, 50f),
                            style = Stroke(2f)
                        )
                    }
                }
            }
        }
    }
}


// Brick Breaker game structure
@Composable
fun BrickBreakerGame(
    onGameComplete: (score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }

    var paddleX by remember { mutableStateOf(300f) }
    var ballX by remember { mutableStateOf(300f) }
    var ballY by remember { mutableStateOf(500f) }
    var ballDX by remember { mutableStateOf(6f) }
    var ballDY by remember { mutableStateOf(-6f) }

    // Represent bricks as active (true) or destroyed (false)
    // 4 rows of 6 columns
    val bricks = remember { mutableStateListOf<Boolean>() }
    
    fun initBricks() {
        bricks.clear()
        for (i in 0 until 24) {
            bricks.add(true)
        }
    }

    if (isPlaying && !gameOver) {
        LaunchedEffect(Unit) {
            while (isPlaying && !gameOver) {
                delay(16) // ~60fps

                // Move ball
                ballX += ballDX
                ballY += ballDY

                // Wall collisions
                if (ballX < 15f) {
                    ballX = 15f
                    ballDX = -ballDX
                }
                if (ballX > 685f) {
                    ballX = 685f
                    ballDX = -ballDX
                }
                if (ballY < 15f) {
                    ballY = 15f
                    ballDY = -ballDY
                }

                // Paddle collision (paddle around y=800f, height=20f, width=120f)
                if (ballY > 780f && ballY < 800f && ballX > paddleX - 60f && ballX < paddleX + 60f) {
                    ballY = 780f
                    ballDY = -ballDY
                    // Deflection angle based on where ball hits paddle
                    val offset = (ballX - paddleX) / 60f
                    ballDX = offset * 8f
                }

                // Check bottom boundary (death)
                if (ballY > 850f) {
                    lives--
                    if (lives <= 0) {
                        gameOver = true
                        isPlaying = false
                        onGameComplete(score)
                    } else {
                        // Reset ball
                        ballX = paddleX
                        ballY = 500f
                        ballDX = 5f
                        ballDY = -5f
                    }
                }

                // Brick collisions
                // Bricks grid starts at y=100f, size of each brick is width=105f, height=35f (approx)
                // Let's assume layout has columns of width 105, rows of height 35
                val brickWidth = 100f
                val brickHeight = 30f
                val startY = 100f

                for (row in 0 until 4) {
                    for (col in 0 until 6) {
                        val index = row * 6 + col
                        if (bricks[index]) {
                            val bx = col * 110f + 30f
                            val by = row * 40f + startY

                            // Simple bounding box collision
                            if (ballX > bx && ballX < bx + brickWidth && ballY > by && ballY < by + brickHeight) {
                                bricks[index] = false
                                ballDY = -ballDY
                                score += 20

                                // Check level clear
                                if (bricks.none { it }) {
                                    // Reward bonus and reset
                                    score += 200
                                    initBricks()
                                    ballY = 500f
                                    ballDY = -ballDY
                                }
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(Color(0xFF070709), shape = RoundedCornerShape(12.dp))
            .border(1.dp, CyberBorder, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!isPlaying) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (gameOver) "GAME OVER" else "BRICK BREAKER",
                    color = if (gameOver) CyberPink else NeonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (gameOver) {
                    Text("Final Score: $score pts", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text("Slide paddle at bottom to deflect ball and smash bricks!", color = TextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(
                    onClick = {
                        score = 0
                        lives = 3
                        gameOver = false
                        paddleX = 300f
                        ballX = 300f
                        ballY = 500f
                        ballDX = 5f
                        ballDY = -5f
                        initBricks()
                        isPlaying = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_brick_breaker_btn")
                ) {
                    Icon(imageVector = if (gameOver) Icons.Filled.Refresh else Icons.Filled.PlayArrow, contentDescription = "Play")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (gameOver) "Try Again" else "Play Now", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Game HUD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Score: $score", color = NeonPurple, fontWeight = FontWeight.Bold)
                    Text("❤️ Lives: $lives", color = CyberPink, fontWeight = FontWeight.Bold)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                paddleX = (paddleX + dragAmount.x).coerceIn(60f, size.width.toFloat() - 60f)
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw bricks
                    val brickWidth = 100f
                    val brickHeight = 30f
                    val startY = 100f

                    for (row in 0 until 4) {
                        for (col in 0 until 6) {
                            val index = row * 6 + col
                            if (bricks[index]) {
                                val bx = col * 110f + 30f
                                val by = row * 40f + startY
                                val brickColor = when (row) {
                                    0 -> CyberPink
                                    1 -> NeonPurple
                                    2 -> NeonMint
                                    else -> AccentOrange
                                }
                                drawRect(
                                    color = brickColor,
                                    topLeft = Offset(bx, by),
                                    size = Size(brickWidth, brickHeight)
                                )
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    topLeft = Offset(bx, by),
                                    size = Size(brickWidth, brickHeight),
                                    style = Stroke(2f)
                                )
                            }
                        }
                    }

                    // Draw sliding paddle at bottom
                    val padY = canvasHeight - 40f
                    drawRoundRect(
                        color = NeonMint,
                        topLeft = Offset(paddleX - 60f, padY),
                        size = Size(120f, 15f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    // Paddle neon stroke accent
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(paddleX - 55f, padY + 2f),
                        size = Size(110f, 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )

                    // Draw bouncing neon ball
                    drawCircle(Color.White, 12f, Offset(ballX, ballY))
                    drawCircle(NeonPurple, 16f, Offset(ballX, ballY), style = Stroke(2f))
                }
            }
        }
    }
}
