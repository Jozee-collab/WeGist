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
fun PostsAndReelsScreen(
    posts: List<Post>,
    reels: List<Reel>,
    stories: List<Story>,
    onAddPost: (String, String?) -> Unit,
    onLikePost: (Post) -> Unit,
    onAddPostComment: (Post) -> Unit,
    onAddReel: (String, String, String) -> Unit,
    onLikeReel: (Reel) -> Unit,
    onAddReelComment: (Reel) -> Unit,
    onViewReel: (Reel) -> Unit,
    onAddStory: (String, String?) -> Unit,
    onMarkStoryAsViewed: (Int) -> Unit,
    onShareContent: (chatId: String, contentType: String, author: String, content: String, mediaUrl: String?, note: String?) -> Unit,
    onSearchClicked: () -> Unit,
    isDarkMode: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf("POSTS") } // POSTS or REELS
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateReelDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var activeStoryToShow by remember { mutableStateOf<Story?>(null) }
    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var activeCommentReel by remember { mutableStateOf<Reel?>(null) }
    var activeSharePost by remember { mutableStateOf<Post?>(null) }
    var activeShareReel by remember { mutableStateOf<Reel?>(null) }
    var customPostComments by remember { mutableStateOf(mapOf<Int, List<String>>()) }
    var customReelComments by remember { mutableStateOf(mapOf<Int, List<String>>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberSlate)
    ) {
        // Immersive Top Sub-header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header Title matching Brand style
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "WeGist",
                        tint = NeonMint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WeGist",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FEED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NeonPurple,
                        modifier = Modifier
                            .background(NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { onToggleTheme(!isDarkMode) },
                        modifier = Modifier.size(28.dp).testTag("feed_theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = "Toggle Theme",
                            tint = TextWhite.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onSearchClicked,
                        modifier = Modifier.size(28.dp).testTag("feed_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Global Search",
                            tint = TextWhite.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Sub-Tab Switcher
                Row(
                    modifier = Modifier
                        .background(CyberSlate, RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedSubTab == "POSTS") NeonMint else Color.Transparent)
                            .clickable { selectedSubTab = "POSTS" }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("tab_sub_posts"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gists",
                            color = if (selectedSubTab == "POSTS") Color.Black else TextWhite.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedSubTab == "REELS") NeonPurple else Color.Transparent)
                            .clickable { selectedSubTab = "REELS" }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("tab_sub_reels"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Clips",
                            color = if (selectedSubTab == "REELS") Color.Black else TextWhite.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = CyberBorder, thickness = 1.dp)

        if (selectedSubTab == "POSTS") {
            // POSTS / GISTS FEED
            Column(modifier = Modifier.fillMaxSize()) {
                // --- STORIES/STATUS TRAY ---
                Text(
                    text = "STATUSES / STORIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonMint,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                )
                
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add My Story trigger item
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showCreateStoryDialog = true }
                                .testTag("add_story_trigger")
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                    contentDescription = "My Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CyberBorder, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(NeonPurple)
                                        .border(1.5.dp, CyberCard, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Story",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("My Status", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Friends' Stories
                    items(stories) { story ->
                        val borderBrush = if (story.isViewed) {
                            Brush.sweepGradient(listOf(TextGray.copy(alpha = 0.5f), TextGray.copy(alpha = 0.5f)))
                        } else {
                            Brush.sweepGradient(listOf(NeonMint, NeonPurple, NeonMint))
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    activeStoryToShow = story
                                    onMarkStoryAsViewed(story.id)
                                }
                                .testTag("story_item_${story.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .border(2.5.dp, borderBrush, CircleShape)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                                    contentDescription = "${story.authorName}'s story",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (story.authorName.length > 10) story.authorName.take(8) + ".." else story.authorName,
                                color = TextWhite,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider(color = CyberBorder, thickness = 1.dp)

                // Post Input Card at the top of Feed
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showCreatePostDialog = true }
                        .testTag("create_post_trigger_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                            contentDescription = "My Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.dp, NeonMint, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "What gaming gist is on your mind?",
                            color = TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach Image",
                            tint = NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Forum,
                                contentDescription = "No Posts",
                                tint = TextGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No gaming gists shared yet.", color = TextGray)
                            Text("Share your gameplay updates with everyone!", color = TextGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(posts) { post ->
                            PostCard(
                                post = post,
                                onLike = { onLikePost(post) },
                                onComment = { activeCommentPost = post },
                                onShare = { activeSharePost = post }
                            )
                        }
                    }
                }
            }
        } else {
            // REELS / CLIPS VERTICAL PLAYER
            ReelsPlayerView(
                reels = reels,
                onLikeReel = onLikeReel,
                onAddReelComment = { activeCommentReel = it },
                onShareReel = { activeShareReel = it },
                onViewReel = onViewReel,
                onPostReelClick = { showCreateReelDialog = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // CREATE STORY DIALOG
    if (showCreateStoryDialog) {
        CreateStoryDialog(
            onAdd = onAddStory,
            onDismiss = { showCreateStoryDialog = false }
        )
    }

    // STORY VIEWER DIALOG
    val currentStoryToShow = activeStoryToShow
    if (currentStoryToShow != null) {
        StoryViewerDialog(
            story = currentStoryToShow,
            onDismiss = { activeStoryToShow = null }
        )
    }

    // CREATE POST DIALOG
    if (showCreatePostDialog) {
        var postText by remember { mutableStateOf("") }
        var selectedImageOption by remember { mutableStateOf<String?>(null) } // "retro", "puzzle", or none

        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            title = { Text("Create Gist", color = TextWhite, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Broadcast a post update to WeGist friends! Earn +50 coins.", color = TextGray, fontSize = 12.sp)

                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text("Type your gaming gist...", color = TextGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("post_input_field")
                    )

                    Text("Attach Gameplay Thumbnail (Optional)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Option 1: Space Shooter
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedImageOption = if (selectedImageOption == "img_retro_stream_thumbnail_1783282368689") null else "img_retro_stream_thumbnail_1783282368689"
                                },
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (selectedImageOption == "img_retro_stream_thumbnail_1783282368689") NeonMint else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = R.drawable.img_retro_stream_thumbnail_1783282368689),
                                    contentDescription = "Space Shooter option",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Space", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }

                        // Option 2: Brick Breaker
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedImageOption = if (selectedImageOption == "img_puzzle_stream_thumbnail_1783282381092") null else "img_puzzle_stream_thumbnail_1783282381092"
                                },
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (selectedImageOption == "img_puzzle_stream_thumbnail_1783282381092") NeonMint else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = R.drawable.img_puzzle_stream_thumbnail_1783282381092),
                                    contentDescription = "Brick Breaker option",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Bricks", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postText.isNotBlank()) {
                            onAddPost(postText, selectedImageOption)
                            showCreatePostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("submit_post_btn")
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = CyberCard,
            tonalElevation = 8.dp
        )
    }

    // CREATE REEL DIALOG
    if (showCreateReelDialog) {
        var reelCaption by remember { mutableStateOf("") }
        var selectedGameTag by remember { mutableStateOf("Space Shooter") }

        AlertDialog(
            onDismissRequest = { showCreateReelDialog = false },
            title = { Text("Share Retro Clip", color = TextWhite, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Post a highscore video simulation to Clips! Earn +100 coins.", color = TextGray, fontSize = 12.sp)

                    OutlinedTextField(
                        value = reelCaption,
                        onValueChange = { reelCaption = it },
                        placeholder = { Text("Add an exciting caption...", color = TextGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reel_input_field")
                    )

                    Text("Select Game Category", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedGameTag == "Space Shooter") NeonPurple.copy(alpha = 0.3f) else CyberSlate)
                                .border(1.dp, if (selectedGameTag == "Space Shooter") NeonPurple else CyberBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedGameTag = "Space Shooter" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Space Shooter", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedGameTag == "Brick Breaker Classic") NeonPurple.copy(alpha = 0.3f) else CyberSlate)
                                .border(1.dp, if (selectedGameTag == "Brick Breaker Classic") NeonPurple else CyberBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedGameTag = "Brick Breaker Classic" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Brick Breaker", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reelCaption.isNotBlank()) {
                            val thumbnail = if (selectedGameTag == "Space Shooter") "img_retro_stream_thumbnail_1783282368689" else "img_puzzle_stream_thumbnail_1783282381092"
                            onAddReel(reelCaption, selectedGameTag, thumbnail)
                            showCreateReelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("submit_reel_btn")
                ) {
                    Text("Post Clip", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateReelDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = CyberCard,
            tonalElevation = 8.dp
        )
    }

    activeCommentPost?.let { post ->
        GistCommentDialog(
            post = post,
            customComments = customPostComments[post.id] ?: emptyList(),
            onAddComment = { commentText ->
                onAddPostComment(post)
                val currentList = customPostComments[post.id] ?: emptyList()
                customPostComments = customPostComments + (post.id to (currentList + commentText))
            },
            onDismiss = { activeCommentPost = null }
        )
    }

    activeCommentReel?.let { reel ->
        ClipCommentDialog(
            reel = reel,
            customComments = customReelComments[reel.id] ?: emptyList(),
            onAddComment = { commentText ->
                onAddReelComment(reel)
                val currentList = customReelComments[reel.id] ?: emptyList()
                customReelComments = customReelComments + (reel.id to (currentList + commentText))
            },
            onDismiss = { activeCommentReel = null }
        )
    }

    activeSharePost?.let { post ->
        ShareToChatDialog(
            title = "Shared Gist",
            content = post.content,
            author = post.authorName,
            mediaUrl = post.imageUrl,
            contentType = "POST",
            onShare = { chatId, note ->
                onShareContent(chatId, "POST", post.authorName, post.content, post.imageUrl, note)
            },
            onDismiss = { activeSharePost = null }
        )
    }

    activeShareReel?.let { reel ->
        ShareToChatDialog(
            title = "Shared Clip",
            content = reel.caption,
            author = reel.creatorName,
            mediaUrl = reel.thumbnailResName,
            contentType = "REEL",
            onShare = { chatId, note ->
                onShareContent(chatId, "REEL", reel.creatorName, reel.caption, reel.thumbnailResName, note)
            },
            onDismiss = { activeShareReel = null }
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(post.timestamp) { formatter.format(Date(post.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Post Author Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val authorAvatarRes = R.drawable.img_profile_me_1783282394572 // fallback / standard
                Image(
                    painter = painterResource(id = authorAvatarRes),
                    contentDescription = post.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, NeonPurple, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "Gamer • $formattedTime",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = TextWhite,
                lineHeight = 20.sp
            )

            // Attached game snapshot image
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val imageRes = when (post.imageUrl) {
                    "img_retro_stream_thumbnail_1783282368689" -> R.drawable.img_retro_stream_thumbnail_1783282368689
                    else -> R.drawable.img_puzzle_stream_thumbnail_1783282381092
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Attached Gameplay",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Like / Comment / Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button (Animated)
                val likeScale by animateFloatAsState(
                    targetValue = if (post.hasLiked) 1.2f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                    label = "LikeAnimation"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLike() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.hasLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.hasLiked) CyberPink else TextGray,
                        modifier = Modifier
                            .size(20.dp)
                            .animateContentSize()
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.likesCount.toString(),
                        color = if (post.hasLiked) CyberPink else TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Comment Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onComment() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("comment_post_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.commentsCount.toString(),
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                // Share Button
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp).testTag("share_post_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Share",
                        tint = TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

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
