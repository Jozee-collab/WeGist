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
fun GistCommentDialog(
    post: Post,
    customComments: List<String>,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    val preseeded = remember(post.id, post.authorName) {
        when (post.authorName) {
            "Bob_Destroyer" -> listOf(
                "Bob, you are literally the retro breaker god! 🎮🏆",
                "Can't wait to tune into the stream tomorrow, show us that brick reflection trick again!"
            )
            "SpaceQueen" -> listOf(
                "Count me in for a co-op challenge tonight! 🌌🛸🚀",
                "Your gameplay is always top-tier, let's smash some invaders!"
            )
            else -> listOf(
                "Awesome update! Love the vibes here. 🔥",
                "Add me as a friend, let's play soon! 👾"
            )
        }
    }
    
    val allComments = remember(preseeded, customComments) {
        preseeded + customComments
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = NeonMint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comments (${allComments.size})",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSlate),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "@${post.authorName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NeonPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.content,
                                    fontSize = 13.sp,
                                    color = TextWhite,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    items(allComments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCard, RoundedCornerShape(8.dp))
                                .border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(NeonPurple.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, NeonPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (preseeded.contains(comment)) "WeGist Gamer" else "You (WeGist Member)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (preseeded.contains(comment)) TextWhite.copy(alpha = 0.8f) else NeonMint
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment,
                                    fontSize = 12.sp,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Write a comment...", color = TextGray.copy(alpha = 0.5f), fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                onAddComment(text)
                                text = ""
                            }
                        },
                        modifier = Modifier
                            .background(NeonMint, CircleShape)
                            .size(36.dp)
                            .testTag("submit_comment_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Post Comment",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonMint)
            }
        },
        containerColor = CyberCard,
        tonalElevation = 8.dp
    )
}

@Composable
fun ClipCommentDialog(
    reel: Reel,
    customComments: List<String>,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    val preseeded = remember(reel.id, reel.creatorName) {
        when (reel.creatorName) {
            "Alice (Retro Gamer)" -> listOf(
                "OMG that reflection shot is insane! 🙌🔥",
                "I've been trying to pull this off for hours. Masterclass!"
            )
            "SpaceQueen" -> listOf(
                "How did you survive on 1 HP? Breathtaking reflex! 🛸💥",
                "Space shooter matches are so heart-pounding!"
            )
            else -> listOf(
                "Awesome clip, clean gameplay! 🎯🎮",
                "This is peak retro gaming content."
            )
        }
    }
    
    val allComments = remember(preseeded, customComments) {
        preseeded + customComments
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clip Comments (${allComments.size})",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSlate),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "@${reel.creatorName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NeonPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = reel.caption,
                                    fontSize = 13.sp,
                                    color = TextWhite,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    items(allComments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCard, RoundedCornerShape(8.dp))
                                .border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(NeonPurple.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, NeonPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (preseeded.contains(comment)) "WeGist Member" else "You (WeGist Member)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (preseeded.contains(comment)) TextWhite.copy(alpha = 0.8f) else NeonPurple
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment,
                                    fontSize = 12.sp,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Write a comment...", color = TextGray.copy(alpha = 0.5f), fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reel_comment_input"),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                onAddComment(text)
                                text = ""
                            }
                        },
                        modifier = Modifier
                            .background(NeonPurple, CircleShape)
                            .size(36.dp)
                            .testTag("submit_reel_comment_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Post Comment",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonPurple)
            }
        },
        containerColor = CyberCard,
        tonalElevation = 8.dp
    )
}

@Composable
fun ShareToChatDialog(
    title: String,
    content: String,
    author: String,
    mediaUrl: String?,
    contentType: String, // "POST" or "REEL"
    onShare: (chatId: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var shareSuccessByChat by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val contacts = listOf(
        Pair("alice", "Alice (Retro Gamer)"),
        Pair("bob", "Bob_Destroyer"),
        Pair("guild", "Gaming Guild")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = NeonMint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Repost to Chat",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mini preview of shared content
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlate),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (contentType == "POST") "Gist by @$author" else "Clip by @$author",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = content,
                            fontSize = 12.sp,
                            color = TextWhite,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Custom Note field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Add a note to your friend...", color = TextGray.copy(alpha = 0.5f), fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMint,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_note_input"),
                    singleLine = true
                )

                Text(
                    text = "Select a conversation to share with:",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                // Contact list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contacts.forEach { (chatId, name) ->
                        val hasShared = shareSuccessByChat == chatId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCard, RoundedCornerShape(10.dp))
                                .border(1.dp, if (hasShared) NeonMint else CyberBorder, RoundedCornerShape(10.dp))
                                .clickable(enabled = !hasShared) {
                                    onShare(chatId, noteText)
                                    shareSuccessByChat = chatId
                                    scope.launch {
                                        delay(1500)
                                        shareSuccessByChat = null
                                        onDismiss()
                                    }
                                }
                                .padding(12.dp)
                                .testTag("share_to_contact_$chatId"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(NeonPurple.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, NeonPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = name,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (hasShared) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Shared",
                                    tint = NeonMint,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Send",
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeonMint)
            }
        },
        containerColor = CyberCard,
        tonalElevation = 8.dp
    )
}

@Composable
fun CreateStoryDialog(
    onAdd: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var textContent by remember { mutableStateOf("") }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val mockImageOptions = listOf(
        Pair("img_retro_stream_thumbnail_1783282368689", "Space Arena"),
        Pair("img_puzzle_stream_thumbnail_1783282381092", "Brick Breaker Grid"),
        Pair("img_profile_me_1783282394572", "Co-op Lobby")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Gaming Story",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    placeholder = { Text("What's happening in your game right now? 🎮", color = TextGray.copy(alpha = 0.5f), fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("create_story_text_input")
                )

                Text(
                    text = "Choose a gaming backdrop (Optional):",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mockImageOptions.forEach { (resName, label) ->
                        val isSelected = selectedImageUrl == resName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(CyberSlate, RoundedCornerShape(8.dp))
                                .border(
                                    2.dp,
                                    if (isSelected) NeonPurple else CyberBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedImageUrl = if (isSelected) null else resName
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) NeonPurple else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textContent.isNotBlank()) {
                        onAdd(textContent, selectedImageUrl)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_story_btn")
            ) {
                Text("Share", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        },
        containerColor = CyberCard,
        tonalElevation = 8.dp
    )
}

@Composable
fun StoryViewerDialog(
    story: Story,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(story) {
        val duration = 5000L
        val interval = 50L
        val steps = duration / interval
        for (i in 1..steps) {
            delay(interval)
            progress = i.toFloat() / steps
        }
        onDismiss()
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .testTag("story_viewer_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSlate),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .height(4.dp),
                    color = NeonMint,
                    trackColor = TextGray.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, NeonMint, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.authorName,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Gamer Status • Active Now",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextWhite
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .background(CyberCard, RoundedCornerShape(12.dp))
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (story.imageUrl != null) {
                        Image(
                            painter = painterResource(id = R.drawable.img_retro_stream_thumbnail_1783282368689),
                            contentDescription = "Story image backdrop",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f))
                        )
                    }

                    Text(
                        text = story.textContent,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = NeonMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swipe up or reply to Status",
                        color = NeonMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
