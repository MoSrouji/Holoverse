package com.example.holoverse.chat_system.data.local

import androidx.room.TypeConverter
import com.example.holoverse.core.domain.model.AppCategory
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromAppCategory(value: AppCategory): String {
        return value.name
    }

    @TypeConverter
    fun toAppCategory(value: String): AppCategory {
        return AppCategory.fromString(value)
    }

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
