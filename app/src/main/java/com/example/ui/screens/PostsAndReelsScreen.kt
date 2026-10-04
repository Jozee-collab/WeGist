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
