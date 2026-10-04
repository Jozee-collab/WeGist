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
fun GameChallengeInviteView(
    message: ChatMessage,
    isMeSender: Boolean,
    onPlayTicTacToeMove: (messageId: Int, cellIndex: Int) -> Unit,
    onPlayRockPaperScissors: (messageId: Int, choice: String) -> Unit,
    onPlayTriviaAnswer: (messageId: Int, selectedOptionIdx: Int) -> Unit = { _, _ -> },
    onPlayChessMove: (messageId: Int, fromIdx: Int, toIdx: Int) -> Unit = { _, _, _ -> },
    onPlayConnectFourMove: (messageId: Int, columnIdx: Int) -> Unit = { _, _ -> },
    onPlayBattleshipMove: (messageId: Int, cellIdx: Int) -> Unit = { _, _ -> },
    onPlayDiceDuelMove: (messageId: Int) -> Unit = { _ -> },
    onPlayMemoryMatchMove: (messageId: Int, cardIdx: Int) -> Unit = { _, _ -> },
    onPlayNumberGuesserMove: (messageId: Int, guess: Int) -> Unit = { _, _ -> },
    onPlayBlackjackMove: (messageId: Int, action: String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.VideogameAsset, contentDescription = "Game", tint = NeonMint)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${message.gameType} Challenge",
                fontWeight = FontWeight.Bold,
                color = NeonMint,
                fontSize = 15.sp
            )
        }

        if (message.gameType == "TIC_TAC_TOE") {
            val boardData = message.gameData?.split(",") ?: List(9) { "" }
            val completed = message.gameStatus == "COMPLETED"

            // 3x3 Grid
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0..2) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (col in 0..2) {
                                val idx = row * 3 + col
                                val cell = boardData.getOrElse(idx) { "" }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, CyberBorder)
                                        .clickable(enabled = !completed && cell.isEmpty()) {
                                            onPlayTicTacToeMove(message.id, idx)
                                        }
                                        .testTag("tic_tac_toe_cell_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell,
                                        color = if (cell == "X") NeonMint else if (cell == "O") NeonPurple else Color.Transparent,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text outcome
            Text(
                text = when {
                    completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+100 🪙)"
                    completed && message.gameWinner == "opponent" -> "Winner: Opponent 🤖"
                    completed && message.gameWinner == "DRAW" -> "Tie Game! 🤝"
                    completed -> "Completed"
                    else -> "Your turn: Make a move! 🕹️"
                },
                fontSize = 12.sp,
                color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                fontWeight = FontWeight.Bold
            )

        } else if (message.gameType == "ROCK_PAPER_SCI") {
            val completed = message.gameStatus == "COMPLETED"

            if (!completed) {
                // Rock Paper Scissors Choices
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    val choices = listOf("ROCK" to "🪨", "PAPER" to "📄", "SCISSORS" to "✂️")
                    choices.forEach { (type, emoji) ->
                        Button(
                            onClick = { onPlayRockPaperScissors(message.id, type) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("rps_btn_$type")
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
                Text("Pick Rock, Paper, or Scissors to match!", color = TextGray, fontSize = 11.sp)
            } else {
                // Show completed choice results
                val parts = message.gameData?.split(",") ?: listOf("?", "?")
                val userCh = parts.getOrNull(0) ?: "ROCK"
                val oppCh = parts.getOrNull(1) ?: "ROCK"

                val userEmoji = if (userCh == "ROCK") "🪨" else if (userCh == "PAPER") "📄" else "✂️"
                val oppEmoji = if (oppCh == "ROCK") "🪨" else if (oppCh == "PAPER") "📄" else "✂️"

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", color = TextGray, fontSize = 11.sp)
                        Text(userEmoji, fontSize = 28.sp)
                    }
                    Text("VS", fontWeight = FontWeight.Bold, color = CyberPink)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent", color = TextGray, fontSize = 11.sp)
                        Text(oppEmoji, fontSize = 28.sp)
                    }
                }
                Text(message.content, fontSize = 12.sp, textAlign = TextAlign.Center, color = NeonMint, fontWeight = FontWeight.Bold)
            }
        } else if (message.gameType == "TRIVIA") {
            val completed = message.gameStatus == "COMPLETED"
            val boardData = message.gameData?.split(",") ?: listOf("-1", "-1")
            val chosenIdx = boardData.getOrNull(0)?.toIntOrNull() ?: -1
            val correctIdx = boardData.getOrNull(1)?.toIntOrNull() ?: -1

            val qIdx = Math.abs(message.id.hashCode()) % 3
            val (question, options, realCorrectIdx) = when (qIdx) {
                0 -> Triple(
                    "Which video game is officially the best-selling of all time?",
                    listOf("Tetris", "Grand Theft Auto V", "Minecraft"),
                    2
                )
                1 -> Triple(
                    "In what year was the original Nintendo Entertainment System (NES) released?",
                    listOf("1983", "1985", "1989"),
                    1
                )
                else -> Triple(
                    "What is the name of the main protagonist in the Halo franchise?",
                    listOf("Doomguy", "Master Chief", "Marcus Fenix"),
                    1
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = question,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                options.forEachIndexed { idx, optionText ->
                    val isSelectedByMe = completed && chosenIdx == idx
                    val isCorrectIdx = completed && idx == correctIdx
                    
                    val btnBg = when {
                        isCorrectIdx -> Color(0xFF00A884).copy(alpha = 0.3f)
                        isSelectedByMe -> CyberPink.copy(alpha = 0.3f)
                        else -> CyberBorder
                    }
                    val btnBorderColor = when {
                        isCorrectIdx -> Color(0xFF00A884)
                        isSelectedByMe -> CyberPink
                        else -> CyberBorder.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(btnBg, RoundedCornerShape(8.dp))
                            .border(1.dp, btnBorderColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = !completed) {
                                onPlayTriviaAnswer(message.id, idx)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("trivia_option_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionText,
                            color = if (isCorrectIdx) NeonMint else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (completed) {
                    Text(
                        text = if (chosenIdx == correctIdx) "You solved it correctly! 🎓 (+50 🪙)" else "Whoops! Better luck next time.",
                        color = if (chosenIdx == correctIdx) NeonMint else CyberPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Solve the quiz question before time runs out!",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }
        } else if (message.gameType == "CHESS") {
            val completed = message.gameStatus == "COMPLETED"
            val board = message.gameData?.split(",") ?: List(16) { "." }
            var selectedIdx by remember { mutableStateOf<Int?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "4x4 Tactical Mini-Chess",
                    color = NeonMint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0..3) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (col in 0..3) {
                                    val idx = row * 4 + col
                                    val piece = board.getOrElse(idx) { "." }
                                    val isDarkSquare = (row + col) % 2 == 1
                                    val isSelected = selectedIdx == idx

                                    val squareBg = when {
                                        isSelected -> NeonPurple.copy(alpha = 0.4f)
                                        isDarkSquare -> Color(0xFF1E293B)
                                        else -> Color(0xFF475569)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(squareBg)
                                            .border(0.5.dp, CyberBorder.copy(alpha = 0.3f))
                                            .clickable(enabled = !completed) {
                                                val currentSel = selectedIdx
                                                if (currentSel == null) {
                                                    if (piece in listOf("♖", "♘", "♗", "♔")) {
                                                        selectedIdx = idx
                                                    }
                                                } else {
                                                    if (currentSel == idx) {
                                                        selectedIdx = null
                                                    } else {
                                                        onPlayChessMove(message.id, currentSel, idx)
                                                        selectedIdx = null
                                                    }
                                                }
                                            }
                                            .testTag("chess_cell_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (piece == ".") "" else piece,
                                            fontSize = 24.sp,
                                            color = if (piece in listOf("♜", "♞", "♝", "♚")) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (selectedIdx != null) "Select target square to move..." else "Touch your piece (white bottom row) to select!",
                    color = if (selectedIdx != null) NeonPurple else TextWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else if (message.gameType == "CONNECT_FOUR") {
            val completed = message.gameStatus == "COMPLETED"
            val board = message.gameData?.split(",") ?: List(25) { "" }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("5x5 Connect Four", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0..4) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (col in 0..4) {
                                    val idx = row * 5 + col
                                    val cell = board.getOrElse(idx) { "" }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(2.dp)
                                            .border(1.dp, CyberBorder.copy(alpha = 0.4f), CircleShape)
                                            .clickable(enabled = !completed) {
                                                onPlayConnectFourMove(message.id, col)
                                            }
                                            .testTag("c4_cell_${row}_${col}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cell.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(0.8f)
                                                    .background(
                                                        color = if (cell == "R") CyberPink else NeonMint,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!completed) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0..4) {
                            val isFull = board[col].isNotEmpty()
                            Button(
                                onClick = { onPlayConnectFourMove(message.id, col) },
                                enabled = !isFull,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .testTag("c4_drop_btn_$col")
                            ) {
                                Text("⬇️", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Text(
                    text = when {
                        completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+120 🪙)"
                        completed && message.gameWinner == "opponent" -> "Winner: Alice 🤖"
                        completed && message.gameWinner == "DRAW" -> "Tie Game! 🤝"
                        completed -> "Completed"
                        else -> "Your turn! Tap column drop buttons."
                    },
                    fontSize = 11.sp,
                    color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (message.gameType == "BATTLESHIP") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split("|") ?: listOf()
            if (parts.size >= 4) {
                val playerShots = parts[0].split(",")
                val aiShips = parts[1].split(",")
                val aiShots = parts[2].split(",")
                val playerShips = parts[3].split(",")

                val userHitsCount = playerShots.count { it == "H" }
                val aiHitsCount = aiShots.count { it == "H" }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Tactical Battleship (4x4)", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("My Ships: ${3 - aiHitsCount}/3 ⛵", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("AI Ships: ${3 - userHitsCount}/3 🛸", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (row in 0..3) {
                                Row(modifier = Modifier.weight(1f)) {
                                    for (col in 0..3) {
                                        val idx = row * 4 + col
                                        val cell = playerShots.getOrElse(idx) { "." }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .border(0.5.dp, CyberBorder.copy(alpha = 0.3f))
                                                .clickable(enabled = !completed && cell == ".") {
                                                    onPlayBattleshipMove(message.id, idx)
                                                }
                                                .testTag("bs_cell_$idx"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (cell) {
                                                    "H" -> "🔥"
                                                    "M" -> "💧"
                                                    else -> "🌊"
                                                },
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = when {
                            completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+150 🪙)"
                            completed && message.gameWinner == "opponent" -> "Winner: Alice 🤖"
                            completed -> "Completed"
                            else -> "Tap waves 🌊 to launch torpedoes!"
                        },
                        fontSize = 11.sp,
                        color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (message.gameType == "DICE_DUEL") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split(",") ?: listOf("0", "0", "0", "0", "0")
            val userScore = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val oppScore = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val round = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val lastUser = parts.getOrNull(3)?.toIntOrNull() ?: 0
            val lastOpp = parts.getOrNull(4)?.toIntOrNull() ?: 0

            val diceEmojis = listOf("", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
            val userDiceText = if (lastUser in 1..12) {
                if (lastUser > 6) {
                    "${diceEmojis.getOrElse(lastUser / 2) { "" }} ${diceEmojis.getOrElse(lastUser - (lastUser / 2)) { "" }}"
                } else {
                    diceEmojis[lastUser]
                }
            } else "🎲"
            val oppDiceText = if (lastOpp in 1..12) {
                if (lastOpp > 6) {
                    "${diceEmojis.getOrElse(lastOpp / 2) { "" }} ${diceEmojis.getOrElse(lastOpp - (lastOpp / 2)) { "" }}"
                } else {
                    diceEmojis[lastOpp]
                }
            } else "🎲"

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Dice Duel (Round $round/3)", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", color = TextGray, fontSize = 11.sp)
                        Text(userDiceText, fontSize = 32.sp)
                        Text("$userScore pts", color = CyberPink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text("VS", fontWeight = FontWeight.Black, color = CyberPink, fontSize = 16.sp)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Alice", color = TextGray, fontSize = 11.sp)
                        Text(oppDiceText, fontSize = 32.sp)
                        Text("$oppScore pts", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (!completed) {
                    Button(
                        onClick = { onPlayDiceDuelMove(message.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("dice_roll_btn")
                    ) {
                        Text("Roll Dice 🎲", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = when {
                            message.gameWinner == "me" -> "You won the Duel! 🏆 (+100 🪙)"
                            message.gameWinner == "opponent" -> "Alice won! 🤖"
                            else -> "It's a draw! 🤝"
                        },
                        color = if (message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (message.gameType == "MEMORY_MATCH") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split("|") ?: listOf()
            if (parts.size >= 5) {
                val cardsLayout = parts[0].split(",")
                val matchedArray = parts[1].split(",")
                val firstSel = parts[2].toIntOrNull() ?: -1
                val playerScore = parts[3].toIntOrNull() ?: 0
                val oppScore = parts[4].toIntOrNull() ?: 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Memory Card Match", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Pairs: $playerScore", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("AI Pairs: $oppScore", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (row in 0..3) {
                                Row(modifier = Modifier.weight(1f)) {
                                    for (col in 0..3) {
                                        val idx = row * 4 + col
                                        val emoji = cardsLayout.getOrElse(idx) { "" }
                                        val isMatched = matchedArray.getOrElse(idx) { "" } == "M"
                                        val isSelected = firstSel == idx

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .padding(2.dp)
                                                .background(
                                                    if (isMatched || isSelected) Color(0xFF1E293B) else CyberBorder,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(0.5.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .clickable(enabled = !completed && !isMatched && !isSelected) {
                                                    onPlayMemoryMatchMove(message.id, idx)
                                                }
                                                .testTag("mem_card_$idx"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isMatched || isSelected) emoji else "❓",
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = when {
                            completed && message.gameWinner == "me" -> "Winner: You! 🎉 (+120 🪙)"
                            completed && message.gameWinner == "opponent" -> "Winner: Alice 🤖"
                            completed -> "Tie game!"
                            firstSel != -1 -> "Flip second card!"
                            else -> "Tap cards to find pairs!"
                        },
                        fontSize = 11.sp,
                        color = if (completed && message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (message.gameType == "NUMBER_GUESSER") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split(",") ?: listOf("25", "0", "6", "Start guessing!")
            val attempts = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val maxAttempts = parts.getOrNull(2)?.toIntOrNull() ?: 6
            val feedback = parts.getOrNull(3) ?: "Guess the number!"

            var sliderValue by remember { mutableStateOf(25f) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Guess the Number (1 to 50)", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Attempts: $attempts / $maxAttempts", color = TextGray, fontSize = 11.sp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = feedback,
                        color = if (completed) NeonMint else CyberPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!completed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "My Guess: ${sliderValue.toInt()}",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 1f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonMint,
                                activeTrackColor = NeonMint,
                                inactiveTrackColor = CyberBorder
                            ),
                            modifier = Modifier.testTag("guess_slider")
                        )
                        Button(
                            onClick = { onPlayNumberGuesserMove(message.id, sliderValue.toInt()) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("guess_submit_btn")
                        ) {
                            Text("Submit Guess 🎯", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Secret number was ${parts.getOrNull(0) ?: ""}",
                        color = NeonMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (message.gameType == "BLACKJACK") {
            val completed = message.gameStatus == "COMPLETED"
            val parts = message.gameData?.split("|") ?: listOf("2,3", "5,6")
            val playerHand = parts.getOrNull(0)?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
            val oppHand = parts.getOrNull(1)?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()

            val playerScore = playerHand.sum()
            val oppScore = oppHand.sum()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Blackjack 21 Duel", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Hand", color = TextGray, fontSize = 11.sp)
                        Text(playerHand.joinToString("  "), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Score: $playerScore", color = if (playerScore > 21) Color.Red else NeonMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Alice's Hand", color = TextGray, fontSize = 11.sp)
                        val oppDisplay = if (completed) oppHand.joinToString("  ") else "${oppHand.firstOrNull() ?: ""}  🎴"
                        Text(oppDisplay, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        val oppScoreDisplay = if (completed) oppScore.toString() else "?"
                        Text("Score: $oppScoreDisplay", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (!completed) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onPlayBlackjackMove(message.id, "HIT") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("bj_hit_btn")
                        ) {
                            Text("Hit 🃏", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onPlayBlackjackMove(message.id, "STAND") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBorder),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("bj_stand_btn")
                        ) {
                            Text("Stand 🛑", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = message.content,
                        color = if (message.gameWinner == "me") NeonMint else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (message.gameType == "CUSTOM_LINK") {
            val parts = message.gameData?.split("|") ?: listOf("Custom Game", "", "Challenge link")
            val gameName = parts.getOrNull(0) ?: "Custom Game"
            val link = parts.getOrNull(1) ?: ""
            val desc = parts.getOrNull(2) ?: "Accept challenge to join!"
            val context = androidx.compose.ui.platform.LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game name tag/badge
                Box(
                    modifier = Modifier
                        .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonPurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎮 $gameName CHALLENGE",
                        color = NeonPurple,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // Show URL Link beautifully
                if (link.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Link",
                            tint = NeonMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = link,
                            color = NeonMint,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Join button
                Button(
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "Launching $gameName challenge lobby at:\n$link ... 🚀",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("launch_custom_game_btn")
                ) {
                    Text("Accept & Play Now ⚔️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// In-chat mini-game selector panel

@Composable
fun GameChallengeSelectionView(
    onSelectGame: (String) -> Unit,
    onShowCustomLinkDialog: () -> Unit
) {
    Surface(
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Send In-Chat Casual Challenge", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("TIC_TAC_TOE") }
                        .testTag("challenge_ttt"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("❌ Tic Tac Toe ⭕", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Traditional 3-in-a-row", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("ROCK_PAPER_SCI") }
                        .testTag("challenge_rps"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪨📄✂️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rock, Paper, Scissors", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("CHESS") }
                        .testTag("challenge_chess"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👑 Chess ♞", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tactical 4x4 Chess", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("TRIVIA") }
                        .testTag("challenge_trivia"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💡 Trivia 🎓", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Multiplayer Quiz Challenge", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("CONNECT_FOUR") }
                        .testTag("challenge_connect_four"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🟡 Connect 🔴", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("5x5 Connect Four", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("BATTLESHIP") }
                        .testTag("challenge_battleship"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⛵ Battleship 💥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tactical Ship Hunt", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("DICE_DUEL") }
                        .testTag("challenge_dice_duel"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎲 Dice Duel 🎲", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("3-Round High Roll", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("MEMORY_MATCH") }
                        .testTag("challenge_memory_match"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧠 Match 🧠", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Memory Card Match", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("NUMBER_GUESSER") }
                        .testTag("challenge_number_guesser"),
                    border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯 Guess 🎯", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Secret Number Game", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectGame("BLACKJACK") }
                        .testTag("challenge_blackjack"),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🃏 Blackjack 🃏", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Beat the Dealer to 21", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1D24)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowCustomLinkDialog() }
                    .testTag("challenge_custom_link"),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔗", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Send Custom Invite Link", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Share challenge invite URL for Minecraft, Chess, Roblox etc.", color = TextGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Sticker Selection panel
