package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMessage::class, LiveStream::class, UserStats::class, Post::class, Reel::class, Friend::class, FriendRequest::class, Story::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun liveStreamDao(): LiveStreamDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun postDao(): PostDao
    abstract fun reelDao(): ReelDao
    abstract fun friendDao(): FriendDao
    abstract fun friendRequestDao(): FriendRequestDao
    abstract fun storyDao(): StoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wegist_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
