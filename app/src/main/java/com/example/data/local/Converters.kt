package com.example.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = "|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|||").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
