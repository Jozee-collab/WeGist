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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserStatsIfMissing(stats: UserStats)
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

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY username ASC")
    fun getAllFriends(): Flow<List<Friend>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun deleteFriend(userId: String)

    @Query("DELETE FROM friends")
    suspend fun clearAllFriends()
}

@Dao
interface FriendRequestDao {
    @Query("SELECT * FROM friend_requests WHERE receiverId = :receiverId AND status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingRequests(receiverId: String = "me"): Flow<List<FriendRequest>>

    @Query("SELECT * FROM friend_requests WHERE senderId = :senderId ORDER BY timestamp DESC")
    fun getSentRequests(senderId: String = "me"): Flow<List<FriendRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: FriendRequest)

    @Update
    suspend fun updateRequest(request: FriendRequest)

    @Query("DELETE FROM friend_requests WHERE id = :requestId")
    suspend fun deleteRequest(requestId: Int)

    @Query("DELETE FROM friend_requests")
    suspend fun clearAllRequests()
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story)

    @Update
    suspend fun updateStory(story: Story)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteStoryById(id: Int)

    @Query("DELETE FROM stories")
    suspend fun clearAllStories()
}


