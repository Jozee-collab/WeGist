package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface LiveStreamDao {
    @Query("SELECT * FROM live_streams WHERE isLive = 1 ORDER BY viewerCount DESC")
    fun getActiveStreams(): Flow<List<LiveStream>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: LiveStream)

    @Update
    suspend fun updateStream(stream: LiveStream)

    @Query("DELETE FROM live_streams WHERE id = :id")
    suspend fun deleteStreamById(id: String)

    @Query("DELETE FROM live_streams WHERE isUserStream = 0")
    suspend fun deleteNonUserStreams()

    @Query("DELETE FROM live_streams")
    suspend fun clearAllStreams()
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    fun getUserStats(userId: String = "me"): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Update
    suspend fun updatePost(post: Post)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: Int)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()
}

@Dao
interface ReelDao {
    @Query("SELECT * FROM reels ORDER BY timestamp DESC")
    fun getAllReels(): Flow<List<Reel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: Reel)

    @Update
    suspend fun updateReel(reel: Reel)

    @Query("DELETE FROM reels WHERE id = :id")
    suspend fun deleteReelById(id: Int)

    @Query("DELETE FROM reels")
    suspend fun clearAllReels()
}

