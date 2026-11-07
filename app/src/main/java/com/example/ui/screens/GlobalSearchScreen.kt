package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LiveStream
import com.example.data.Post
import com.example.data.Reel
import com.example.data.UserStats
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    posts: List<Post>,
    reels: List<Reel>,
    streams: List<LiveStream>,
    userStats: UserStats,
    onNavigateToChat: (chatId: String) -> Unit,
    onNavigateToStream: (streamId: String) -> Unit,
    onNavigateToPost: (postId: Int) -> Unit,
    onNavigateToReel: (reelId: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") } // ALL, FRIENDS, STREAMS, POSTS

    // Seed list of simulated users on the platform to search for friends
    val staticUsers = remember {
        listOf(
            UserStats(
                userId = "alice",
                username = "Alice (Retro Gamer)",
                avatarResName = "img_profile_me_1783282394572",
                globalRank = 42,
                wins = 32
            ),
            UserStats(
                userId = "bob",
                username = "Bob_Destroyer",
                avatarResName = "img_profile_me_1783282394572",
                globalRank = 110,
                wins = 25
            ),
            UserStats(
                userId = "spacequeen",
                username = "SpaceQueen",
                avatarResName = "img_profile_me_1783282394572",
                globalRank = 18,
                wins = 45
            ),
            UserStats(
                userId = "charlie",
                username = "GamerCharlie",
                avatarResName = "img_profile_me_1783282394572",
                globalRank = 301,
                wins = 12
            ),
            UserStats(
                userId = "dave",
                username = "PixelDave",
                avatarResName = "img_profile_me_1783282394572",
                globalRank = 412,
                wins = 8
            )
        )
    }

    // Filtered results
    val matchingUsers = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) emptyList()
        else staticUsers.filter { it.username.contains(searchQuery, ignoreCase = true) }
    }

    val matchingStreams = remember(searchQuery, streams) {
        if (searchQuery.trim().isEmpty()) emptyList()
        else streams.filter {
            it.streamTitle.contains(searchQuery, ignoreCase = true) ||
            it.streamerName.contains(searchQuery, ignoreCase = true) ||
            it.gameName.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchingPosts = remember(searchQuery, posts) {
        if (searchQuery.trim().isEmpty()) emptyList()
        else posts.filter {
            it.content.contains(searchQuery, ignoreCase = true) ||
            it.authorName.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchingReels = remember(searchQuery, reels) {
        if (searchQuery.trim().isEmpty()) emptyList()
        else reels.filter {
            it.caption.contains(searchQuery, ignoreCase = true) ||
            it.creatorName.contains(searchQuery, ignoreCase = true) ||
            it.gameTag.contains(searchQuery, ignoreCase = true)
        }
    }

    val hasResults = matchingUsers.isNotEmpty() || matchingStreams.isNotEmpty() || matchingPosts.isNotEmpty() || matchingReels.isNotEmpty()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(CyberCard)
                    .statusBarsPadding()
            ) {
                // Main Search Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = TextWhite
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Find friends, streams, posts...", color = TextGray, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NeonMint,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = CyberBorder,
                            focusedContainerColor = CyberSlate,
                            unfocusedContainerColor = CyberSlate,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("global_search_input")
                    )
                }

                // Filter Category Chips Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        CategoryChip(
                            label = "All Results",
                            selected = selectedCategory == "ALL",
                            icon = Icons.Default.AllInclusive,
                            onClick = { selectedCategory = "ALL" },
                            testTag = "chip_all"
                        )
                    }
                    item {
                        CategoryChip(
                            label = "Friends & Users",
                            selected = selectedCategory == "FRIENDS",
                            icon = Icons.Default.People,
                            onClick = { selectedCategory = "FRIENDS" },
                            testTag = "chip_friends"
                        )
                    }
                    item {
                        CategoryChip(
                            label = "Live Streams",
                            selected = selectedCategory == "STREAMS",
                            icon = Icons.Default.Sensors,
                            onClick = { selectedCategory = "STREAMS" },
                            testTag = "chip_streams"
                        )
                    }
                    item {
                        CategoryChip(
                            label = "Trending Gists",
                            selected = selectedCategory == "POSTS",
                            icon = Icons.Default.Share,
                            onClick = { selectedCategory = "POSTS" },
                            testTag = "chip_posts"
                        )
                    }
                }
            }
        },
        containerColor = CyberSlate,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (searchQuery.trim().isEmpty()) {
                // Onboarding Search Suggestion State
                SearchOnboardingView(
                    onSelectSuggestion = { searchQuery = it }
                )
            } else if (!hasResults) {
                // Empty search result screen
                NoSearchResultsView(query = searchQuery)
            } else {
                // Search Results Column
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Friends Category Match
                    if (selectedCategory == "ALL" || selectedCategory == "FRIENDS") {
                        if (matchingUsers.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Friends & Users", icon = Icons.Default.People)
                            }
                            items(matchingUsers) { user ->
                                FriendResultCard(
                                    user = user,
                                    onMessageClick = { onNavigateToChat(user.userId) }
                                )
                            }
                        }
                    }

                    // 2. Streams Category Match
                    if (selectedCategory == "ALL" || selectedCategory == "STREAMS") {
                        if (matchingStreams.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Active Live Streams", icon = Icons.Default.Sensors)
                            }
                            items(matchingStreams) { stream ->
                                StreamResultCard(
                                    stream = stream,
                                    onClick = { onNavigateToStream(stream.id) }
                                )
                            }
                        }
                    }

                    // 3. Posts & Clips Category Match
                    if (selectedCategory == "ALL" || selectedCategory == "POSTS") {
                        if (matchingPosts.isNotEmpty() || matchingReels.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Trending Gists & Clips", icon = Icons.Default.Share)
                            }
                            
                            // Render matched posts
                            items(matchingPosts) { post ->
                                PostResultCard(
                                    post = post,
                                    onClick = { onNavigateToPost(post.id) }
                                )
                            }

                            // Render matched clips
                            items(matchingReels) { reel ->
                                ReelResultCard(
                                    reel = reel,
                                    onClick = { onNavigateToReel(reel.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonMint,
            selectedLabelColor = Color.Black,
            selectedLeadingIconColor = Color.Black,
            containerColor = CyberCard,
            labelColor = TextWhite.copy(alpha = 0.7f),
            iconColor = TextWhite.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, if (selected) NeonMint else CyberBorder),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonPurple,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            color = NeonPurple,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun FriendResultCard(
    user: UserStats,
    onMessageClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMessageClick() }
            .testTag("friend_result_${user.userId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile circular Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonMint.copy(alpha = 0.1f))
                    .border(1.dp, NeonMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                    contentDescription = user.username,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "Rank",
                        tint = AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Rank #${user.globalRank} • ${user.wins} Wins",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onMessageClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("Chat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StreamResultCard(
    stream: LiveStream,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("stream_result_${stream.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail indicator with live badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSlate)
                    .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            ) {
                // Render fallback gaming controller thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(CyberPink.copy(alpha = 0.3f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = CyberPink,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Small red Pulsing Live Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(CyberPink, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream.streamTitle,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${stream.streamerName} playing ${stream.gameName}",
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Viewers",
                        tint = NeonMint,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${stream.viewerCount} watching",
                        color = NeonMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = "Watch",
                tint = TextWhite,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun PostResultCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("post_result_${post.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tiny Avatar
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                        contentDescription = post.authorName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.authorName,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "GIST",
                    color = NeonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .background(NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.content,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = CyberPink,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.likesCount}", color = TextGray, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = NeonMint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.commentsCount}", color = TextGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ReelResultCard(
    reel: Reel,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("reel_result_${reel.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tiny Avatar
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_me_1783282394572),
                        contentDescription = reel.creatorName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reel.creatorName,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "CLIP",
                    color = NeonMint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .background(NeonMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reel.caption,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            // Tag with Game tag
            Text(
                text = "#${reel.gameTag}",
                color = NeonMint,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = CyberPink,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${reel.likesCount}", color = TextGray, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = NeonMint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${reel.viewsCount} views", color = TextGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SearchOnboardingView(
    onSelectSuggestion: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TravelExplore,
            contentDescription = null,
            tint = NeonMint.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Search WeGist Platform",
            color = TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Type friends' names, live streams, game titles, or trending topics above to instantly connect.",
            color = TextGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Trending searches section
        Text(
            text = "TRENDING SEARCHES",
            color = NeonPurple,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val suggestions = listOf(
            "Alice" to Icons.Default.Person,
            "Brick Breaker" to Icons.Default.SportsEsports,
            "SpaceQueen" to Icons.Default.Sensors,
            "Space Shooter" to Icons.Default.RocketLaunch,
            "Bob" to Icons.Default.SmartButton
        )

        suggestions.forEach { (text, icon) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSuggestion(text) }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    color = TextWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = NeonMint,
                    modifier = Modifier.size(16.dp)
                )
            }
            HorizontalDivider(color = CyberBorder)
        }
    }
}

@Composable
fun NoSearchResultsView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = CyberPink.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Matches Found",
            color = TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "We couldn't find any friends, active streams, or posts matching \"$query\".",
            color = TextGray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
