package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val chatMessageDao: ChatMessageDao,
    private val liveStreamDao: LiveStreamDao,
    private val userStatsDao: UserStatsDao,
    private val postDao: PostDao,
    private val reelDao: ReelDao
) {
    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    val activeStreams: Flow<List<LiveStream>> = liveStreamDao.getActiveStreams()
    val userStats: Flow<UserStats?> = userStatsDao.getUserStats("me")
    val allPosts: Flow<List<Post>> = postDao.getAllPosts()
    val allReels: Flow<List<Reel>> = reelDao.getAllReels()

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesForChat(chatId)

    suspend fun insertMessage(message: ChatMessage): Long =
        chatMessageDao.insertMessage(message)

    suspend fun updateMessage(message: ChatMessage) =
        chatMessageDao.updateMessage(message)

    suspend fun deleteMessageById(id: Int) =
        chatMessageDao.deleteMessageById(id)

    suspend fun insertStream(stream: LiveStream) =
        liveStreamDao.insertStream(stream)

    suspend fun updateStream(stream: LiveStream) =
        liveStreamDao.updateStream(stream)

    suspend fun deleteStreamById(id: String) =
        liveStreamDao.deleteStreamById(id)

    suspend fun clearNonUserStreams() =
        liveStreamDao.deleteNonUserStreams()

    suspend fun insertUserStats(stats: UserStats) =
        userStatsDao.insertUserStats(stats)

    // --- Posts Operations ---
    suspend fun insertPost(post: Post) = postDao.insertPost(post)
    suspend fun updatePost(post: Post) = postDao.updatePost(post)
    suspend fun deletePostById(id: Int) = postDao.deletePostById(id)

    // --- Reels Operations ---
    suspend fun insertReel(reel: Reel) = reelDao.insertReel(reel)
    suspend fun updateReel(reel: Reel) = reelDao.updateReel(reel)
    suspend fun deleteReelById(id: Int) = reelDao.deleteReelById(id)

    suspend fun clearAllData() {
        chatMessageDao.clearAllMessages()
        liveStreamDao.clearAllStreams()
        postDao.clearAllPosts()
        reelDao.clearAllReels()
    }
}
