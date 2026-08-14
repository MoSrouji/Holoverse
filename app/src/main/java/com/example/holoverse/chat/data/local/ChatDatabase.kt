package com.example.holoverse.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.holoverse.chat.data.local.dao.ChatDao
import com.example.holoverse.chat.data.local.dao.MessageDao
import com.example.holoverse.chat.data.local.entities.ChatEntity
import com.example.holoverse.chat.data.local.entities.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val messageDao: MessageDao

    companion object {
        const val DATABASE_NAME = "holoverse_chat_db"
    }
}

