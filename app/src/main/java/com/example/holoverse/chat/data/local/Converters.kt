package com.example.holoverse.chat.data.local

import androidx.room.TypeConverter
import com.example.holoverse.chat.domain.model.Poll
import com.example.holoverse.chat.domain.model.BookingRequest
import com.example.holoverse.core.domain.model.AppCategory
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromPoll(value: Poll?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toPoll(value: String?): Poll? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromBookingRequest(value: BookingRequest?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toBookingRequest(value: String?): BookingRequest? {
        return value?.let { Json.decodeFromString(it) }
    }

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

