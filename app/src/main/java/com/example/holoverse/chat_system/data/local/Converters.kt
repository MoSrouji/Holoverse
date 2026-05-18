package com.example.holoverse.chat_system.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        return Json.decodeFromString(value)
    }
}
