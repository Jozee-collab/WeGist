package com.example.data

import kotlinx.coroutines.flow.first

/**
 * Seeds the local Room database with demo content on first launch.
 *
 * NOTE: WeGist is a local-first demo — all "other users", streams, posts and
 * messages below are sample data, not real people or network content.
 */
object DatabaseSeeder {

    suspend fun seedIfEmpty(repository: Repository) {
        repository.clearNonUserStreams()

        // Seed UserStats
        repository.insertUserStats(UserStats())

        // Seed Live Streams if empty
        val currentStreams = repository.activeStreams.first()
        if (currentStreams.isEmpty()) {
            repository.insertStream(
                LiveStream(
                    id = "stream_bob",
                    streamerName = "Bob_Destroyer",
                    streamerAvatar = "img_profile_me_1783282394572", // generic
                    streamTitle = "Breaking brick highscores today! 🔥",
                    gameName = "Brick Breaker Classic",
                    thumbnailResName = "img_puzzle_stream_thumbnail_1783282381092",
                    viewerCount = 1420,
                    isUserStream = false
                )
            )
            repository.insertStream(
                LiveStream(
                    id = "stream_queen",
                    streamerName = "SpaceQueen",
                    streamerAvatar = "img_profile_me_1783282394572",
                    streamTitle = "Ranked space matches! 🌌 Let's go!",
                    gameName = "Space Shooter",
                    thumbnailResName = "img_retro_stream_thumbnail_1783282368689",
                    viewerCount = 890,
                    isUserStream = false
                )
            )
        }

        // Seed initial messages to simulate active chats
        val currentMessages = repository.allMessages.first()
        if (currentMessages.isEmpty()) {
            // Alice Chat (chatId = "alice")
            repository.insertMessage(
                ChatMessage(
                    chatId = "alice",
                    senderName = "Alice (Retro Gamer)",
                    senderAvatar = "img_profile_me_1783282394572",
                    content = "Hey! Down for some casual gaming? 🎮",
                    type = "TEXT"
                )
            )
            repository.insertMessage(
                ChatMessage(
                    chatId = "alice",
                    senderName = "Alice (Retro Gamer)",
                    senderAvatar = "img_profile_me_1783282394572",
                    content = "I challenge you to Tic-Tac-Toe! Make your move:",
                    type = "GAME_INVITE",
                    gameType = "TIC_TAC_TOE",
                    gameStatus = "PENDING",
                    gameData = ",,,,,,,," // Empty board (9 commas-separated cells)
                )
            )

            // Bob Chat (chatId = "bob")
            repository.insertMessage(
                ChatMessage(
                    chatId = "bob",
                    senderName = "Bob_Destroyer",
                    senderAvatar = "img_profile_me_1783282394572",
                    content = "What's up, I just went live broadcasting Brick Breaker!",
                    type = "TEXT"
                )
            )
            repository.insertMessage(
                ChatMessage(
                    chatId = "bob",
                    senderName = "Bob_Destroyer",
                    senderAvatar = "img_profile_me_1783282394572",
                    content = "https://wegist.live/stream_bob",
                    type = "MEDIA" // simulated stream sharing
                )
            )
        }

        // Seed initial posts if empty
        val currentPosts = repository.allPosts.first()
        if (currentPosts.isEmpty()) {
            repository.insertPost(
                Post(
                    authorName = "Bob_Destroyer",
                    authorAvatar = "img_profile_me_1783282394572",
                    content = "Just set an absolute new personal highscore in Brick Breaker! Check out my stream tomorrow 🎮🔥 I might stream Rock-Paper-Scissors challenges too!",
                    imageUrl = "img_puzzle_stream_thumbnail_1783282381092",
                    likesCount = 24,
                    commentsCount = 3,
                    hasLiked = false
                )
            )
            repository.insertPost(
                Post(
                    authorName = "SpaceQueen",
                    authorAvatar = "img_profile_me_1783282394572",
                    content = "Space Shooter rank matching is so intense tonight! Who's down for a co-op or 1v1 challenge? Let's go! 🌌🛸🚀",
                    likesCount = 18,
                    commentsCount = 7,
                    hasLiked = false
                )
            )
            repository.insertPost(
                Post(
                    authorName = "Alice (Retro Gamer)",
                    authorAvatar = "img_profile_me_1783282394572",
                    content = "Hello WeGist! Just joined the best gaming social community around! 👾⚡ Mini-games here are so addictive.",
                    likesCount = 31,
                    commentsCount = 12,
                    hasLiked = false
                )
            )
        }

        // Seed initial reels if empty
        val currentReels = repository.allReels.first()
        if (currentReels.isEmpty()) {
            repository.insertReel(
                Reel(
                    creatorName = "Alice (Retro Gamer)",
                    creatorAvatar = "img_profile_me_1783282394572",
                    caption = "Brick Breaker insane double reflection trickshot! 🎯🙌 Double coins incoming!",
                    gameTag = "Brick Breaker Classic",
                    thumbnailResName = "img_puzzle_stream_thumbnail_1783282381092",
                    likesCount = 145,
                    commentsCount = 9,
                    hasLiked = false,
                    viewsCount = 1205
                )
            )
            repository.insertReel(
                Reel(
                    creatorName = "SpaceQueen",
                    creatorAvatar = "img_profile_me_1783282394572",
                    caption = "Surviving with literally 1 HP in the space shooter arena! absolute madness! 🛸💥🌌",
                    gameTag = "Space Shooter",
                    thumbnailResName = "img_retro_stream_thumbnail_1783282368689",
                    likesCount = 289,
                    commentsCount = 24,
                    hasLiked = false,
                    viewsCount = 3491
                )
            )
        }

        // Seed initial friends
        val currentFriends = repository.allFriends.first()
        if (currentFriends.isEmpty()) {
            repository.insertFriend(
                Friend(
                    userId = "alice",
                    username = "Alice (Retro Gamer)",
                    avatarResName = "img_profile_me_1783282394572",
                    globalRank = 42,
                    wins = 32,
                    isOnline = true,
                    presence = "online"
                )
            )
            repository.insertFriend(
                Friend(
                    userId = "bob",
                    username = "Bob_Destroyer",
                    avatarResName = "img_profile_me_1783282394572",
                    globalRank = 110,
                    wins = 25,
                    isOnline = true,
                    presence = "in-game"
                )
            )
            repository.insertFriend(
                Friend(
                    userId = "dave",
                    username = "Dave_The_Noob",
                    avatarResName = "img_profile_me_1783282394572",
                    globalRank = 230,
                    wins = 2,
                    isOnline = false,
                    presence = "offline"
                )
            )
        }

        // Seed initial incoming friend requests
        val currentRequests = repository.pendingRequests.first()
        if (currentRequests.isEmpty()) {
            repository.insertFriendRequest(
                FriendRequest(
                    senderId = "spacequeen",
                    senderName = "SpaceQueen",
                    senderAvatar = "img_profile_me_1783282394572",
                    receiverId = "me",
                    status = "PENDING"
                )
            )
            repository.insertFriendRequest(
                FriendRequest(
                    senderId = "charlie",
                    senderName = "GamerCharlie",
                    senderAvatar = "img_profile_me_1783282394572",
                    receiverId = "me",
                    status = "PENDING"
                )
            )
        }

        // Seed initial stories if empty
        val currentStories = repository.allStories.first()
        if (currentStories.isEmpty()) {
            repository.insertStory(
                Story(
                    authorName = "Alice (Retro Gamer)",
                    authorAvatar = "img_profile_me_1783282394572",
                    textContent = "Warming up with Brick Breaker before tonight's highscore match! 🧱🙌",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isViewed = false
                )
            )
            repository.insertStory(
                Story(
                    authorName = "SpaceQueen",
                    authorAvatar = "img_profile_me_1783282394572",
                    textContent = "Look at this amazing Space Shooter galaxy art background! 🌌🛸",
                    imageUrl = "img_retro_stream_thumbnail_1783282368689",
                    timestamp = System.currentTimeMillis() - 7200000,
                    isViewed = false
                )
            )
            repository.insertStory(
                Story(
                    authorName = "Bob_Destroyer",
                    authorAvatar = "img_profile_me_1783282394572",
                    textContent = "Rethinking my Tic-Tac-Toe strategies. Nobody beats the Destroyer! 😈",
                    timestamp = System.currentTimeMillis() - 10800000,
                    isViewed = true
                )
            )
        }
    }
}
